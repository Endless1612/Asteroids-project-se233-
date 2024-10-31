package com.example.iceteriod.view;

import javafx.animation.Animation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class PlayerExplosion extends ImageView {
    private final Animation animation;

    public PlayerExplosion(double x, double y) {
        Image image = new Image(getClass().getResourceAsStream("/sprites/alien_explosion.png"));
        this.setImage(image);

        // ปรับขนาดของเฟรมและจำนวนเฟรมตามสไปรต์ชีต
        int columns = 6;
        int count = 6;
        int frameWidth = 32;  // ความกว้างของแต่ละเฟรม (ปรับตามสไปรต์ชีตของคุณ)
        int frameHeight = 32; // ความสูงของแต่ละเฟรม (ปรับตามสไปรต์ชีตของคุณ)

        // ตั้งค่า Viewport เริ่มต้น
        this.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));

        // ตั้งตำแหน่งของการระเบิด
        this.setTranslateX(x - frameWidth / 2);
        this.setTranslateY(y - frameHeight / 2);

        // ขยายเอฟเฟกต์การระเบิด
        this.setScaleX(4.0); // ปรับขนาดตามต้องการ
        this.setScaleY(4.0);

        // สร้างแอนิเมชันการระเบิด
        animation = new SpriteAnimation(
                this,
                Duration.millis(300), // ระยะเวลาของแอนิเมชัน
                count, columns,
                0, 0,
                frameWidth, frameHeight
        );
        animation.setCycleCount(1);
        animation.setOnFinished(e -> {
            if (this.getParent() instanceof Group) {
                ((Group) this.getParent()).getChildren().remove(this);
            }
        });
        animation.play();
    }
}