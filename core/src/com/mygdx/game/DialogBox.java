package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

/**
 *  This is directly from the book Java Game Development with LibGDX written by Lee Stemkoski
 */

/***
 * This class stores a background image and label and has a set of methods that can be used to configure the appearance.
 */

public class DialogBox extends BaseActor
{
    private Label dialogLabel;
    private float padding = 16;

    public DialogBox(float x, float y, Stage s)
    {
        super(x,y,s);
        loadTexture("DialogBox.png");

        dialogLabel = new Label(" ", BaseGame.labelStyle);
        dialogLabel.setWrap(true);
        dialogLabel.setAlignment( Align.topLeft );
        dialogLabel.setPosition( padding, padding );
        this.setDialogSize( getWidth(), getHeight() );
        this.addActor(dialogLabel);
    }

    public void setDialogSize(float width, float height)
    {
        this.setSize(width, height);
        dialogLabel.setWidth( width - 2 * padding );
        dialogLabel.setHeight( height - 2 * padding );
    }

    // This is the main setter for the dialog that is used inside of the visual novel.
    public void setText(String text)
    {  dialogLabel.setText(text);  }

    public void setFontScale(float scale)
    {  dialogLabel.setFontScale(scale);  }

    public void setFontColor(Color color)
    {  dialogLabel.setColor(color);  }

    public void setBackgroundColor(Color color)
    {  this.setColor(color);  }
    
    public void alignTopLeft()
    {  dialogLabel.setAlignment( Align.topLeft );  }
    
    public void alignCenter()
    {  dialogLabel.setAlignment( Align.center );  }
}