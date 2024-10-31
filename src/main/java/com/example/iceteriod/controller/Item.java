package com.example.iceteriod.controller;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class Item extends GameCharacter {

    public enum ItemType {
        SHOTGUN_AMMO,
        LIFE_POTION,
        BOMB_AMMO


        // คุณสามารถเพิ่มไอเท็มประเภทอื่น ๆ ที่นี่
    }

    private ItemType itemType;

    public Item(ItemType itemType, double x, double y) {
        super(EntityType.ITEM, 0.2, x, y);
        this.itemType = itemType;
        this.setRadius(15); // ตั้งค่ารัศมีสำหรับการชน

        // ตั้งค่าภาพตามประเภทของไอเท็ม
        String imagePath = getImagePath(itemType);
        Image image = new Image(getClass().getResourceAsStream(imagePath));

        this.getImageView().setImage(image);

        // รับค่า scale สำหรับไอเท็มชนิดนี้
        double scale = getScaleForItemType(itemType);

        // กำหนดขนาดของ ImageView ตามค่า scale ที่ต้องการ
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        this.getImageView().setFitWidth(imageWidth * scale);
        this.getImageView().setFitHeight(imageHeight * scale);

        // ตั้งค่า Anchor Point ให้อยู่ตรงกลาง
        this.getImageView().setX(-imageWidth * scale / 2);
        this.getImageView().setY(-imageHeight * scale / 2);

        // กำหนดการเคลื่อนที่ช้า ๆ สำหรับไอเท็ม
        setRandomMovement();
    }

    private double getScaleForItemType(ItemType itemType) {
        switch (itemType) {
            case SHOTGUN_AMMO:
                return 0.2; // ขนาดของไอเท็ม SHOTGUN_AMMO
            case LIFE_POTION:
                return 0.1; // ขนาดของไอเท็ม LIFE_POTION
            case BOMB_AMMO:
                return 0.3; // ขนาดของไอเท็ม LIFE_POTION
            // เพิ่มกรณีสำหรับไอเท็มอื่น ๆ ที่นี่
            default:
                return 0.5; // ขนาดเริ่มต้น
        }
    }

    private String getImagePath(ItemType itemType) {
        switch (itemType) {
            case SHOTGUN_AMMO:
                return "/sprites/shotgun_item.png";
            case LIFE_POTION:
                return "/sprites/life_potion.png";
            case BOMB_AMMO:
                return "/sprites/bomb_item.png"; // เส้นทางของภาพสำหรับไอเท็มระเบิด
            default:
                return "/sprites/default_item.png";
        }
    }

    public ItemType getItemType() {
        return itemType;
    }

    // เมธอดสำหรับกำหนดการเคลื่อนที่แบบสุ่มช้า ๆ
    private void setRandomMovement() {
        // สุ่มความเร็วและทิศทาง
        double angle = Math.random() * 360;
        double speed = 0.5; // ความเร็วช้า ๆ
        double dx = speed * Math.cos(Math.toRadians(angle));
        double dy = speed * Math.sin(Math.toRadians(angle));
        this.move = new Point2D(dx, dy);
    }

    @Override
    public void applyMove(int screenWidth, int screenHeight) {
        this.imageView.setTranslateX(this.imageView.getTranslateX() + this.move.getX());
        this.imageView.setTranslateY(this.imageView.getTranslateY() + this.move.getY());

        // เปลี่ยนทิศทางเมื่อชนขอบหน้าจอ
        if (this.imageView.getTranslateX() <= 0 || this.imageView.getTranslateX() >= screenWidth) {
            this.move = new Point2D(-this.move.getX(), this.move.getY());
        }
        if (this.imageView.getTranslateY() <= 0 || this.imageView.getTranslateY() >= screenHeight) {
            this.move = new Point2D(this.move.getX(), -this.move.getY());
        }
    }
}