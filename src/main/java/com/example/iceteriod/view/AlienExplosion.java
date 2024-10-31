package com.example.iceteriod.view;

import javafx.animation.Animation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class AlienExplosion extends ImageView {
    private static final String ALIEN_EXPLOSION_IMAGE = "/sprites/alien_explosion.png"; // ตรวจสอบให้แน่ใจว่าเส้นทางถูกต้อง
    private static final int COLUMNS = 6;
    private static final int COUNT = 6;
    private static final int WIDTH = 32;  // ความกว้างของแต่ละเฟรม
    private static final int HEIGHT = 32; // ความสูงของแต่ละเฟรม

    private final Animation animation;

    public AlienExplosion(double x, double y) {
        Image image = new Image(getClass().getResourceAsStream(ALIEN_EXPLOSION_IMAGE));
        this.setImage(image);
        this.setViewport(new Rectangle2D(0, 0, WIDTH, HEIGHT));
        this.setTranslateX(x - WIDTH / 2);
        this.setTranslateY(y - HEIGHT / 2);

        this.setScaleX(4.0); // ขยายเป็น 2 เท่าในแกน X
        this.setScaleY(4.0); // ขยายเป็น 2 เท่าในแกน Y

        animation = new SpriteAnimation(
                this,
                Duration.millis(300), // ระยะเวลาของแอนิเมชัน
                COUNT, COLUMNS,
                0, 0,
                WIDTH, HEIGHT
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
