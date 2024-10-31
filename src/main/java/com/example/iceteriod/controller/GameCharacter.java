package com.example.iceteriod.controller;

import com.example.Exception.ExceptionHandler;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.io.FileNotFoundException;

public class GameCharacter {

    public enum EntityType {
        PLAYER_SHIP,
        LARGE_ASTEROID,
        MEDIUM_ASTEROID,
        SMALL_ASTEROID,
        ALIEN_SHIP,
        BULLET,
        ALIEN_BULLET,
        ITEM,
        LIFE_ICON,
        BOMB_BULLET,
        BOSS,
        BOSS_BULLET
    }

    public EntityType entityType;
    protected double rotation;
    protected double scale = 1;
    protected double radius = 30; // ค่าเริ่มต้นของรัศมี
    protected ImageView imageView;
    protected Point2D move = new Point2D(0, 0);

    protected double angle; // ฟิลด์สำหรับเก็บค่ามุมการหมุน

    public GameCharacter(EntityType entityType, double scale, double x, double y) {
        this.entityType = entityType;
        this.scale = scale;

        String imagePath = getImagePath(entityType);

        try {
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream == null) {
                throw new FileNotFoundException("Image not found at path: " + imagePath);
            }
            Image image = new Image(imageStream);
            imageView = new ImageView(image);
            imageView.setPreserveRatio(true);

        } catch (Exception e) {

            imageView = new ImageView();
            ExceptionHandler.handle(e,"Failed to load image: " + imagePath);
        }

        imageView.setTranslateX(x);
        imageView.setTranslateY(y);

        this.move = new Point2D(0, 0);
        this.rotation = 0;
        imageView.setRotate(this.rotation);

        setScale(scale); // เรียกใช้ setScale เพื่อปรับขนาดและตำแหน่ง
    }

    private String getImagePath(EntityType entityType) {
        switch (entityType) {
            case PLAYER_SHIP:
                return "/sprites/player_ship_sprites2.png"; // ใช้ Sprite Sheet ของผู้เล่น
            case LARGE_ASTEROID:
                return "/sprites/rock.png";
            case MEDIUM_ASTEROID:
                return "/sprites/rock.png";
            case SMALL_ASTEROID:
                return "/sprites/rock.png";
            case ALIEN_SHIP:
                return "/sprites/enemy.png";
            case BULLET:
                return "/sprites/bullet_sprite.png";
            case ALIEN_BULLET:
                return "/sprites/alien_bullet.png";
            case BOSS:
                return "/sprites/boss.png";
            case BOSS_BULLET:
                return "/sprites/boss_bullet.png";
            case LIFE_ICON:
                return "/sprites/life_icon.png";
            case BOMB_BULLET:
                return "/sprites/bomb_bullet_sprite.png";
            default:
                return "";
        }
    }

    public void setScale(double scale) {
        this.scale = scale;
        Image image = imageView.getImage();
        if (image != null) {
            if (this.entityType != EntityType.PLAYER_SHIP) { // แก้ไขเงื่อนไข
                // ปรับขนาดของ ImageView ตาม scale สำหรับวัตถุอื่นๆ
                imageView.setFitWidth(image.getWidth() * scale);
                imageView.setFitHeight(image.getHeight() * scale);
                imageView.setPreserveRatio(true);

                // ปรับตำแหน่งเพื่อให้ ImageView อยู่กึ่งกลาง
                imageView.setX(-imageView.getFitWidth() / 2);
                imageView.setY(-imageView.getFitHeight() / 2);

                // ปรับค่า radius ให้เหมาะสมกับขนาดใหม่
                this.setRadius(Math.max(imageView.getFitWidth(), imageView.getFitHeight()) / 2);
            } else {
                // สำหรับ PLAYER_SHIP ให้ไม่ปรับ fitWidth และ fitHeight เนื่องจาก PlayerShip จัดการเอง
                imageView.setScaleX(scale);
                imageView.setScaleY(scale);
                imageView.setPreserveRatio(true);
            }
        }
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return Math.max(imageView.getBoundsInLocal().getWidth() * imageView.getScaleX(),
                imageView.getBoundsInLocal().getHeight() * imageView.getScaleY()) / 2;
    }

    public double getAngle() {
        return imageView.getRotate();
    }

    protected void rotLeft() {
        this.imageView.setRotate(this.imageView.getRotate() - 4);
    }

    protected void rotRight() {
        this.imageView.setRotate(this.imageView.getRotate() + 4);
    }

    public void setRotation(double angle) {
        this.imageView.setRotate(angle);
    }

    protected void applyMove(int screenWidth, int screenHeight) {
        this.imageView.setTranslateX(this.imageView.getTranslateX() + this.move.getX());
        this.imageView.setTranslateY(this.imageView.getTranslateY() + this.move.getY());

        if (this.imageView.getTranslateX() > screenWidth + this.radius) {
            this.imageView.setTranslateX(-this.radius);
        }
        if (this.imageView.getTranslateX() < -this.radius) {
            this.imageView.setTranslateX(screenWidth + this.radius);
        }
        if (this.imageView.getTranslateY() > screenHeight + this.radius) {
            this.imageView.setTranslateY(-this.radius);
        }
        if (this.imageView.getTranslateY() < -this.radius) {
            this.imageView.setTranslateY(screenHeight + this.radius);
        }
    }

    protected void applyAcceleration(double da) {
        double angleInRadians = Math.toRadians(this.imageView.getRotate());
        double dx = da * Math.cos(angleInRadians);
        double dy = da * Math.sin(angleInRadians);
        this.move = this.move.add(dx, dy);
    }

    public Point2D getMove() {
        return this.move;
    }

    protected void halt() {
        this.move = new Point2D(0, 0);
    }

    public void strafeLeft(double amount) {
        move = move.subtract(amount, 0);
    }

    public void strafeRight(double amount) {
        move = move.add(amount, 0);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public boolean collision(GameCharacter other) {
        double dx = this.getImageView().getTranslateX() - other.getImageView().getTranslateX();
        double dy = this.getImageView().getTranslateY() - other.getImageView().getTranslateY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (this.getRadius() + other.getRadius());
    }
}