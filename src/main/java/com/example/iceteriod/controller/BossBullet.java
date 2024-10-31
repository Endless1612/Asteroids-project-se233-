package com.example.iceteriod.controller;

import javafx.scene.image.Image;

public class BossBullet extends GameCharacter {
    private double bulletWidth;
    private double bulletHeight;

    // ตัวสร้างที่มีพารามิเตอร์สำหรับปรับขนาด
    public BossBullet(EntityType entityType, double x, double y, double bulletWidth, double bulletHeight) {
        super(entityType, 1.0, x, y);
        this.bulletWidth = bulletWidth;
        this.bulletHeight = bulletHeight;
        this.setRadius(Math.max(bulletWidth, bulletHeight) / 2); // ปรับรัศมีให้เหมาะสมกับขนาดกระสุนใหม่

        // ตั้งค่าภาพสำหรับกระสุนบอส
        this.getImageView().setImage(new Image(getClass().getResourceAsStream("/sprites/boss_bullet.png")));

        // ปรับขนาดของกระสุนบอสตามพารามิเตอร์ที่ส่งมา
        this.getImageView().setFitWidth(bulletWidth);
        this.getImageView().setFitHeight(bulletHeight);
    }

    @Override
    public void applyAcceleration(double acc) {
        super.applyAcceleration(acc);
    }

    @Override
    public void applyMove(int screenWidth, int screenHeight) {
        super.applyMove(screenWidth, screenHeight);
    }
}