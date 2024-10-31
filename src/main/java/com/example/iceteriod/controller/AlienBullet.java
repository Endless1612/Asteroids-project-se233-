package com.example.iceteriod.controller;

public class AlienBullet extends GameCharacter {

    public AlienBullet(EntityType entityType, double x, double y) {
        super(entityType, 1.0, x, y);
        this.setRadius(2);
    }

    public void applyAcceleration(double acc) {
        super.applyAcceleration(acc);
    }

    public void rotLeft() {
        super.rotLeft();
    }

    public void rotRight() {
        super.rotRight();
    }

    public void applyMove(int screenWidth, int screenHeight) {
        super.applyMove(screenWidth, screenHeight);
    }
}
