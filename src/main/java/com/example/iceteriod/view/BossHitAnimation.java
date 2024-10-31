package com.example.iceteriod.view;

import javafx.animation.Animation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class BossHitAnimation extends ImageView {
    private final Animation animation;

    public BossHitAnimation(double x, double y) {
        Image image = new Image(getClass().getResourceAsStream("/sprites/boss_hit_animation1.png"));
        this.setImage(image);

        // Adjust the viewport to the size of a single frame.
        this.setViewport(new Rectangle2D(0, 0, 96, 96));

        // Position at the collision point.
        this.setTranslateX(x - 48); //
        this.setTranslateY(y - 48);

        // Scale the animation if necessary.
        this.setScaleX(0.6);
        this.setScaleY(0.6);

        // Create the animation with adjusted frame size.
        animation = new SpriteAnimation(
                this,
                Duration.millis(150), // Duration for one animation loop
                5, // Total number of frames
                5, // Number of columns in the sprite sheet
                0, 0, // Offset for the first frame
                139, 121 // Width and height of each frame
        );
        animation.setCycleCount(1);
        animation.setOnFinished(e -> {
            // Remove the animation from the scene when done.
            if (this.getParent() instanceof Group) {
                ((Group) this.getParent()).getChildren().remove(this);
            }
        });
        animation.play();
    }
}