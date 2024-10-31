package com.example.iceteriod.controller;

import com.example.iceteriod.view.SpriteAnimation;
import com.example.iceteriod.model.GameLogic;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class BombBullet extends GameCharacter {

    private boolean isExploding = false;
    private ImageView explosionImageView;
    private Transition explosionAnimation;
    private SpriteAnimation moveAnimation;

    public BombBullet(EntityType entityType, double x, double y) {
        super(entityType, 1.0, x, y);

        // โหลดสไปรต์ชีตของกระสุนระเบิด
        Image spriteSheet = new Image(getClass().getResourceAsStream("/sprites/bomb_bullet_sprite.png"));
        this.getImageView().setImage(spriteSheet);

        // กำหนดขนาดของแต่ละเฟรมในสไปรต์ชีต
        int frameWidth = 41; // กำหนดตามขนาดของแต่ละเฟรม
        int frameHeight = 41;
        int columns = 3; // จำนวนคอลัมน์ในสไปรต์ชีต
        int totalFrames = 3;

        // ตั้งค่า Viewport เริ่มต้น
        this.getImageView().setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));

        // สร้างและตั้งค่า SpriteAnimation สำหรับการเคลื่อนที่
        moveAnimation = new SpriteAnimation(
                this.getImageView(),
                Duration.millis(50), // ความเร็วแอนิเมชันของกระสุน
                totalFrames, columns,
                0, 0,
                frameWidth, frameHeight
        );
        moveAnimation.setCycleCount(Animation.INDEFINITE);
        moveAnimation.play();

        // กำหนดขนาดและรัศมี
        double scale = 2.0;
        this.getImageView().setFitWidth(frameWidth * scale);
        this.getImageView().setFitHeight(frameHeight * scale);
        this.setRadius(15);
    }

    @Override
    public void setRotation(double angle) {
        super.setRotation(angle);
    }

    @Override
    public void applyAcceleration(double acc) {
        double deltaX = acc * Math.cos(Math.toRadians(this.getImageView().getRotate()));
        double deltaY = acc * Math.sin(Math.toRadians(this.getImageView().getRotate()));
        move = move.add(deltaX, deltaY);
    }

    @Override
    public void applyMove(int screenWidth, int screenHeight) {
        if (!isExploding) {
            this.getImageView().setTranslateX(this.getImageView().getTranslateX() + move.getX());
            this.getImageView().setTranslateY(this.getImageView().getTranslateY() + move.getY());

            // จัดการการวนรอบหน้าจอ
            if (this.getImageView().getTranslateX() < 0) {
                this.getImageView().setTranslateX(screenWidth);
            } else if (this.getImageView().getTranslateX() > screenWidth) {
                this.getImageView().setTranslateX(0);
            }

            if (this.getImageView().getTranslateY() < 0) {
                this.getImageView().setTranslateY(screenHeight);
            } else if (this.getImageView().getTranslateY() > screenHeight) {
                this.getImageView().setTranslateY(0);
            }
        }
    }

    public void explode(Group root, List<AsteroidClass> asteroidList, GameLogic gameLogic, PlayerShip playerShip) {
        isExploding = true;

        // ลบภาพกระสุนระเบิดออก
        root.getChildren().remove(this.getImageView());

        // สร้าง ImageView สำหรับแอนิเมชันการระเบิด
        Image explosionImage = new Image(getClass().getResourceAsStream("/sprites/explosion_big.png"));
        explosionImageView = new ImageView(explosionImage);

        // ตั้งค่าขนาดของเฟรมและจำนวนเฟรมตามสไปรต์ชีต
        int columns = 8;
        int rows = 1;
        int frameWidth = 132;
        int frameHeight = 136;
        int totalFrames = columns * rows;

        // ตั้งค่า Viewport เริ่มต้น
        explosionImageView.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));

        // ปรับขนาดของแอนิเมชันการระเบิด
        double scale = 3.0; // ปรับขนาดตามที่คุณต้องการ
        explosionImageView.setFitWidth(frameWidth * scale);
        explosionImageView.setFitHeight(frameHeight * scale);

        // ตั้งตำแหน่งของแอนิเมชัน
        double x = this.getImageView().getTranslateX();
        double y = this.getImageView().getTranslateY();
        explosionImageView.setTranslateX(x - (frameWidth * scale) / 2);
        explosionImageView.setTranslateY(y - (frameHeight * scale) / 2);

        // เพิ่มแอนิเมชันการระเบิดลงใน root
        root.getChildren().add(explosionImageView);

        // นำ explosionImageView ไปไว้ด้านหน้าสุด
        explosionImageView.toFront();

        // นำยานของผู้เล่นไปไว้ด้านหน้าสุด
        playerShip.getImageView().toFront();

        // สร้าง Transition สำหรับแอนิเมชันการระเบิด
        explosionAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(1000));
                setInterpolator(Interpolator.LINEAR);
            }

            private int lastIndex = -1;

            @Override
            protected void interpolate(double frac) {
                int index = Math.min((int) Math.floor(frac * totalFrames), totalFrames - 1);
                if (index != lastIndex) {
                    int xIndex = index % columns;
                    int yIndex = index / columns;
                    int xOffset = xIndex * frameWidth;
                    int yOffset = yIndex * frameHeight;
                    explosionImageView.setViewport(new Rectangle2D(xOffset, yOffset, frameWidth, frameHeight));
                    lastIndex = index;
                }
            }
        };

        // เมื่อแอนิเมชันจบ ให้ลบแอนิเมชันออกจาก root
        explosionAnimation.setOnFinished(event -> {
            root.getChildren().remove(explosionImageView);
        });

        // เริ่มต้นแอนิเมชัน
        explosionAnimation.play();

        // กำหนดรัศมีการทำลายล้าง
        double explosionRadius = 100 * scale; // ปรับรัศมีตามความเหมาะสม

        // ทำลายดาวเคราะห์น้อยใกล้เคียง
        List<AsteroidClass> asteroidsToRemove = new ArrayList<>();
        for (AsteroidClass asteroid : asteroidList) {
            double dx = x - asteroid.getImageView().getTranslateX();
            double dy = y - asteroid.getImageView().getTranslateY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= explosionRadius) {
                asteroidsToRemove.add(asteroid);
            }
        }
        for (AsteroidClass asteroid : asteroidsToRemove) {
            gameLogic.destroyAsteroid(asteroid, true); // ส่ง true เพื่อระบุว่าถูกทำลายโดยกระสุนระเบิด
        }

        // ทำลายเอเลี่ยนใกล้เคียง (ถ้ามี)
        List<AlienShip> aliensToRemove = new ArrayList<>();
        for (AlienShip alienShip : gameLogic.getAlienShipList()) {
            double dx = x - alienShip.getImageView().getTranslateX();
            double dy = y - alienShip.getImageView().getTranslateY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= explosionRadius) {
                aliensToRemove.add(alienShip);
            }
        }
        for (AlienShip alienShip : aliensToRemove) {
            gameLogic.destroyAlienShip(alienShip);
        }
    }
}