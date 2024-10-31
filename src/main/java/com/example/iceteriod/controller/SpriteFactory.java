package com.example.iceteriod.controller;

public class SpriteFactory {

    public static GameCharacter createEntity(GameCharacter.EntityType entityType, double x, double y, int level) {
        switch (entityType) {
            case PLAYER_SHIP:
                return new PlayerShip(entityType, 1.0, x, y);
            case LARGE_ASTEROID:
                return new AsteroidClass(entityType, x, y, 1.5);
            case MEDIUM_ASTEROID:
                return new AsteroidClass(entityType, x, y, 0.7);
            case SMALL_ASTEROID:
                return new AsteroidClass(entityType, x, y, 0.3);
            case ALIEN_SHIP:
                return new AlienShip(entityType, x, y, 0.6);
            case BULLET:
                return new Bullet(entityType, x, y);
            case ALIEN_BULLET:
                return new AlienBullet(entityType, x, y);
            case BOSS:
                return new Boss(entityType, x, y, level); // สร้าง Boss อย่างถูกต้อง
            case BOSS_BULLET:
                return new BossBullet(entityType, x, y, 1000, 1000);
            default:
                throw new IllegalArgumentException("Invalid entity type: " + entityType);
        }
    }
}
