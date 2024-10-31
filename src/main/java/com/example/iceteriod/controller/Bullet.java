package com.example.iceteriod.controller;

import com.example.iceteriod.view.SpriteAnimation;
import javafx.animation.Animation;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class Bullet extends GameCharacter {

    private SpriteAnimation spriteAnimation;

    public Bullet(EntityType entityType, double x, double y) {
        super(entityType, 7, x, y);
        this.setRadius(2);

        // ตั้งค่าภาพกระสุน (สไปรต์ชีต)
        Image spriteSheet = new Image(getClass().getResourceAsStream("/sprites/bullet_sprite.png"));
        this.getImageView().setImage(spriteSheet);

        // กำหนดขนาดของแต่ละเฟรมในสไปรต์ชีต
        int frameWidth = 108;  // ความกว้างของแต่ละเฟรม
        int frameHeight = 67;  // ความสูงของแต่ละเฟรม

        // ตั้งค่า Viewport และคำนวณจุดศูนย์กลางของ Viewport เพื่อให้กระสุนออกจากด้านหน้าของยาน
        this.getImageView().setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));
        this.getImageView().setFitWidth(frameWidth);
        this.getImageView().setFitHeight(frameHeight);
        this.getImageView().setX(-frameWidth / 2.0);  // ตั้งค่า X ให้ตรงกับจุดกึ่งกลาง
        this.getImageView().setY(-frameHeight / 2.0); // ตั้งค่า Y ให้ตรงกับจุดกึ่งกลาง

        // สร้างและตั้งค่า SpriteAnimation
        spriteAnimation = new SpriteAnimation(
                this.getImageView(),
                Duration.millis(1200), // ระยะเวลาของแอนิเมชันหนึ่งรอบ
                16, 4, // จำนวนเฟรมทั้งหมดและจำนวนคอลัมน์
                0, 0,
                frameWidth, frameHeight
        );
        spriteAnimation.setCycleCount(Animation.INDEFINITE);
        spriteAnimation.play();
    }

    @Override
    public void applyAcceleration(double acc) {
        super.applyAcceleration(acc);
    }

    @Override
    public void applyMove(int screenWidth, int screenHeight) {
        super.applyMove(screenWidth, screenHeight);
    }

    // เพิ่มเมธอดเพื่อหยุดแอนิเมชันเมื่อกระสุนถูกลบ
    public void stopAnimation() {
        if (spriteAnimation != null) {
            spriteAnimation.stop();
        }
    }


    public void rotLeft() {
        super.rotLeft();
    }

    public void rotRight() {
        super.rotRight();
    }


}
