package com.example.iceteriod.view;

import javafx.animation.Animation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Explosion extends ImageView {
    private final Animation animation;

    public Explosion(double x, double y, String imagePath, int count, int columns, int width, int height, Duration duration) {
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        this.setImage(image);
        this.setViewport(new Rectangle2D(0, 0, width, height));
        this.setTranslateX(x - width / 2);
        this.setTranslateY(y - height / 2);
        this.setScaleX(1.5); // ขยายเป็น 2 เท่าในแกน X
        this.setScaleY(1.5);

        animation = new SpriteAnimation(
                this,
                duration,
                count, columns,
                0, 0,
                width, height
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
