package com.example.iceteriod.view;

import javafx.animation.Animation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class BossExplosion extends ImageView {
    private final Animation animation;

    public BossExplosion(double screenWidth, double screenHeight) {
        Image image = new Image(getClass().getResourceAsStream("/sprites/bossexplotion.png"));
        this.setImage(image);

        // ปรับขนาดให้เต็มหน้าจอ
        this.setViewport(new Rectangle2D(0, 0, 427, 221));
        this.setFitWidth(screenWidth);
        this.setFitHeight(screenHeight);

        // ตั้งตำแหน่งให้อยู่ที่จุดบนซ้ายของหน้าจอ
        this.setTranslateX(0);
        this.setTranslateY(0);

        // สร้างแอนิเมชันสำหรับเอฟเฟกต์ระเบิด
        animation = new SpriteAnimation(
                this,
                Duration.millis(400),
                11, // จำนวนเฟรมทั้งหมด
                4,  // จำนวนคอลัมน์
                0, 0,
                427, 221
        );

        animation.setCycleCount(1);
        animation.setOnFinished(e -> {
            // ลบการแสดงผลหลังจากแอนิเมชันเสร็จสิ้น
            if (this.getParent() instanceof Group) {
                ((Group) this.getParent()).getChildren().remove(this);
            }
        });
        animation.play();
    }
}