package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

// Made by Darren


/**
 * This class is designed to hold multiple different backgrounds for the visual novel.
 *
 *
 * */

public class Background extends BaseActor {

    private Texture texture;
    private Animation<TextureRegion> animation;
    private float elapsedTime;

    /**
     * The Animation declaration is a imported library from the libGDX framework that stores a list of objects
     * representing a animated sequence. Sort of like a Key frame. This is going to simplify the transition between each
     * each background for the visual novel.
     */

    // Each parameter is used for a different background inside of the visual novel.
    public Animation First_Scene; // เปลี่ยนชื่อให้สื่อว่าเป็น Animation
    public Animation lateForClassHouseAnim;
    public Animation lateForClassRunningAnim;
    public Animation getReadyFastAnim;
    public Animation runToTheKitchenAnim;
    public Animation lateForClassGetARideAnim;
    public Animation arriveToSchoolAnim;
    public Animation goToClassAnim;
    public Animation theEndAnim;

    // This is a background constructor that the statement to inherit the constructor from the superclass BaseActor.
    public Background(float x, float y, Stage s) {
        super(x, y, s);
        elapsedTime = 0;

        // These variables are being stored into a string list in the loadTexture method inside of the BaseActor
        // class that we are extending from.
        First_Scene = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        lateForClassHouseAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        lateForClassRunningAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        getReadyFastAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        runToTheKitchenAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        lateForClassGetARideAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        arriveToSchoolAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        goToClassAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        theEndAnim = loadTexture("Background/First_Entry.png"); // สมมติว่าเป็น Animation
        setSize(1000, 600);
    }

    public void setTexture(Texture t) {
        texture = t;
        animation = null;
        setSize(texture.getWidth(), texture.getHeight());
    }

    public void setAnimation(Animation<TextureRegion> anim) {
        animation = anim;
        if (animation != null) {
            TextureRegion tr = animation.getKeyFrame(0);
            setSize(tr.getRegionWidth(), tr.getRegionHeight());
        }
        texture = null;
    }

    @Override
    public void act(float dt) {
        super.act(dt);
        elapsedTime += dt;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a);
        if (animation != null) {
            TextureRegion tr = animation.getKeyFrame(elapsedTime, true);
            batch.draw(tr, getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }
        if (texture != null) {
            TextureRegion tr = new TextureRegion(texture); // สร้าง TextureRegion จาก Texture
            batch.draw(tr, getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }
    }
}