package com.example.iceteriod.controller;

import com.example.iceteriod.view.SpriteAnimation;
import com.example.iceteriod.model.GameLogic;
import javafx.animation.Animation;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class Boss extends GameCharacter {

    private int hp;
    private int maxHp;
    private long lastShotTime = 0;
    private boolean isAlive = true;
    protected int level;
    private Animation spriteAnimation;


    // เพิ่มตัวแปรใหม่
    private int maxBulletsPerShot;
    private int currentBulletsShot = 0;
    private double moveSpeed = 2.0;
    private boolean movingRight = true;

    private static final double BULLET_WIDTH = 80; // ความกว้างของกระสุนบอส
    private static final double BULLET_HEIGHT = 80; // ความสูงของกระสุนบอส
    private static final double BULLET_SPEED_BASE = 2.0; // ความเร็วพื้นฐานของกระสุนบอส
    private static final double BULLET_SPEED_PER_LEVEL = 0.1; // ความเร็วเพิ่มขึ้นต่อเลเวล

    public Boss(EntityType entityType, double x, double y, int level) {
        super(entityType, 1.0, x, y);

        this.level = level;
        this.maxHp = 10 + (level * 5);
        this.hp = maxHp;

        // Determine max bullets based on level
        if (level < 10) {
            this.maxBulletsPerShot = 3;
        } else {
            this.maxBulletsPerShot = 6;
        }

        // Load the sprite sheet for the boss
        Image bossSpriteSheet = new Image(getClass().getResourceAsStream("/sprites/ice_sprite.png"));
        this.getImageView().setImage(bossSpriteSheet);

        // Set the size of the boss
        this.getImageView().setFitWidth(200);
        this.getImageView().setFitHeight(200);
        this.getImageView().setTranslateX(x);
        this.getImageView().setTranslateY(y);

        // Initialize SpriteAnimation
        spriteAnimation = new SpriteAnimation(
                this.getImageView(),
                Duration.millis(200), // Total duration of one animation cycle
                3, // Total number of frames
                3, // Number of columns in the sprite sheet
                0, 0, // Starting X and Y offsets
                512, 594 // Width and height of each frame
        );
        spriteAnimation.setCycleCount(Animation.INDEFINITE); // Loop indefinitely
        spriteAnimation.play(); // Start the animation
    }

    private double calculateAngleToPlayer(double playerX, double playerY) {
        double deltaX = playerX - this.getImageView().getTranslateX();
        double deltaY = playerY - this.getImageView().getTranslateY();
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
        return angle;
    }


    public boolean isAlive() {
        return isAlive;
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.isAlive = false;
        }
    }

    public int getHp() {
        return hp;
    }

    // ปรับปรุงเมธอด shoot()
    public void shoot(GameLogic gameLogic) {
        long currentTime = System.currentTimeMillis();
        int shootInterval = Math.max(4000 - (level * 100), 500); // ลดเวลาการยิงลงตามเลเวล แต่ไม่ต่ำกว่า 500ms

        if (currentTime - lastShotTime < shootInterval) {
            return; // ควบคุมอัตราการยิงของบอส
        }
        lastShotTime = currentTime;

        // รีเซ็ตจำนวนกระสุนที่ยิงในแต่ละครั้ง
        currentBulletsShot = 0;

        // ดึงตำแหน่งของผู้เล่นจาก GameLogic
        double playerX = gameLogic.getPlayerShip().getImageView().getTranslateX();
        double playerY = gameLogic.getPlayerShip().getImageView().getTranslateY();

        // คำนวณมุมจากบอสไปยังผู้เล่น
        double baseAngle = calculateAngleToPlayer(playerX, playerY);

        // กำหนดมุมกระจาย
        double angleSpread = 30; // มุมกระจาย 30 องศา
        double startAngle = baseAngle - (angleSpread / 2);

        // ยิงกระสุนตามจำนวนที่กำหนด
        for (int i = 0; i < maxBulletsPerShot; i++) {
            double angle = startAngle + ((angleSpread / (maxBulletsPerShot - 1)) * i);

            // สร้างกระสุนบอสจากตำแหน่งด้านล่างของบอสโดยตรง
            BossBullet bossBullet = new BossBullet(
                    GameCharacter.EntityType.BOSS_BULLET,
                    this.getImageView().getTranslateX(),
                    this.getImageView().getTranslateY() + this.getImageView().getFitHeight() / 2,
                    BULLET_WIDTH, // ใช้ค่าคงที่สำหรับขนาด
                    BULLET_HEIGHT
            );
            bossBullet.setRotation(angle);
            bossBullet.applyAcceleration(BULLET_SPEED_BASE + (level * BULLET_SPEED_PER_LEVEL)); // ใช้ค่าคงที่สำหรับความเร็ว
            gameLogic.addBossBullet(bossBullet);
            currentBulletsShot++;
        }
    }


    // ปรับปรุงเมธอดเคลื่อนไหว
    public void moveLeftRight(int screenWidth) {
        double currentX = this.getImageView().getTranslateX();
        double movementRange = 200; // กำหนดขอบเขตการเคลื่อนไหวซ้ายขวา

        if (movingRight) {
            currentX += moveSpeed;
            if (currentX >= (screenWidth / 2) + movementRange) {
                movingRight = false;
            }
        } else {
            currentX -= moveSpeed;
            if (currentX <= (screenWidth / 2) - movementRange) {
                movingRight = true;
            }
        }
        this.getImageView().setTranslateX(currentX);
    }

    public void setHp(int hp){
        this.hp = hp;
        if (this.hp <= 0){
            this.isAlive = false;
        } else {
        }
    }
}