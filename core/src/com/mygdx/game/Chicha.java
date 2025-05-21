package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Chicha extends BaseActor {

    // --- ประกาศ Animation สำหรับแต่ละอารมณ์ ---
    public Animation<TextureRegion> normal;
    public Animation<TextureRegion> smile;
    public Animation<TextureRegion> bigSmile; // ใช้ camelCase สำหรับชื่อตัวแปร
    public Animation<TextureRegion> panic;

    public Chicha(float x, float y, Stage s) {
        super(x, y, s);

        // --- โหลดไฟล์ภาพ ---
        // ตรวจสอบให้แน่ใจว่า Path ถูกต้อง (อิงจากรูปคือ assets/Chicha/...)
        String normalPath = "Chicha/Chicha_Normal.png";
        String smilePath = "Chicha/Chicha_Smile.png";
        String bigSmilePath = "Chicha/Chicha_Big_Smile.png";
        String panicPath = "Chicha/Chicha_Panic.png";

        normal = loadTexture(normalPath);
        smile = loadTexture(smilePath);
        bigSmile = loadTexture(bigSmilePath);
        panic = loadTexture(panicPath);

        // --- จัดการ Error และตั้งค่าเริ่มต้น ---
        if (normal == null) {
            handleLoadError(normalPath);
            // ใช้ placeholder ถ้าโหลดภาพ normal ไม่ได้
            Texture errorTex = new Texture(Gdx.files.internal("badlogic.jpg"));
            normal = new Animation<>(1f, new TextureRegion(errorTex));
        }
        if (smile == null) handleLoadError(smilePath);
        if (bigSmile == null) handleLoadError(bigSmilePath);
        if (panic == null) handleLoadError(panicPath);


        // ตั้งค่า Animation เริ่มต้น
        setAnimation(normal);
    }

    // Helper method to report loading errors
    private void handleLoadError(String path) {
        System.err.println("Error loading Chicha texture: " + path + ". Make sure the file exists in the correct assets path.");
    }
}
