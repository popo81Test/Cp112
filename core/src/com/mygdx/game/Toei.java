package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Toei extends BaseActor {

    // --- ประกาศ Animation สำหรับแต่ละอารมณ์ ---
    public Animation<TextureRegion> normal;
    //public Animation<TextureRegion> act; // Corresponds to Toei_Act.jpg
    public Animation<TextureRegion> smile;
    public Animation<TextureRegion> panic;
    public Animation<TextureRegion> doubt;
    public Animation<TextureRegion> shadow;
    // เพิ่ม Animation อื่นๆ ที่ต้องการจาก list ได้ตามต้องการ
    // public Animation<TextureRegion> book;
    // public Animation<TextureRegion> dtt_normal; // ตัวอย่างถ้าต้องการแยก DTT

    public Toei(float x, float y, Stage s) {
        super(x, y, s);

        // --- โหลดไฟล์ภาพสำหรับแต่ละอารมณ์ ---
        // *** ตรวจสอบให้แน่ใจว่า Path และชื่อไฟล์ถูกต้อง และไฟล์อยู่ใน assets ***
        // *** หากไฟล์อยู่ในโฟลเดอร์ย่อย ต้องระบุ path ด้วย เช่น "characters/toei/Toei_Normal.png" ***
        String normalPath = "Toei/DTT_Normal.png";
        //String actPath = "Toei/Toei_Act.jpg"; // ใช้ Toei_Act.jpg สำหรับ Grin
        String smilePath = "Toei/DTT_Smile.png";
        String panicPath = "Toei/DTT_SoPanic.png"; // ใช้ DTT_Doubt.png
        String doubtPath = "Toei/DTT_Doubt.png";
        String shadowPath = "Toei/DTT_Shadow.png";
        // String bookPath = "Toei_Book.jpg";
        // String dttNormalPath = "DTT_Normal.png";


        normal = loadTexture(normalPath);
        //act = loadTexture(actPath);
        smile = loadTexture(smilePath);
        panic = loadTexture(panicPath);
        doubt = loadTexture(doubtPath);
        shadow = loadTexture(shadowPath);
        // book = loadTexture(bookPath);
        // dtt_normal = loadTexture(dttNormalPath);


        // --- จัดการ Error และตั้งค่าเริ่มต้น (สำคัญ!) ---
        if (normal == null) {
            handleLoadError(normalPath);
            // ถ้า normal โหลดไม่ได้ อาจจะใช้ grin หรือภาพอื่นแทน หรือใช้ placeholder
            if(panic != null) normal = smile; // สำรอง
            else { // ถ้า grin ก็ไม่ได้ ให้ใช้ placeholder
                Texture errorTex = new Texture(Gdx.files.internal("badlogic.jpg")); // Placeholder
                normal = new Animation<>(1f, new TextureRegion(errorTex));
            }
        }
        //if (act == null) handleLoadError(actPath);
        if (smile == null) handleLoadError(smilePath);
        if (panic == null) handleLoadError(panicPath);
        if (doubt == null) handleLoadError(doubtPath);
        if (shadow == null) handleLoadError(shadowPath);
        // if (book == null) handleLoadError(bookPath);
        // if (dtt_normal == null) handleLoadError(dttNormalPath);


        // ตั้งค่า Animation เริ่มต้น
        setAnimation(normal);
    }

    // Helper method to report loading errors
    private void handleLoadError(String path) {
        System.err.println("Error loading Toei texture: " + path + ". Make sure the file exists in the correct assets path.");
    }
}