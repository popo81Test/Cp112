package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
/**
 *  This is directly from the book Java Game Development with LibGDX written by Lee Stemkoski
 */


/***
 * This class serves as one of the 4 main parts involved with creating a game. This Class specifically
 * focuses on storing a reference point to the Game object initialized by the DesktopLauncher.
 */

/**
 *  Created when program is launched;
 *  manages the screens that appear during the game.
 */
public abstract class BaseGame extends Game
{
    /**
     *  Stores reference to game; used when calling setActiveScreen method.
     */
    private static BaseGame game;

    public static LabelStyle labelStyle; // BitmapFont + Color
    public static TextButtonStyle textButtonStyle; // NPD + BitmapFont + Color
    public static Sound clickSound;
    /**
     *  Called when game is initialized; stores global reference to game object.
     */
    public BaseGame()
    {
        game = this;
    }

    /**
     *  Called when game is initialized,
     *  after Gdx.input and other objects have been initialized.
     */
    public void create()
    {
        // prepare for multiple classes/stages/actors to receive discrete input
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor( im );

        // parameters for generating a custom bitmap font
        FreeTypeFontGenerator fontGenerator =
            new FreeTypeFontGenerator(Gdx.files.internal("ComicSansMSRegular.ttf"));
        FreeTypeFontParameter fontParameters = new FreeTypeFontParameter();
        fontParameters.size = 24;
        fontParameters.color = Color.WHITE;
        fontParameters.borderWidth = 2;
        fontParameters.borderColor = Color.BLACK;
        fontParameters.borderStraight = true;
        fontParameters.minFilter = TextureFilter.Linear;
        fontParameters.magFilter = TextureFilter.Linear;

        BitmapFont customFont = fontGenerator.generateFont(fontParameters);

        labelStyle = new LabelStyle();
        labelStyle.font = customFont;

        textButtonStyle = new TextButtonStyle();

        Texture   buttonTex   = new Texture( Gdx.files.internal("Button.png") );
        NinePatch buttonPatch = new NinePatch(buttonTex, 24,24,24,24);
        textButtonStyle.up    = new NinePatchDrawable( buttonPatch );
        textButtonStyle.font      = customFont;
        textButtonStyle.fontColor = Color.GRAY;

        clickSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/Click.wav"));
        fontGenerator.dispose();
    }

    /**
     *  Used to switch screens while game is running.
     *  Method is static to simplify usage.
     */
    public static void setActiveScreen(BaseScreen s)
    {
        game.setScreen(s);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (clickSound != null) {
            clickSound.dispose(); // อย่าลืม Dispose เสียงเมื่อเลิกใช้งาน
        }
    }
}