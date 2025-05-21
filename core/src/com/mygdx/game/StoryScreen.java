package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label; // Import ที่ต้องมี
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.Align;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mygdx.game.BaseGame.setActiveScreen;

/**
 * StoryScreen - Handles story progression, dialogue,
 * and displaying multiple characters (Toei, Guy, Kitty, Chicha) with emotions.
 * *** Added Auto-Play Feature (2s after text) + Colored Status Label (Formatted) ***
 */
public class StoryScreen extends BaseScreen {

    Scene scene;
    Background background;

    // --- Characters ---
    Toei toei;
    Guy guy;
    Kitty kitty;
    Chicha chicha;

    BaseActor currentlySpeaking = null;

    // --- UI Elements ---
    DialogBox dialogBox;
    BaseActor continueKey;
    Table buttonTable;
    Label autoPlayStatusLabel; // Label สำหรับสถานะ Auto-Play

    // --- Constants ---
    final float CHARACTER_SCALE_PERCENTAGE = 0.8f;
    final float CHARACTER_Y_POSITION = 00f;

    // --- Auto-Play Variables ---
    private boolean isAutoPlayActive = false;
    private float autoPlayTimer = 0f;
    private boolean waitingForAutoPlayDelay = false; // สถานะ: กำลังรอดีเลย์หลังข้อความจบหรือไม่
    private static final float AUTO_PLAY_DELAY = 2.0f; // รอ 2 วินาทีหลังข้อความจบ

    @Override
    public void initialize() {
        // --- Background Setup ---
        background = new Background(0, 0, mainStage);
        background.setOpacity(0);
        BaseActor.setWorldBounds(background.getWidth(), background.getHeight());
        float screenHeight = BaseActor.getWorldBounds().height;
        float screenWidth = BaseActor.getWorldBounds().width;

        // --- Character Initialization ---
        toei = new Toei(0, 0, mainStage);
        calculateAndSetSize(toei, 495f, 600f, screenHeight * CHARACTER_SCALE_PERCENTAGE);
        toei.setVisible(false);
        toei.setPosition(-toei.getWidth(), 0);

        guy = new Guy(0, 0, mainStage);
        calculateAndSetSize(guy, 374f, 600f, screenHeight * CHARACTER_SCALE_PERCENTAGE);
        guy.setVisible(false);
        guy.setPosition(-guy.getWidth(), 0);

        kitty = new Kitty(0, 0, mainStage);
        calculateAndSetSize(kitty, 516f, 600f, screenHeight * CHARACTER_SCALE_PERCENTAGE);
        kitty.setVisible(false);
        kitty.setPosition(-kitty.getWidth(), 0);

        chicha = new Chicha(0, 0, mainStage);
        calculateAndSetSize(chicha, 516f, 600f, screenHeight * CHARACTER_SCALE_PERCENTAGE);
        chicha.setVisible(false);
        chicha.setPosition(-chicha.getWidth(), 0);

        // --- DialogBox Setup ---
        dialogBox = new DialogBox(0, 0, uiStage);
        dialogBox.setDialogSize(screenWidth * 0.7f, 180);
        dialogBox.setPosition((screenWidth - dialogBox.getWidth()) / 2, 0);
        dialogBox.setBackgroundColor(new Color(0.1f, 0.1f, 0.1f, 0.85f));
        dialogBox.setVisible(false);

        // --- Continue Key Setup ---
        continueKey = null;
        try {
            continueKey = new BaseActor(0, 0, uiStage);
            continueKey.loadTexture("key-C.png");
            continueKey.setSize(32, 32);
            continueKey.setPosition(dialogBox.getWidth() - continueKey.getWidth() - 10, 10);
            dialogBox.addActor(continueKey);
            continueKey.setVisible(false);
        } catch (Exception e) {
            System.err.println("Error loading key-C.png or creating continueKey: " + e.getMessage());
            if (continueKey != null) {
                continueKey.setVisible(false);
            }
        }

        // --- Button Table Setup ---
        buttonTable = new Table();
        buttonTable.setVisible(false);

        // --- Create Auto-Play Status Label ---
        autoPlayStatusLabel = null; // Initialize as null
        if (BaseGame.labelStyle != null) {
            autoPlayStatusLabel = new Label("Auto: OFF", BaseGame.labelStyle);
            autoPlayStatusLabel.setColor(Color.RED); // --- เริ่มต้นเป็นสีแดง ---
            autoPlayStatusLabel.setVisible(true);    // --- ทำให้มองเห็นเลย ---
        } else {
            System.err.println("Error: BaseGame.labelStyle is null. Cannot create Auto-Play status label.");
        }

        // --- UI Table Layout ---
        uiTable.clearChildren();

        // Row 1: Status Label (Top Right)
        if (autoPlayStatusLabel != null) {
            uiTable.add(autoPlayStatusLabel).expandX().top().right().pad(10); // Use expandX() to push right
        } else {
            uiTable.add(); // Add empty cell if label failed
        }
        uiTable.row(); // Move to next row

        // Row 2: Spacer to push ButtonTable down
        uiTable.add().expandY();
        uiTable.row();

        // Row 3: Button Table (Centered)
        uiTable.add(buttonTable).center().padBottom(dialogBox.getHeight() + 20);
        uiTable.row();

        // Row 4: Dialog Box (Bottom)
        uiTable.add(dialogBox).expandX().fillX().height(dialogBox.getHeight()).bottom();

        // --- Scene Setup ---
        scene = new Scene();
        mainStage.addActor(scene);

        // --- Start Story ---
        wakeFromDream();
    }

    // --- Helper Methods ---

    private void calculateAndSetSize(BaseActor actor, float originalWidth, float originalHeight, float desiredHeight) {
        if (originalHeight <= 0 || originalWidth <= 0) {
            System.err.println("Error: Invalid original dimensions for " + actor.getClass().getSimpleName() + ". Using default size.");
            actor.setSize(100, 100);
            return;
        }
        float aspectRatio = originalWidth / originalHeight;
        float desiredWidth = desiredHeight * aspectRatio;
        actor.setSize(desiredWidth, desiredHeight);
        // System.out.println("Set " + actor.getClass().getSimpleName() + " size to: " + desiredWidth + "x" + desiredHeight); // Optional Log
    }

    public ArrayList<String[]> textFileReader(String fileName) {
        ArrayList<String[]> dialogueLines = new ArrayList<>();
        String pathExtension = "";
        String userDir = System.getProperty("user.dir");

        if (userDir.contains("\\")) {
            pathExtension = "Story_Text_Files\\";
        } else if (userDir.contains("/")) {
            pathExtension = "Story_Text_Files/";
        } else {
            pathExtension = "Story_Text_Files/"; // Fallback
        }

        Pattern pattern = Pattern.compile("([^\\[:]*?)(?:\\[([^\\]]+)\\])?:(.*)");

        try (FileReader fr = new FileReader(pathExtension + fileName);
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Matcher matcher = pattern.matcher(line);
                String characterName = null;
                String emotion = "Normal";
                String dialogueText = line;

                if (matcher.matches()) {
                    characterName = matcher.group(1).trim();
                    if (characterName.isEmpty()) {
                        characterName = null;
                    }
                    if (matcher.group(2) != null) {
                        emotion = matcher.group(2).trim();
                    } else if (characterName == null) {
                        emotion = null; // Narrator has no default emotion
                    }
                    dialogueText = matcher.group(3).trim();
                } else if (line.startsWith(":")) {
                    characterName = null;
                    emotion = null;
                    dialogueText = line.substring(1).trim();
                } else { // Treat as narration if no colon or brackets
                    characterName = null;
                    emotion = null;
                    dialogueText = line;
                }

                if (characterName != null && characterName.equalsIgnoreCase("Narrator")) {
                    characterName = null;
                    emotion = null;
                }
                dialogueLines.add(new String[]{characterName, emotion, dialogueText});
            }
        } catch (IOException ioe) {
            System.err.println("Error reading story file: " + pathExtension + fileName);
            ioe.printStackTrace();
            dialogueLines.add(new String[]{null, null, "[Error reading file: " + fileName + "]"});
        }
        return dialogueLines;
    }

    private Animation<TextureRegion> getCharacterAnimation(BaseActor character, String emotion) {
        if (emotion == null || emotion.trim().isEmpty()) {
            emotion = "Normal";
        }

        if (character instanceof Toei) {
            Toei c = (Toei) character;
            if (emotion.equalsIgnoreCase("Smile") || emotion.equalsIgnoreCase("Act")) return c.smile;
            else if (emotion.equalsIgnoreCase("Panic")) return c.panic;
            else if (emotion.equalsIgnoreCase("Doubt")) return c.doubt;
            else if (emotion.equalsIgnoreCase("Speak") || emotion.equalsIgnoreCase("Shadow")) return c.shadow;
            else if (emotion.equalsIgnoreCase("Normal")) return c.normal;
            else { System.out.println("Warning: Emotion '" + emotion + "' not recognized for Toei, using Normal."); return c.normal; }
        } else if (character instanceof Guy) {
            Guy c = (Guy) character;
            // if (!emotion.equalsIgnoreCase("Normal")) { System.out.println("Warning: Emotion '" + emotion + "' specified for Guy, using Normal."); }
            return c.normal;
        } else if (character instanceof Kitty) {
            Kitty c = (Kitty) character;
            if (emotion.equalsIgnoreCase("Smile")) return c.smile;
            else if (emotion.equalsIgnoreCase("Blood")) return c.blood;
            else if (emotion.equalsIgnoreCase("Shadow")) return c.shadow;
            else if (emotion.equalsIgnoreCase("Normal")) return c.normal;
            else { System.out.println("Warning: Emotion '" + emotion + "' not recognized for Kitty, using Normal."); return c.normal; }
        } else if (character instanceof Chicha) {
            Chicha c = (Chicha) character;
            if (emotion.equalsIgnoreCase("Smile")) return c.smile;
            else if (emotion.equalsIgnoreCase("Big_Smile") || emotion.equalsIgnoreCase("BigSmile")) return c.bigSmile;
            else if (emotion.equalsIgnoreCase("Panic")) return c.panic;
            else if (emotion.equalsIgnoreCase("Normal")) return c.normal;
            else { System.out.println("Warning: Emotion '" + emotion + "' not recognized for Chicha, using Normal."); return c.normal; }
        }
        // Add other characters here...

        System.err.println("Error: Unknown character type in getCharacterAnimation: " + character.getClass().getName());
        return null;
    }

    public void readArray(ArrayList<String[]> dialogueLines) {
        if (dialogueLines == null || dialogueLines.isEmpty()) {
            System.out.println("Warning: Attempted to read an empty or null dialogue list.");
            return;
        }
        for (String[] lineData : dialogueLines) {
            if (lineData != null && lineData.length == 3) {
                addDialogueSegment(lineData[0], lineData[1], lineData[2]);
            } else {
                System.err.println("Error: Invalid dialogue line data encountered in readArray.");
                addDialogueSegment(null, null, "[Error: Invalid dialogue line data]");
            }
        }
    }

    public void addDialogueSegment(String characterName, String emotion, String text) {
        if (continueKey == null && isAutoPlayActive) {
            System.err.println("CRITICAL ERROR: continueKey is null, cannot proceed with Auto-Play logic. Disabling Auto-Play.");
            isAutoPlayActive = false;
            // Also update label if it exists
            if (autoPlayStatusLabel != null) {
                autoPlayStatusLabel.setText("Auto: OFF");
                autoPlayStatusLabel.setColor(Color.RED);
                autoPlayStatusLabel.setVisible(true);
            }
        }

        BaseActor targetCharacter = null;
        if (characterName != null) {
            if (characterName.equalsIgnoreCase("Toei")) targetCharacter = toei;
            else if (characterName.equalsIgnoreCase("Guy")) targetCharacter = guy;
            else if (characterName.equalsIgnoreCase("Kitty")) targetCharacter = kitty;
            else if (characterName.equalsIgnoreCase("Chicha")) targetCharacter = chicha;
                // Add other characters here...
            else { System.out.println("Warning: Character '" + characterName + "' not recognized. Treating as narration."); }
        }

        // Hide previous character instantly
        if (currentlySpeaking != null && currentlySpeaking != targetCharacter) {
            final BaseActor charToHide = currentlySpeaking;
            scene.addSegment(new SceneSegment(charToHide, Actions.run(() -> charToHide.setVisible(false))));
        }

        // Show new character instantly
        if (targetCharacter != null) {
            final BaseActor charToShow = targetCharacter;
            final float targetX = (BaseActor.getWorldBounds().width - charToShow.getWidth()) / 2.4f;
            final float targetY = CHARACTER_Y_POSITION;
            scene.addSegment(new SceneSegment(charToShow, Actions.run(() -> {
                charToShow.setPosition(targetX, targetY);
                charToShow.setVisible(true);
            })));
            Animation<TextureRegion> anim = getCharacterAnimation(charToShow, emotion);
            if (anim != null) {
                scene.addSegment(new SceneSegment(charToShow, SceneActions.setAnimation(anim)));
            }
            currentlySpeaking = targetCharacter;
        } else { // Narration: Hide current speaker if any
            if (currentlySpeaking != null) {
                final BaseActor charToHide = currentlySpeaking;
                scene.addSegment(new SceneSegment(charToHide, Actions.run(() -> charToHide.setVisible(false))));
                currentlySpeaking = null;
            }
        }

        // Add segments for dialogue display
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(true)));
        scene.addSegment(new SceneSegment(dialogBox, SceneActions.typewriter(text)));
        // Show continue key only if it was created successfully
        if (continueKey != null) {
            scene.addSegment(new SceneSegment(continueKey, Actions.visible(true))); // Important signal for auto-play
        } else {
            System.err.println("Warning: continueKey is null in addDialogueSegment.");
        }
        scene.addSegment(new SceneSegment(background, SceneActions.pause())); // Wait for input or auto-play
        // Hide continue key only if it was created successfully
        if (continueKey != null) {
            scene.addSegment(new SceneSegment(continueKey, Actions.visible(false)));
        }
    }

    // ============================================================
    // ===== STORY SCENE DEFINITIONS (Formatted for Readability) ====
    // ============================================================

    public void wakeFromDream() {
        System.out.println("Starting wakeFromDream...");
        scene.clearSegments();
        currentlySpeaking = null;

        ArrayList<String[]> prelude0Lines = textFileReader("Pre0.txt");
        ArrayList<String[]> prelude1Lines = textFileReader("Pre1.txt");
        ArrayList<String[]> prelude1_5Lines = textFileReader("Pre1.5.txt");
        ArrayList<String[]> prelude2Lines = textFileReader("Pre2.txt");
        ArrayList<String[]> prelude3Lines = textFileReader("Pre3.txt");

        // Initial setup (Black Screen)
        background.setAnimation(null);
        try {
            background.setTexture(new Texture(Gdx.files.internal("Background/black.png")));
            background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            background.setColor(Color.WHITE);
            background.setOpacity(1);
        } catch (Exception e) {
            handleBackgroundLoadError("Background/black.png", e);
            return; // Exit if critical background fails
        }

        dialogBox.setVisible(false);
        if(toei != null) toei.setVisible(false);
        if(guy != null) guy.setVisible(false);
        if(kitty != null) kitty.setVisible(false);
        if(chicha != null) chicha.setVisible(false);

        // --- Build Scene ---
        readArray(prelude0Lines);

        // Transition 1
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(false)));
        scene.addSegment(new SceneSegment(background, Actions.fadeOut(1f)));
        scene.addSegment(new SceneSegment(background, Actions.run(() -> { /* Still black */ })));
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        readArray(prelude1Lines);

        // Transition 1.5
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(false)));
        scene.addSegment(new SceneSegment(background, Actions.fadeOut(1f)));
        scene.addSegment(new SceneSegment(background, Actions.run(() -> { /* Still black */ })));
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        readArray(prelude1_5Lines);

        // Transition 2
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(false)));
        scene.addSegment(new SceneSegment(background, Actions.fadeOut(1f)));
        scene.addSegment(new SceneSegment(background, Actions.run(() -> {
            try {
                background.setTexture(new Texture(Gdx.files.internal("Background/Temple.png")));
                background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            } catch (Exception e) {
                handleBackgroundLoadError("Background/Temple.png", e);
            }
        })));
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        readArray(prelude2Lines);

        // Transition 3
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(false)));
        scene.addSegment(new SceneSegment(background, Actions.fadeOut(1f)));
        scene.addSegment(new SceneSegment(background, Actions.run(() -> {
            try {
                background.setTexture(new Texture(Gdx.files.internal("Background/ChichaHouse.png")));
                background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            } catch (Exception e) {
                handleBackgroundLoadError("Background/ChichaHouse.png", e);
            }
        })));
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        readArray(prelude3Lines);

        // --- Prepare for Next Scene ---
        transitionToScene(this::Scene_2);

        scene.start();
    }

    public void Scene_2() {
        System.out.println("Starting Scene_2...");
        scene.clearSegments();
        currentlySpeaking = null;

        ArrayList<String[]> scene2Lines = textFileReader("Scene_2.txt");

        // Set background
        background.setAnimation(null);
        try {
            background.setTexture(new Texture(Gdx.files.internal("Background/First_Entry.png")));
            background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            background.setOpacity(0); // Start transparent
        } catch (Exception e) {
            handleBackgroundLoadError("Background/First_Entry.png", e);
        }

        if(toei != null) toei.setVisible(false);
        if(guy != null) guy.setVisible(false);
        if(kitty != null) kitty.setVisible(false);
        if(chicha != null) chicha.setVisible(false);
        dialogBox.setVisible(false);

        // Fade in background
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));

        // Show dialogue leading up to the choice
        readArray(scene2Lines);

        // --- Hide elements before showing choice buttons ---
        if (currentlySpeaking != null) {
            scene.addSegment(new SceneSegment(currentlySpeaking, Actions.run(() -> currentlySpeaking.setVisible(false))));
        }
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(false)));
        if(continueKey != null) {
            scene.addSegment(new SceneSegment(continueKey, Actions.visible(false)));
        }

        // --- Show Choice Buttons ---
        scene.addSegment(new SceneSegment(buttonTable, Actions.visible(true)));

        // --- Button 1: Go Straight ---
        TextButton getGoStraightButton = new TextButton("Go straight in", BaseGame.textButtonStyle);
        getGoStraightButton.clearListeners();
        getGoStraightButton.addListener((Event e) -> {
            if (!(e instanceof InputEvent) || !((InputEvent) e).getType().equals(Type.touchDown)) return false;
            if(BaseGame.clickSound != null) BaseGame.clickSound.play();
            System.out.println("Choice: Go Straight");
            scene.clearSegments(); // Clear the pause action
            scene.addSegment(new SceneSegment(buttonTable, Actions.visible(false)));
            scene.addSegment(new SceneSegment(background, Actions.fadeOut(1f)));
            scene.addSegment(new SceneSegment(background, Actions.run(this::lateForClassRunning)));
            scene.start(); // Start the new sequence
            return true;
        });

        // --- Button 2: Wait ---
        TextButton getWaitSunButton = new TextButton("Wait until the sun sets", BaseGame.textButtonStyle);
        getWaitSunButton.clearListeners();
        getWaitSunButton.addListener((Event e) -> {
            if (!(e instanceof InputEvent) || !((InputEvent) e).getType().equals(Type.touchDown)) return false;
            if(BaseGame.clickSound != null) BaseGame.clickSound.play();
            System.out.println("Choice: Wait");
            scene.clearSegments(); // Clear the pause action
            scene.addSegment(new SceneSegment(buttonTable, Actions.visible(false)));
            // Add dialogue after choice
            addDialogueSegment("Toei", "Normal", "There is no way im leaving the house without getting ready for the day.");
            addDialogueSegment("Toei", "Smile", "Im going to try to get ready as quickly as possible.");
            // Transition to next scene
            transitionToScene(this::getReadyFast); // Use helper
            scene.start(); // Start the new sequence
            return true;
        });

        // --- Add Buttons to Table ---
        buttonTable.clearChildren();
        buttonTable.add(getGoStraightButton).pad(10);
        buttonTable.row();
        buttonTable.add(getWaitSunButton).pad(10);

        // Pause waiting for button input
        scene.addSegment(new SceneSegment(background, SceneActions.pause()));

        scene.start(); // Start the scene up to the pause
    }

    // --- Helper method for standard scene setup ---
    private void setupScene(String backgroundPath, String textFileName) {
        System.out.println("Setting up scene with background: " + backgroundPath + " and text: " + textFileName);
        scene.clearSegments();
        currentlySpeaking = null;

        ArrayList<String[]> lines = textFileReader(textFileName);

        // Set background
        background.setAnimation(null);
        try {
            background.setTexture(new Texture(Gdx.files.internal(backgroundPath)));
            background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            background.setOpacity(0); // Start transparent
        } catch (Exception e) {
            handleBackgroundLoadError(backgroundPath, e);
            // Add error dialogue even if background fails, uses fallback black texture
            addDialogueSegment(null, null, "[Error: Failed to load background '" + backgroundPath + "']");
        }

        // Hide characters initially
        if(toei != null) toei.setVisible(false);
        if(guy != null) guy.setVisible(false);
        if(kitty != null) kitty.setVisible(false);
        if(chicha != null) chicha.setVisible(false);
        dialogBox.setVisible(false);

        // Add segments: fade in background, then show dialogue
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        readArray(lines); // Add dialogue segments
    }

    // --- Helper method for standard scene transition ---
    private void transitionToScene(Runnable nextSceneMethod) {
        // Hide last speaker & dialog & continue key
        if (currentlySpeaking != null) {
            scene.addSegment(new SceneSegment(currentlySpeaking, Actions.run(() -> currentlySpeaking.setVisible(false))));
            currentlySpeaking = null; // Reset speaker for the next scene
        }
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(false)));
        if(continueKey != null) {
            scene.addSegment(new SceneSegment(continueKey, Actions.visible(false)));
        }

        // Fade out background and run next method
        scene.addSegment(new SceneSegment(background, Actions.fadeOut(1f)));
        scene.addSegment(new SceneSegment(background, Actions.run(nextSceneMethod))); // Use the provided method reference
        // scene.start() will be called by the calling method (e.g., at the end of setupScene + transition)
    }

    // --- Refactored Story Methods using Helpers ---

    public void lateForClassRunning() {
        setupScene("Background/Temple.png", "lateForClassRunning.txt");
        transitionToScene(this::arriveToSchool);
        scene.start(); // Start the scene actions
    }

    public void getReadyFast() {
        setupScene("Background/First_Entry.png", "getReadyFast.txt");
        transitionToScene(this::runToTheKitchen);
        scene.start(); // Start the scene actions
    }

    public void runToTheKitchen() {
        System.out.println("Starting runToTheKitchen...");
        // Needs special handling because it loads two text files
        scene.clearSegments();
        currentlySpeaking = null;

        ArrayList<String[]> lines1 = textFileReader("runToTheKitchen.txt");
        ArrayList<String[]> lines2 = textFileReader("runToTheKitchenpt2.txt");

        // Set background
        background.setAnimation(null);
        try {
            background.setTexture(new Texture(Gdx.files.internal("Background/First_Entry.png")));
            background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            background.setOpacity(0);
        } catch (Exception e) {
            handleBackgroundLoadError("Background/First_Entry.png", e);
        }

        if(toei != null) toei.setVisible(false);
        if(guy != null) guy.setVisible(false);
        if(kitty != null) kitty.setVisible(false);
        if(chicha != null) chicha.setVisible(false);
        dialogBox.setVisible(false);

        // Add segments
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        readArray(lines1); // Add first part of dialogue
        readArray(lines2); // Add second part of dialogue

        // Transition
        transitionToScene(this::lateForClassGetARide);
        scene.start(); // Start the scene actions
    }


    public void lateForClassGetARide() {
        setupScene("Background/First_Entry.png", "lateForClassGetARide.txt");
        transitionToScene(this::arriveToSchool);
        scene.start(); // Start the scene actions
    }

    public void arriveToSchool() {
        setupScene("Background/First_Entry.png", "arriveToSchool.txt");
        transitionToScene(this::goToClass);
        scene.start(); // Start the scene actions
    }

    public void goToClass() {
        System.out.println("Starting goToClass...");
        scene.clearSegments();
        currentlySpeaking = null;

        // Set background
        background.setAnimation(null);
        try {
            background.setTexture(new Texture(Gdx.files.internal("Background/First_Entry.png")));
            background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            background.setOpacity(0);
        } catch (Exception e) {
            handleBackgroundLoadError("Background/First_Entry.png", e);
        }

        if(toei != null) toei.setVisible(false);
        if(guy != null) guy.setVisible(false);
        if(kitty != null) kitty.setVisible(false);
        if(chicha != null) chicha.setVisible(false);
        dialogBox.setVisible(false);

        // Add segments
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        addDialogueSegment(null, null, "Right as I get into class I realize that nobody was here except the professor");

        // Transition
        transitionToScene(this::theEnd);
        scene.start(); // Start the scene actions
    }

    public void theEnd() {
        System.out.println("Starting theEnd...");
        scene.clearSegments();
        currentlySpeaking = null;

        // Set background to black
        background.setAnimation(null);
        try {
            background.setTexture(new Texture(Gdx.files.internal("Background/black.png")));
            background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
            background.setOpacity(0);
        } catch (Exception e) {
            handleBackgroundLoadError("Background/black.png", e);
        }

        if(toei != null) toei.setVisible(false);
        if(guy != null) guy.setVisible(false);
        if(kitty != null) kitty.setVisible(false);
        if(chicha != null) chicha.setVisible(false);
        dialogBox.setVisible(false);

        // Add segments
        scene.addSegment(new SceneSegment(background, Actions.fadeIn(1f)));
        addDialogueSegment(null, null, "Thank you for playing.");

        // Hide dialog after message, fade out, and go to menu
        scene.addSegment(new SceneSegment(dialogBox, Actions.visible(false)));
        if(continueKey != null) {
            scene.addSegment(new SceneSegment(continueKey, Actions.visible(false)));
        }
        scene.addSegment(new SceneSegment(background, Actions.fadeOut(1f)));
        scene.addSegment(new SceneSegment(background, Actions.run(() -> setActiveScreen(new MenuScreen())))); // Go to menu

        scene.start(); // Start the final actions
    }

    // --- Helper for Background Loading Errors ---
    private void handleBackgroundLoadError(String path, Exception e) {
        System.err.println("CRITICAL Error loading background: " + path + " - " + e.getMessage());
        // Set to black as fallback IN CASE the background object itself exists
        if (background != null) {
            try {
                background.setTexture(new Texture(Gdx.files.internal("Background/black.png")));
                background.setSize(BaseActor.getWorldBounds().width, BaseActor.getWorldBounds().height);
                background.setOpacity(1); // Make it visible immediately
            } catch (Exception fallbackErr) {
                System.err.println("CRITICAL Error loading fallback background! - " + fallbackErr.getMessage());
                // At this point, the game might be unplayable visually.
            }
        }
    }


    // --- Input Handling & Update Loop ---
    @Override
    public void update(float dt) {
        // Basic check if essential objects are ready
        if (scene == null || dialogBox == null || buttonTable == null) {
            return;
        }

        // --- Auto-Play Logic (Wait 2s after text finishes) ---
        // Condition to *start* the delay timer:
        boolean canStartDelay = isAutoPlayActive &&
                !buttonTable.isVisible() &&
                !scene.isSceneFinished() &&
                dialogBox.isVisible() &&
                continueKey != null && continueKey.isVisible() && // Check if C key is visible (text done)
                !waitingForAutoPlayDelay;

        if (canStartDelay) {
            waitingForAutoPlayDelay = true;
            autoPlayTimer = 0f;
            // System.out.println("Auto-Play: Text finished, starting " + AUTO_PLAY_DELAY + "s delay."); // Optional Log
        }

        // If currently waiting for the delay:
        if (waitingForAutoPlayDelay) {
            autoPlayTimer += dt;
            if (autoPlayTimer >= AUTO_PLAY_DELAY) {
                // Double-check conditions before auto-advancing
                boolean canAutoAdvance = isAutoPlayActive &&
                        !buttonTable.isVisible() &&
                        scene != null && !scene.isSceneFinished() &&
                        dialogBox.isVisible() &&
                        continueKey != null && continueKey.isVisible();

                if (canAutoAdvance && !scene.isLastSegment()) {
                    if (BaseGame.clickSound != null) {
                        BaseGame.clickSound.play();
                    }
                    scene.loadNextSegment(); // Load next segment (which hides continueKey)
                    // System.out.println("Auto-Play: Delay finished, scene advanced."); // Optional Log
                }
                // Reset waiting state regardless of whether it advanced
                waitingForAutoPlayDelay = false;
                autoPlayTimer = 0f;
            }
        }

        // --- Reset waiting state if conditions change ---
        // (e.g., player presses C, toggles auto, choice appears)
        if (waitingForAutoPlayDelay && (!isAutoPlayActive || (continueKey != null && !continueKey.isVisible()) || buttonTable.isVisible())) {
            waitingForAutoPlayDelay = false;
            autoPlayTimer = 0f;
            // System.out.println("Auto-Play: Delay interrupted or conditions changed."); // Optional Log
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        // Basic check if essential objects are ready
        if (scene == null || buttonTable == null) {
            return false;
        }

        // --- Manual 'C' Press ---
        if (keyCode == Keys.C && !buttonTable.isVisible() && !scene.isSceneFinished()) {
            // If 'C' is pressed while waiting for input (text finished and C key visible)
            if (continueKey != null && continueKey.isVisible()) {
                waitingForAutoPlayDelay = false; // Interrupt auto-play delay
                autoPlayTimer = 0f;

                if (!scene.isLastSegment()) {
                    if (BaseGame.clickSound != null) {
                        BaseGame.clickSound.play();
                    }
                    scene.loadNextSegment(); // Load next
                    return true; // Handled
                }
            }
            // If 'C' is pressed while typewriter is active
            else if (!scene.isSegmentFinished()) {
                scene.finishCurrentSegment(); // Finish typewriter
                // Don't reset auto-play timer here, let the update loop handle starting the delay
                return true; // Handled
            }
        }

        // --- Toggle Auto-Play 'A' Key ---
        if (keyCode == Keys.A) {
            isAutoPlayActive = !isAutoPlayActive;
            // Reset waiting state and timer when toggling
            waitingForAutoPlayDelay = false;
            autoPlayTimer = 0f;

            // Update status label
            if (autoPlayStatusLabel != null) {
                if (isAutoPlayActive) {
                    autoPlayStatusLabel.setText("Auto: ON");
                    autoPlayStatusLabel.setColor(Color.LIME); // Green for ON
                    autoPlayStatusLabel.setVisible(true); // Ensure visible
                    System.out.println("Auto-Play: ON");
                } else {
                    autoPlayStatusLabel.setText("Auto: OFF");
                    autoPlayStatusLabel.setColor(Color.RED); // Red for OFF
                    autoPlayStatusLabel.setVisible(true); // Keep visible but red
                    System.out.println("Auto-Play: OFF");
                }
            } else { // Fallback console message if label failed
                if (isAutoPlayActive) System.out.println("Auto-Play: ON (Label N/A)");
                else System.out.println("Auto-Play: OFF (Label N/A)");
            }
            return true; // Handled
        }

        // --- ESC Key ---
        if (keyCode == Keys.ESCAPE) {
            System.out.println("ESC key pressed - returning to Menu.");
            // Consider adding a confirmation dialogue later
            setActiveScreen(new MenuScreen());
            return true; // Handled
        }

        return false; // Event not handled by this screen
    }

} // End of class StoryScreen