package com.example.iceteriod.controller;

public class AlienShip extends GameCharacter {

    public boolean alive = true;

    public AlienShip(EntityType entityType, double x, double y, double scale) {
        super(entityType, scale, x, y);
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

    public boolean collision(GameCharacter other) {
        return super.collision(other);
    }

    public boolean isAlive() {
        return alive;
    }

}
