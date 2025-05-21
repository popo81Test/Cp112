// ===============================================================
// ===== 1. โค้ดสำหรับไฟล์: core/src/com/mygdx/game/Kitty.java =====
// ===============================================================
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Kitty extends BaseActor {

    // --- ประกาศ Animation สำหรับแต่ละอารมณ์/สถานะใหม่ ---
    public Animation<TextureRegion> normal;
    public Animation<TextureRegion> smile;
    public Animation<TextureRegion> blood;
    public Animation<TextureRegion> shadow;

    public Kitty(float x, float y, Stage s) {
        super(x, y, s);

        // --- โหลดไฟล์ภาพ ---
        // ตรวจสอบให้แน่ใจว่า Path ถูกต้อง (อิงจากรูปคือ assets/Kitty/...)
        String normalPath = "Kitty/Kitty_Normal.png";
        String smilePath = "Kitty/Kitty_Smile.png";
        String bloodPath = "Kitty/Kitty_Blood.png";
        String shadowPath = "Kitty/Kitty_Shadow.png";

        normal = loadTexture(normalPath);
        smile = loadTexture(smilePath);
        blood = loadTexture(bloodPath);
        shadow = loadTexture(shadowPath);

        // --- จัดการ Error และตั้งค่าเริ่มต้น ---
        if (normal == null) {
            handleLoadError(normalPath);
            // ใช้ placeholder ถ้าโหลดภาพ normal ไม่ได้
            Texture errorTex = new Texture(Gdx.files.internal("badlogic.jpg"));
            normal = new Animation<>(1f, new TextureRegion(errorTex));
        }
        if (smile == null) handleLoadError(smilePath);
        if (blood == null) handleLoadError(bloodPath);
        if (shadow == null) handleLoadError(shadowPath);

        // ตั้งค่า Animation เริ่มต้น
        setAnimation(normal);
    }

    // Helper method to report loading errors
    private void handleLoadError(String path) {
        System.err.println("Error loading Kitty texture: " + path + ". Make sure the file exists in the correct assets path.");
    }
}