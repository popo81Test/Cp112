package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Guy extends BaseActor {

    // --- ประกาศ Animation ---
    // เนื่องจากมีไฟล์เดียว จึงอาจมีแค่ state เดียวคือ normal
    public Animation<TextureRegion> normal;

    public Guy(float x, float y, Stage s) {
        super(x, y, s);

        // --- โหลดไฟล์ภาพ ---
        // ตรวจสอบให้แน่ใจว่า Path ถูกต้อง (อิงจากรูปคือ assets/Guy/Guy.png)
        String normalPath = "Guy/Guy.png";

        normal = loadTexture(normalPath);

        // --- จัดการ Error และตั้งค่าเริ่มต้น ---
        if (normal == null) {
            handleLoadError(normalPath);
            // ถ้าโหลดไม่ได้ ให้ใช้ placeholder
            Texture errorTex = new Texture(Gdx.files.internal("badlogic.jpg")); // Placeholder
            normal = new Animation<>(1f, new TextureRegion(errorTex));
        }

        // ตั้งค่า Animation เริ่มต้น
        setAnimation(normal);
    }

    // Helper method to report loading errors
    private void handleLoadError(String path) {
        System.err.println("Error loading Guy texture: " + path + ". Make sure the file exists in the correct assets path.");
    }
}