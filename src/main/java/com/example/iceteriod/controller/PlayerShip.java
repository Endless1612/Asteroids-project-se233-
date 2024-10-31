package com.example.iceteriod.controller;

import com.example.iceteriod.view.SpriteAnimation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class PlayerShip extends GameCharacter {

    private Animation spriteAnimation; // Sprite Animation
    private boolean immune = false;
    public boolean alive = true;

    private boolean isShotgunMode = false;
    private long shotgunModeEndTime = 0;

    private boolean isBombMode = false;
    private long bombModeEndTime = 0;

    // ขนาดเฟรม
    private int frameWidth;
    private int frameHeight;

    public PlayerShip(EntityType entityType, double scale, double x, double y) {
        super(entityType, scale, x, y);
        this.getImageView().setOpacity(1);

        initializeAnimation(); // เริ่มต้นอนิเมชัน
    }

    private void initializeAnimation() {
        Image spriteSheet = this.getImageView().getImage();
        int count = 6; // จำนวนเฟรมทั้งหมด
        int columns = 6; // จำนวนคอลัมน์ใน Sprite Sheet
        int offsetX = 0;
        int offsetY = 0;
        this.frameWidth = 320;
        this.frameHeight = 340;

        spriteAnimation = new SpriteAnimation(
                this.getImageView(),
                Duration.millis(600), // ระยะเวลาของอนิเมชันหนึ่งรอบ
                count, columns,
                offsetX, offsetY,
                frameWidth, frameHeight
        );
        spriteAnimation.setCycleCount(1); // เล่นเพียงรอบเดียว แล้วหยุดที่เฟรมสุดท้าย
        spriteAnimation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // ค้างไว้ที่เฟรมสุดท้ายเมื่อแอนิเมชันหยุด
                getImageView().setViewport(new Rectangle2D(
                        frameWidth * (count - 1), 0, frameWidth, frameHeight
                ));
            }
        });

        this.imageView.setFitWidth(frameWidth * scale);
        this.imageView.setFitHeight(frameHeight * scale);
        this.imageView.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));
        this.imageView.setPreserveRatio(true);

        this.imageView.setX(- (frameWidth * scale) / 2);
        this.imageView.setY(- (frameHeight * scale) / 2);
    }

    public void applyAcceleration(double acc) {
        super.applyAcceleration(acc);
    }

    public Point2D getMove() {
        return this.move;
    }

    public void rotLeft() {
        super.rotLeft();
    }

    public void rotRight() {
        super.rotRight();
    }

    @Override
    public void applyMove(int screenWidth, int screenHeight) {
        super.applyMove(screenWidth, screenHeight);

        // เพิ่ม damping เพื่อชะลอการเคลื่อนที่
        double dampingFactor = 0.90; // ปรับค่าได้ตามต้องการ
        this.move = this.move.multiply(dampingFactor);

        // หยุดการเคลื่อนที่ถ้าน้อยกว่า threshold
        if (this.move.magnitude() < 0.01) {
            this.move = new Point2D(0, 0);
        }
    }

    public void halt() {
        this.move = new Point2D(0, 0);
        updateAnimationState();
    }

    public boolean collision(GameCharacter other) {
        return super.collision(other);
    }

    public boolean isAlive() {
        return alive;
    }

    public double getPositionX() {
        return this.getImageView().getTranslateX();
    }

    public double getPositionY() {
        return this.getImageView().getTranslateY();
    }

    public void hyperjump() {
        double randomX = Math.random() * 1200;
        double randomY = Math.random() * 800;
        this.getImageView().setTranslateX(randomX);
        this.getImageView().setTranslateY(randomY);
        this.halt();
    }

    public void setImmune(boolean immune) {
        this.immune = immune;
        if (immune) {
            Timeline immunity = new Timeline(
                    new KeyFrame(Duration.seconds(0.10), event -> this.getImageView().setOpacity(0.5)),
                    new KeyFrame(Duration.seconds(0.20), event -> this.getImageView().setOpacity(1))
            );
            immunity.setCycleCount(10);
            immunity.setOnFinished(event -> this.immune = false);
            immunity.play();
        } else {
            this.getImageView().setOpacity(1);
        }
    }

    public void setRotation(double angle) {
        super.setRotation(angle);
    }

    public void strafeLeft(double amount) {
        super.strafeLeft(amount);
    }

    public void strafeRight(double amount) {
        super.strafeRight(amount);
    }

    // เมธอดสำหรับโหมด Shotgun
    public void activateShotgunMode() {
        isShotgunMode = true;
        shotgunModeEndTime = System.currentTimeMillis() + 30000; // โหมด Shotgun นาน 30 วินาที

        Timeline shotgunTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            isShotgunMode = false;
        }));
        shotgunTimeline.play();
    }

    public boolean isShotgunMode() {
        return isShotgunMode;
    }

    public void activateBombMode() {
        isBombMode = true;
        bombModeEndTime = System.currentTimeMillis() + 10000; // โหมดระเบิดนาน 10 วินาที

        Timeline bombTimeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            isBombMode = false;
        }));
        bombTimeline.play();
    }

    public boolean isBombMode() {
        return isBombMode;
    }

    public void updateAnimationState() {
        if (this.getMove().magnitude() > 0) {
            // ยานกำลังเคลื่อนที่
            if (spriteAnimation.getStatus() != Animation.Status.RUNNING) {
                spriteAnimation.play();
            }
        } else {
            // ยานอยู่ในสถานะ idle
            if (spriteAnimation.getStatus() == Animation.Status.RUNNING) {
                spriteAnimation.stop();
            }
            // ค่อยๆ ลดความเร็วและตั้งค่า viewport เป็นเฟรมแรก (เฟรม idle)
            this.getImageView().setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));
        }
    }

    // เพิ่มเมธอด setAlive
    public void setAlive(boolean alive) {
        this.alive = alive;

    }



}