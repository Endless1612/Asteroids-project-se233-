package com.example.iceteriod.model;

import com.example.Exception.ExceptionHandler;
import com.example.iceteriod.*;
import com.example.iceteriod.controller.*;
import com.example.iceteriod.view.*;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.application.Platform; // <-- Add this line

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class GameLogic {
    private Scene mainScene;
    private PlayerShip playerShip;
    private Group root;

    private final List<AsteroidClass> asteroidList = new CopyOnWriteArrayList<>();
    private final List<AlienShip> alienShipList = new CopyOnWriteArrayList<>();
    private final List<AlienBullet> alienBulletList = new CopyOnWriteArrayList<>();
    private final List<Bullet> bulletList = new CopyOnWriteArrayList<>();
    private final List<ImageView> playerLives = new CopyOnWriteArrayList<>();
    private final List<Item> itemList = new CopyOnWriteArrayList<>(); // เพิ่มรายการไอเท็ม
    private final List<BombBullet> bombBulletList = new CopyOnWriteArrayList<>();

    private final Map<KeyCode, Boolean> pressedKeys = new HashMap<>();

    private AnimationTimer gameLoop;


    // Flags and Counters
    private boolean isAlienSpawned = false;
    private long lastAlienBulletTime = 0;
    private int gameLevel = 1;
    private final AtomicInteger score = new AtomicInteger(0);
    private final AtomicBoolean didHyperJump = new AtomicBoolean(false);
    private final AtomicBoolean replenishedLife = new AtomicBoolean(false);
    private final AtomicBoolean isGameOver = new AtomicBoolean(false);
    private AtomicBoolean isImmune = new AtomicBoolean(false);
    private long lastItemSpawnTime = 0; // สำหรับจัดการเวลาการ spawn ไอเท็ม


    // UI Elements
    private TextClass scoreText;
    private TextClass levelText;
    private TextClass gameOverText;
    private TextClass asteroidsCountText;
    private TextClass hyperJumpText;
    private TextClass youDiedText;
    private TextClass nextLevelText;
    private TextClass countdownText;
    private Group gameOverPane;
    private TextClass gameOverTitle;
    private TextClass gameOverScore;
    private TextClass gameOverInstruction;
    private Group gameLayer; // เพิ่มเลเยอร์สำหรับเกม
    private Group uiLayer;   // เพิ่มเลเยอร์สำหรับ UI

    private String playerName = "Anonymous"; // ชื่อผู้เล่นเริ่มต้น

    private int alienSpawnCount = 0;
    private long lastAlienSpawnTime = 0;
    private boolean isLevelTransitioning = false;

    private Boss boss;
    private final List<BossBullet> bossBulletList = new CopyOnWriteArrayList<>();
    private boolean isBossLevel = false;
    private TextClass bossHpText;
    public boolean alive = true;




    private static final Logger logger = LogManager.getLogger(GameLogic.class);
    private final AtomicBoolean isAccelerating = new AtomicBoolean(false);
    private final AtomicBoolean isDecelerating = new AtomicBoolean(false);
    private final AtomicBoolean isStrafingLeft = new AtomicBoolean(false);
    private final AtomicBoolean isStrafingRight = new AtomicBoolean(false);
    private final AtomicBoolean isHyperJumping = new AtomicBoolean(false);

    public void start(Stage mainStage) {
        root = new Group();
        if (root == null) {
            logger.fatal("Root node is null. Cannot start the game.");
            Platform.exit(); // หยุดโปรแกรมทันที
        }
        mainScene = new Scene(root, 1280, 832, Color.web("#070020"));

        // สร้างเลเยอร์เกมและ UI
        gameLayer = new Group();
        uiLayer = new Group();

        // เพิ่มเลเยอร์เกมก่อน
        root.getChildren().add(gameLayer);

        // เพิ่มภาพพื้นหลัง
        addBackground();

        try {
            // คุณสามารถเพิ่มองค์ประกอบเพิ่มเติมได้ที่นี่
        } catch (Exception e) {
            ExceptionHandler.handle(e,"Cannot find background photo"+ e.getMessage());
        }
        mainStage.setScene(mainScene);
        mainStage.setTitle("Asteroids");

        initializePlayer();
        initializeAsteroids();
        initializeLives();
        initializeUI(); // Initialize UI ที่จะถูกเพิ่มใน uiLayer

        // เพิ่มเลเยอร์ UI หลังจากเลเยอร์เกม
        root.getChildren().add(uiLayer);

        setupInputHandlers();
        setupMouseHandlers();

        startGameLoop();

        mainStage.show();

        logger.info("Game Started");
    }

    private void addBackground() {
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/sprites/background.png"));
            ImageView backgroundImageView = new ImageView(backgroundImage);
            backgroundImageView.setFitWidth(1280);
            backgroundImageView.setFitHeight(832);
            backgroundImageView.setPreserveRatio(false);
            gameLayer.getChildren().add(backgroundImageView);
        } catch (Exception e) {
            ExceptionHandler.handle(e,"Cannot find background photo: " + e.getMessage());
        }
    }

    public void setGameLayer(Group gameLayer) {
        this.gameLayer = gameLayer;
    }

    public void setUiLayer(Group uiLayer) {
        this.uiLayer = uiLayer;
    }

    public void saveHighScore() {
        HighScoreEntry newEntry = new HighScoreEntry(playerName, score.get(), gameLevel);

        List<HighScoreEntry> highScores = readHighScores();
        highScores.add(newEntry);
        Collections.sort(highScores);
        if (highScores.size() > 5) {
            highScores = highScores.subList(0, 5); // เก็บเฉพาะ 5 อันดับแรก
        }

        writeHighScores(highScores);
    }

    public List<HighScoreEntry> readHighScores() {
        List<HighScoreEntry> highScores = new ArrayList<>();
        File file = new File("src/main/resources/high_scores.txt"); // ตรวจสอบเส้นทางให้ถูกต้อง

        if (!file.exists()) {
            return highScores; //
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    int level = Integer.parseInt(parts[2]);
                    highScores.add(new HighScoreEntry(name, score, level));
                }
            }
        } catch (IOException | NumberFormatException e) {
            ExceptionHandler.handle(e, "Failed to Reading High Scores");
        }

        return highScores;
    }

    private void writeHighScores(List<HighScoreEntry> highScores) {
        File file = new File("src/main/resources/high_scores.txt"); // ตรวจสอบเส้นทางให้ถูกต้อง

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (HighScoreEntry entry : highScores) {
                writer.write(entry.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e,"Failed to Writing High Scores");
        }
    }

    // เพิ่มเมธอดสำหรับตั้งชื่อผู้เล่นจากหน้าจอ Enter Name
    public void setPlayerName(String name) {
        this.playerName = name;
    }


    public void initializePlayer() {
        playerShip = new PlayerShip(GameCharacter.EntityType.PLAYER_SHIP, 0.5, 600, 400);
        gameLayer.getChildren().add(playerShip.getImageView()); // เพิ่มใน gameLayer
    }

    public void setScoreText(TextClass scoreText) {
        this.scoreText = scoreText;
    }
    public void setPlayerShip(PlayerShip playerShip) {
        this.playerShip = playerShip;
    }

    private void initializeUI() {
        scoreText = new TextClass("Score: 0", 30, 50, Color.WHITE, 40);
        uiLayer.getChildren().add(scoreText.mytext); // เปลี่ยนจาก root เป็น uiLayer

        levelText = new TextClass("Level 1", 1100, 50, Color.WHITE, 30);
        uiLayer.getChildren().add(levelText.mytext);

        asteroidsCountText = new TextClass("No. of asteroids: 0", 30, 750, Color.WHITE, 30);
        uiLayer.getChildren().add(asteroidsCountText.mytext);

        gameOverPane = new Group();

        // Game Over Title
        gameOverTitle = new TextClass("Game Over", 640, 300, Color.RED, 60);
        gameOverTitle.mytext.setTranslateX(gameOverTitle.mytext.getTranslateX() - gameOverTitle.mytext.getBoundsInLocal().getWidth() / 2);
        gameOverTitle.mytext.setOpacity(0);
        gameOverPane.getChildren().add(gameOverTitle.mytext);

        // Final Score
        gameOverScore = new TextClass("Final Score: 0", 590, 380, Color.WHITE, 40);
        gameOverScore.mytext.setTranslateX(gameOverScore.mytext.getTranslateX() - gameOverScore.mytext.getBoundsInLocal().getWidth() / 2);
        gameOverScore.mytext.setOpacity(0);
        gameOverPane.getChildren().add(gameOverScore.mytext);

        // Instruction to Press Enter
        gameOverInstruction = new TextClass("Press Enter to view High Scores", 640, 460, Color.YELLOW, 30);
        gameOverInstruction.mytext.setTranslateX(gameOverInstruction.mytext.getTranslateX() - gameOverInstruction.mytext.getBoundsInLocal().getWidth() / 2);
        gameOverInstruction.mytext.setOpacity(0);
        gameOverPane.getChildren().add(gameOverInstruction.mytext);

        uiLayer.getChildren().add(gameOverPane); // เพิ่มใน uiLayer

        hyperJumpText = new TextClass("Kamui available!", 900, 750, Color.WHITE, 20);
        hyperJumpText.mytext.setOpacity(0);
        uiLayer.getChildren().add(hyperJumpText.mytext);

        youDiedText = new TextClass("YOU DIED (press Enter)", 380, 600, Color.WHITE, 40);
        youDiedText.mytext.setOpacity(0);
        uiLayer.getChildren().add(youDiedText.mytext);
    }

    private void initializeAsteroids() {
        for (int i = 0; i < gameLevel; i++) {
            AsteroidClass asteroid = createLargeAsteroid();
            asteroidList.add(asteroid);
            gameLayer.getChildren().add(asteroid.getImageView());
        }
    }

    private void toggleImmune() {
        isImmune.set(!isImmune.get());
        if (isImmune.get()) {
            logger.info("Immortality Mode Activated!");

        } else {
            logger.info("Immortality Mode Deactivated!");

        }
    }

    public void initializeLives() {
        for (int i = 0; i < 3; i++) {
            Image lifeImage = new Image(getClass().getResourceAsStream("/sprites/life_icon.png"));
            ImageView lifeIcon = new ImageView(lifeImage);

            // ปรับขนาดของไอคอนชีวิต
            lifeIcon.setFitWidth(30); // ปรับความกว้างตามต้องการ
            lifeIcon.setFitHeight(30); // ปรับความสูงตามต้องการ

            // ตั้งตำแหน่งของไอคอนชีวิต
            lifeIcon.setTranslateX(950 + 35 * i); // ปรับระยะห่างระหว่างไอคอน
            lifeIcon.setTranslateY(40);

            // เพิ่มไอคอนชีวิตลงในรายการและกลุ่ม
            playerLives.add(lifeIcon);
            uiLayer.getChildren().add(lifeIcon); // เปลี่ยนจาก root เป็น uiLayer
        }
    }

    public List<ImageView> getPlayerLives() {
        return playerLives;
    }

    private void setupInputHandlers() {
        mainScene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);

            // ตรวจสอบการกดปุ่ม Enter เมื่อเกมจบ
            if (isGameOver.get() && event.getCode() == KeyCode.ENTER) {
                AsteroidsGame.getInstance().showHighScores();
            }

            // จัดการการกดปุ่ม 'I' เพื่อเปิด/ปิดโหมด immortality
            if (event.getCode() == KeyCode.I) {
                toggleImmune();
            }
        });
        mainScene.setOnKeyReleased(event -> pressedKeys.put(event.getCode(), Boolean.FALSE));
    }

    private void setupMouseHandlers() {
        // Mouse movement to rotate the ship
        mainScene.setOnMouseMoved(event -> {
            if (playerShip.isAlive()) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                double deltaX = mouseX - playerShip.getImageView().getTranslateX();
                double deltaY = mouseY - playerShip.getImageView().getTranslateY();
                double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
                playerShip.setRotation(angle);
            }
        });

        // Mouse click to shoot
        mainScene.setOnMouseClicked(event -> shootBullet());

        // Reset shoot flag when mouse button is released
        mainScene.setOnMouseReleased(event -> {});
    }

    private long lastPlayerShotTime = 0;

    private void shootBullet() {
        if (!playerShip.isAlive()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPlayerShotTime < getPlayerShootInterval()) {
            return;
        }
        lastPlayerShotTime = currentTime;

        if (playerShip.isBombMode()) {
            createBombBullet(playerShip.getImageView().getRotate());
            logger.info("Player shot a bomb bullet.");
        } else if (playerShip.isShotgunMode()) {
            int numberOfBullets = 5;
            double spreadAngle = 30;
            double baseAngle = playerShip.getImageView().getRotate();
            double startAngle = baseAngle - (spreadAngle / 2);

            for (int i = 0; i < numberOfBullets; i++) {
                double angle = startAngle + ((spreadAngle / (numberOfBullets - 1)) * i);
                createBullet(angle, true);
            }
            logger.info("Player shot in shotgun mode.");
        } else {
            createBullet(playerShip.getImageView().getRotate(), false);
            logger.info("Player shot a standard bullet.");
        }
    }

    private long getPlayerShootInterval() {
        if (playerShip.isBombMode()) {
            return 1000; // 1 วินาทีระหว่างการยิงในโหมดระเบิด
        } else {
            return 300; // 0.3 วินาทีระหว่างการยิงในโหมดปกติ
        }
    }

    private void createBombBullet(double angle) {
        double radianAngle = Math.toRadians(angle);
        double shipX = playerShip.getImageView().getTranslateX();
        double shipY = playerShip.getImageView().getTranslateY();
        double shipHeight = playerShip.getImageView().getFitHeight();
        double offsetX = Math.cos(radianAngle) * shipHeight / 2;
        double offsetY = Math.sin(radianAngle) * shipHeight / 2;
        double bulletX = shipX + offsetX;
        double bulletY = shipY + offsetY;

        BombBullet bombBullet = new BombBullet(GameCharacter.EntityType.BOMB_BULLET, bulletX, bulletY);
        bombBullet.setRotation(angle);
        bombBullet.applyAcceleration(5.0);
        bombBulletList.add(bombBullet);
        gameLayer.getChildren().add(bombBullet.getImageView()); // เพิ่มใน gameLayer

        // ลบกระสุนระเบิดหลังจาก 5 วินาทีถ้ายังไม่ระเบิด
        Timeline timeToLive = new Timeline(new KeyFrame(Duration.seconds(5), e -> removeBombBullet(bombBullet)));
        timeToLive.play();
    }

    private void removeBombBullet(BombBullet bombBullet) {
        bombBulletList.remove(bombBullet);
        gameLayer.getChildren().remove(bombBullet.getImageView());
    }

    private void createBullet(double angle, boolean isShotgunBullet) {
        double radianAngle = Math.toRadians(angle);
        double shipX = playerShip.getImageView().getTranslateX();
        double shipY = playerShip.getImageView().getTranslateY();

        // คำนวณขนาดที่ถูกต้องของยาน
        double shipWidth = playerShip.getImageView().getBoundsInLocal().getWidth() * playerShip.getImageView().getScaleX();
        double shipHeight = playerShip.getImageView().getBoundsInLocal().getHeight() * playerShip.getImageView().getScaleY();

        // คำนวณจุดที่กระสุนจะถูกสร้างขึ้นจากศูนย์กลางของยาน
        double offsetX = Math.cos(radianAngle) * (shipWidth / 2);
        double offsetY = Math.sin(radianAngle) * (shipHeight / 2);
        double bulletX = shipX + offsetX;
        double bulletY = shipY + offsetY;

        Bullet bullet = new Bullet(GameCharacter.EntityType.BULLET, bulletX, bulletY);
        bullet.setRotation(angle);
        bullet.applyAcceleration(10.0);
        bulletList.add(bullet);
        gameLayer.getChildren().add(bullet.getImageView()); // เปลี่ยนจาก root เป็น gameLayer

        // เปลี่ยนรูปร่างกระสุนเมื่ออยู่ในโหมด Shotgun
        if (isShotgunBullet) {
            bullet.getImageView().setImage(new Image(getClass().getResourceAsStream("/sprites/shot_gun_bullet_sprite.png")));
        }

        // ลบกระสุนหลังจาก 2 วินาที
        Timeline timeToLive = new Timeline(new KeyFrame(Duration.seconds(2), e -> removeBullet(bullet)));
        timeToLive.play();
    }

    private void removeBullet(Bullet bullet) {
        bulletList.remove(bullet);
        gameLayer.getChildren().remove(bullet.getImageView()); // เปลี่ยนจาก root เป็น gameLayer
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateUI();
                handleLevelProgression();
                handleAlienSpawning();
                handleAlienShooting();
                handlePlayerStatus();
                handleInput();
                updateEntities();
                handleCollisions();
                handleGameOver();
                spawnRandomItem();
                playerShip.updateAnimationState();

                if (boss != null && boss.isAlive()) {
                    boss.shoot(GameLogic.this); // เรียกใช้เมธอด shoot() ของบอส
                }
            }
        };
        gameLoop.start();
    }

    public AtomicInteger getScore(){
        return score;
    }
    private void updateUI() {
        levelText.SetText("Level " + gameLevel);
        asteroidsCountText.SetText("No. of asteroids: " + asteroidList.size());
        scoreText.SetText("Score: " + score.get());

        if (!didHyperJump.get()) {
            hyperJumpText.mytext.setOpacity(1);
        } else {
            hyperJumpText.mytext.setOpacity(0);
        }


        if (!isImmune.get()) {
            playerShip.getImageView().setOpacity(1);
        } else {
            playerShip.getImageView().setOpacity(0.5);
        }

        // แสดงเอฟเฟกต์เมื่ออยู่ในโหมด Shotgun
        if (playerShip.isShotgunMode()) {
        } else {
        }
    }

    private void showNextLevelCountdown(int nextLevel) {
        // สร้างข้อความแสดงเลเวลถัดไป
        nextLevelText = new TextClass("Level " + nextLevel, 500, 380, Color.YELLOW, 60);
        uiLayer.getChildren().add(nextLevelText.mytext);

        // สร้างข้อความนับถอยหลัง
        countdownText = new TextClass("", 610, 450, Color.WHITE, 50);
        uiLayer.getChildren().add(countdownText.mytext);

        // หยุดการทำงานของเกมชั่วคราว
        gameLoop.stop();

        // เริ่มต้นการนับถอยหลัง
        new Thread(() -> {
            try {
                for (int i = 3; i > 0; i--) {
                    final int count = i;
                    Platform.runLater(() -> countdownText.SetText("" + count));
                    Thread.sleep(1000);
                }
                // เมื่อการนับถอยหลังเสร็จสิ้น
                Platform.runLater(() -> {
                    // ลบข้อความออกจากหน้าจอ
                    uiLayer.getChildren().removeAll(nextLevelText.mytext, countdownText.mytext);
                    // สร้างอุกกาบาตใหม่สำหรับเลเวลถัดไป
                    initializeAsteroids();
                    // เริ่มต้นเกมใหม่
                    gameLoop.start();
                    // รีเซ็ตการเปลี่ยนเลเวล
                    isLevelTransitioning = false;
                });
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e,"Failed to showNextLevelCountdown");
            }
        }).start();
    }

    private void handleLevelProgression() {
        if (!isLevelTransitioning && asteroidList.isEmpty() && alienShipList.isEmpty() && (boss == null || !boss.isAlive())) {
            isLevelTransitioning = true;
            gameLevel++;
            isAlienSpawned = false;
            didHyperJump.set(false);
            replenishedLife.set(false);
            alienSpawnCount = 0;

            if (gameLevel == 4 || gameLevel == 11 || gameLevel == 16) {
                isBossLevel = true;
                showBossLevelCountdown(gameLevel);
            } else {
                isBossLevel = false;
                showNextLevelCountdown(gameLevel);
            }
        }
    }


    public void initializeBoss() {
        boss = (Boss) SpriteFactory.createEntity(GameCharacter.EntityType.BOSS, 640, 100, gameLevel);
        if (boss == null) {
            logger.error("Failed to create Boss via PolygonsFactory.");
            return;
        }
        gameLayer.getChildren().add(boss.getImageView());

        // จัดตำแหน่งผู้เล่นให้อยู่ด้านล่างบอส
        playerShip.getImageView().setTranslateX(640);
        playerShip.getImageView().setTranslateY(700);
        playerShip.halt();

        // สร้างข้อความแสดง HP ของบอส
        bossHpText = new TextClass("Boss HP: " + boss.getHp(), 540, 50, Color.RED, 30);
        uiLayer.getChildren().add(bossHpText.mytext);

        logger.info("Boss initialized at position (" + boss.getImageView().getTranslateX() + ", " + boss.getImageView().getTranslateY() + ") with HP: " + boss.getHp());
    }

    public void addBossBullet(BossBullet bossBullet) {
        bossBulletList.add(bossBullet);
        gameLayer.getChildren().add(bossBullet.getImageView());

        // ลบกระสุนบอสหลังจาก 5 วินาที
        Timeline timeToLive = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            bossBulletList.remove(bossBullet);
            gameLayer.getChildren().remove(bossBullet.getImageView());
        }));
        timeToLive.play();
    }


    private void createBossExplosion() {
        BossExplosion bossExplosion = new BossExplosion(1280, 832); // กำหนดความกว้างและความสูงของหน้าจอ
        gameLayer.getChildren().add(bossExplosion);
    }

    public void destroyBoss() {
        // สร้างเอฟเฟกต์ระเบิดเฉพาะตัวของบอส
        createBossExplosion();
        gameLayer.getChildren().remove(boss.getImageView());
        boss = null;

        // ลบข้อความแสดง HP ของบอส
        if (bossHpText != null) {
            uiLayer.getChildren().remove(bossHpText.mytext); // เปลี่ยนจาก root เป็น uiLayer
            bossHpText = null;
        }

        // เพิ่มคะแนนให้ผู้เล่น
        score.addAndGet(5000);
        scoreText.SetText("Score: " + score.get());

        // ตั้งค่าให้ไม่ใช่เลเวลบอสอีกต่อไป
        isBossLevel = false;
        isLevelTransitioning = false; // รีเซ็ตการเปลี่ยนเลเวล

        logger.info("Boss defeated! Current score: " + score.get());
    }


    private void showBossLevelCountdown(int level) {
        // สร้างข้อความแสดงเลเวลบอส
        nextLevelText = new TextClass("Boss Level", 430, 380, Color.RED, 60);
        uiLayer.getChildren().add(nextLevelText.mytext);

        // สร้างข้อความนับถอยหลัง
        countdownText = new TextClass("", 610, 450, Color.WHITE, 50);
        uiLayer.getChildren().add(countdownText.mytext);

        // หยุดการทำงานของเกมชั่วคราว
        gameLoop.stop();

        // เริ่มต้นการนับถอยหลัง
        new Thread(() -> {
            try {
                for (int i = 3; i > 0; i--) {
                    final int count = i;
                    Platform.runLater(() -> countdownText.SetText("" + count));
                    Thread.sleep(1000);
                }
                // เมื่อการนับถอยหลังเสร็จสิ้น
                Platform.runLater(() -> {
                    // ลบข้อความออกจากหน้าจอ
                    uiLayer.getChildren().removeAll(nextLevelText.mytext, countdownText.mytext);
                    // สร้างบอสใหม่
                    initializeBoss();
                    // เริ่มต้นเกมใหม่
                    gameLoop.start();
                    // รีเซ็ตการเปลี่ยนเลเวล
                    isLevelTransitioning = false;
                });
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e,"Failed to showBossLevelCountdown");
            }
        }).start();
    }

    private void handleAlienSpawning() {
        if (isBossLevel) return;

        int maxAliensToSpawn = calculateAliensToSpawn();

        if (alienSpawnCount < maxAliensToSpawn) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAlienSpawnTime > getAlienSpawnInterval()) {
                AlienShip alienShip = createAlienShip();
                alienShip.applyAcceleration(gameLevel);
                alienShipList.add(alienShip);
                gameLayer.getChildren().add(alienShip.getImageView()); // เปลี่ยนจาก root เป็น gameLayer
                alienSpawnCount++;
                lastAlienSpawnTime = currentTime;
            }
        }
    }
    public void destroyAlienShip(AlienShip alienShip) {
        alienShipList.remove(alienShip);
        gameLayer.getChildren().remove(alienShip.getImageView()); // เปลี่ยนจาก root เป็น gameLayer

        // สร้างแอนิเมชันการระเบิดของเอเลี่ยน
        createAlienExplosion(alienShip);

        // ปรับคะแนนเมื่อทำลายยานเอเลี่ยน
        score.addAndGet(1500);
        scoreText.SetText("Score: " + score.get());
    }

    private void handleAlienShooting() {
        if (isBossLevel) return;

        if (alienShipList.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        long alienShootInterval = Math.max(2000 - (gameLevel * 100), 500); // ลดเวลาลงตามเลเวล แต่ไม่ต่ำกว่า 500ms

        if (currentTime - lastAlienBulletTime > alienShootInterval) {
            for (AlienShip alienShip : alienShipList) {
                AlienBullet alienBullet = createAlienBullet(alienShip);
                alienBullet.applyAcceleration(5.0);
                alienBulletList.add(alienBullet);
                gameLayer.getChildren().add(alienBullet.getImageView()); // เปลี่ยนจาก root เป็น gameLayer

                // ลบกระสุนเอเลี่ยนหลังจาก 2 วินาที
                Timeline alienBulletRemove = new Timeline(new KeyFrame(Duration.seconds(4), event -> removeAlienBullet(alienBullet)));
                alienBulletRemove.play();
            }
            lastAlienBulletTime = currentTime;

        }
    }

    private AlienBullet createAlienBullet(AlienShip alienShip) {
        AlienBullet alienBullet = (AlienBullet) SpriteFactory.createEntity(GameCharacter.EntityType.ALIEN_BULLET, alienShip.getImageView().getTranslateX(), alienShip.getImageView().getTranslateY(),gameLevel);
        double angle;
        if (playerShip.getImageView().getTranslateX() < alienShip.getImageView().getTranslateX()) {
            angle = Math.toDegrees(Math.atan((playerShip.getImageView().getTranslateY() - alienShip.getImageView().getTranslateY()) /
                    (playerShip.getImageView().getTranslateX() - alienShip.getImageView().getTranslateX())));
            alienBullet.setRotation(angle - 180);
        } else {
            angle = Math.toDegrees(Math.atan((playerShip.getImageView().getTranslateY() - alienShip.getImageView().getTranslateY()) /
                    (playerShip.getImageView().getTranslateX() - alienShip.getImageView().getTranslateX())));
            alienBullet.setRotation(angle);
        }

        return alienBullet;
    }

    private void removeAlienBullet(AlienBullet alienBullet) {
        alienBulletList.remove(alienBullet);
        gameLayer.getChildren().remove(alienBullet.getImageView());

    }
    private long getAlienSpawnInterval() {
        // ตัวอย่าง: สปาวน์เอเลี่ยนทุก ๆ 5 วินาที
        return 5000; // หน่วยเป็นมิลลิวินาที
    }

    private int calculateAliensToSpawn() {
        // ตัวอย่าง: สปาวน์เอเลี่ยนจำนวนเท่ากับเลเวล สูงสุดไม่เกิน 5 ลำ
        return Math.min(gameLevel, 5);
    }

    public void handlePlayerStatus() {
        if (!playerShip.isAlive() && !playerLives.isEmpty()) {
            youDiedText.mytext.setOpacity(1);
        } else {
            youDiedText.mytext.setOpacity(0);
        }

        if (pressedKeys.getOrDefault(KeyCode.ENTER, false) && !playerShip.isAlive()) {
            if (playerLives.isEmpty()) {
                gameLoop.stop();
                AsteroidsGame.getInstance().showHighScores();
                logger.info("No remaining lives. Game Over.");

            } else {
                respawnPlayer();
                logger.info("Respawning player.");

            }
        }

        if (pressedKeys.getOrDefault(KeyCode.L, false) && playerShip.isAlive() && playerLives.size() < 3 && score.get() >= 10000 && !replenishedLife.get()) {
            replenishLife();
            logger.info("Life replenished.");

        }
    }

    private void respawnPlayer() {
        playerShip.getImageView().setTranslateX(600);
        playerShip.getImageView().setTranslateY(400);
        playerShip.halt();
        playerShip.setRotation(0);
        playerShip.alive = true;

        if (!gameLayer.getChildren().contains(playerShip.getImageView())) {
            gameLayer.getChildren().add(playerShip.getImageView());
        }

        youDiedText.mytext.setOpacity(0);
        isImmune.set(true);
        playerShip.setImmune(true);

        Timeline immuneTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            isImmune.set(false);
            playerShip.setImmune(false);
            logger.debug("Player is no longer immune.");
        }));
        immuneTimeline.play();
    }

    public void replenishLife() {
        Image lifeImage = new Image(getClass().getResourceAsStream("/sprites/life_icon.png"));
        ImageView lifeIcon = new ImageView(lifeImage);

        // ปรับขนาดของไอคอนชีวิต
        lifeIcon.setFitWidth(30);
        lifeIcon.setFitHeight(30);

        // ตั้งตำแหน่งของไอคอนชีวิต
        lifeIcon.setTranslateX(950 + 35 * playerLives.size());
        lifeIcon.setTranslateY(40);

        playerLives.add(lifeIcon);
        uiLayer.getChildren().add(lifeIcon);
        replenishedLife.set(true);
        score.addAndGet(-10000);
        scoreText.SetText("Score: " + score.get());
        System.out.println("Life added. Total lives: " + playerLives.size());
    }



    public void setKeyPressed(KeyCode keyCode, boolean isPressed) {
        pressedKeys.put(keyCode, isPressed);
    }


    public void handleInput() {
        if (playerShip.isAlive()) {
            if (pressedKeys.getOrDefault(KeyCode.W, false)) {
                playerShip.applyAcceleration(0.8);
                if (!isAccelerating.get()) {
                    isAccelerating.set(true);
                    logger.info("Player accelerated forward.");
                }
            } else {
                if (isAccelerating.get()) {
                    isAccelerating.set(false);
                    logger.info("Player stopped accelerating forward.");
                }
            }

            if (pressedKeys.getOrDefault(KeyCode.S, false)) {
                playerShip.applyAcceleration(-1.5);
                if (!isDecelerating.get()) {
                    isDecelerating.set(true);
                    logger.info("Player decelerated.");
                }
            } else {
                if (isDecelerating.get()) {
                    isDecelerating.set(false);
                    logger.info("Player stopped decelerating.");
                }
            }

            if (pressedKeys.getOrDefault(KeyCode.A, false)) {
                playerShip.strafeLeft(0.50);
                if (!isStrafingLeft.get()) {
                    isStrafingLeft.set(true);
                    logger.info("Player strafed left.");
                }
            } else {
                if (isStrafingLeft.get()) {
                    isStrafingLeft.set(false);
                    logger.info("Player stopped strafing left.");
                }
            }

            if (pressedKeys.getOrDefault(KeyCode.D, false)) {
                playerShip.strafeRight(0.50);
                if (!isStrafingRight.get()) {
                    isStrafingRight.set(true);
                    logger.info("Player strafed right.");
                }
            } else {
                if (isStrafingRight.get()) {
                    isStrafingRight.set(false);
                    logger.info("Player stopped strafing right.");
                }
            }

            if (pressedKeys.getOrDefault(KeyCode.C, false) && !didHyperJump.get()) {
                performHyperJump();
                if (!isHyperJumping.get()) {
                    isHyperJumping.set(true);
                    logger.info("Player performed a kamui.");
                }
            } else {
                if (isHyperJumping.get()) {
                    isHyperJumping.set(false);
                    logger.info("Player stopped kamui.");
                }
            }
        }
    }

    private void performHyperJump() {
        playerShip.hyperjump();
        didHyperJump.set(true);
        isImmune.set(true);
        playerShip.setImmune(true);

        // Player is immune for 2 seconds after hyperjump
        Timeline immuneTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            isImmune.set(false);
            playerShip.setImmune(false);
            logger.debug("Player is no longer immune after kamui.");
        }));
        immuneTimeline.play();
        System.out.println("Player performed kamui.");
    }

    private void updateEntities() {
        // Move player ship
        playerShip.applyMove(1280, 832);

        // Move asteroids
        asteroidList.forEach(asteroid -> asteroid.applyMove(1280, 832));

        // Move alien ships
        alienShipList.forEach(alienShip -> alienShip.applyMove(1280, 832));

        // Move alien bullets
        alienBulletList.forEach(alienBullet -> alienBullet.applyMove(1280, 832));

        // Move boss bullets
        bossBulletList.forEach(bossBullet -> bossBullet.applyMove(1280, 832));

        // Move player bullets
        bulletList.forEach(bullet -> bullet.applyMove(1280, 832));

        // Move bomb bullets
        bombBulletList.forEach(bombBullet -> bombBullet.applyMove(1280, 832));

        // Move items
        itemList.forEach(item -> item.applyMove(1280, 832));

        // Move boss left and right
        if (boss != null && boss.isAlive()) {
            boss.moveLeftRight(1280); // ส่งความกว้างของหน้าจอ
        }
    }

    public void handleCollisions() {
        handlePlayerCollisions();
        handleBulletAsteroidCollisions();
        handleBulletAlienCollisions();
        handleBombBulletCollisions();
        handleItemCollection();
        handleBossCollisions();
        handleBossBulletCollisions(); // เพิ่มการตรวจสอบการชนกับกระสุนบอส
    }

    public void handleBossBulletCollisions() {
        if (!isImmune.get() && playerShip.isAlive()) {
            List<BossBullet> bulletsToRemove = new ArrayList<>();
            for (BossBullet bossBullet : bossBulletList) {
                if (playerShip.collision(bossBullet)) {
                    bulletsToRemove.add(bossBullet);
                    removeLife();
                    logger.info("Player hit by Boss Bullet.");
                    break;
                }
            }
            for (BossBullet bossBullet : bulletsToRemove) {
                bossBulletList.remove(bossBullet);
                gameLayer.getChildren().remove(bossBullet.getImageView()); // เปลี่ยนจาก root เป็น gameLayer
            }
        }
    }


    public void handleBossCollisions() {
        if (boss != null && boss.isAlive()) {
            Boss currentBoss = boss; // ใช้ตัวแปรท้องถิ่น
            List<Bullet> bulletsToRemove = new ArrayList<>();
            for (Bullet bullet : bulletList) {
                if (currentBoss.collision(bullet)) {
                    bulletsToRemove.add(bullet);
                    currentBoss.takeDamage(1);
                    bossHpText.SetText("Boss HP: " + currentBoss.getHp());
                    logger.info("Boss hit! Remaining HP: " + currentBoss.getHp());

                    createBossHitAnimation(bullet.getImageView().getTranslateX(), bullet.getImageView().getTranslateY());

                    if (!currentBoss.isAlive()) {
                        destroyBoss();
                        break;
                    }
                }
            }
            bulletsToRemove.forEach(this::removeBullet);

            // Collision between boss and bomb bullets
            List<BombBullet> bombsToRemove = new ArrayList<>();
            for (BombBullet bombBullet : bombBulletList) {
                if (currentBoss.collision(bombBullet)) {
                    bombsToRemove.add(bombBullet);
                    currentBoss.takeDamage(5); // ลด HP ของบอสลงมากขึ้นเมื่อโดนระเบิด
                    bossHpText.SetText("Boss HP: " + currentBoss.getHp());
                    logger.info("Boss hit by bomb! Remaining HP: " + currentBoss.getHp());

                    createBossHitAnimation(bombBullet.getImageView().getTranslateX(), bombBullet.getImageView().getTranslateY());

                    if (!currentBoss.isAlive()) {
                        destroyBoss();
                        break;
                    }
                }
            }
            bombsToRemove.forEach(bombBullet -> {
                bombBulletList.remove(bombBullet);
                gameLayer.getChildren().remove(bombBullet.getImageView());
            });

            // Collision between boss and player
            if (playerShip.isAlive() && currentBoss.collision(playerShip) && !isImmune.get()) {
                logger.info("Player collided with Boss.");
                removeLife();

                // สร้างเอฟเฟกต์การระเบิดของผู้เล่น
                createPlayerExplosion();

                // สร้างเอฟเฟกต์การถูกตีของบอสที่ตำแหน่งของผู้เล่น
                createBossHitAnimation(playerShip.getImageView().getTranslateX(), playerShip.getImageView().getTranslateY());
            }
        }

        // **เพิ่มการตรวจสอบสถานะบอสหลังจากการจัดการการชนกัน**
        if (boss != null && !boss.isAlive()) {
            destroyBoss();
        }
    }

    private void handleBombBulletCollisions() {
        List<BombBullet> bombsToExplode = new ArrayList<>();
        for (BombBullet bombBullet : bombBulletList) {
            // ตรวจสอบการชนกับอุกกาบาต
            for (AsteroidClass asteroid : asteroidList) {
                if (bombBullet.collision(asteroid)) {
                    bombsToExplode.add(bombBullet);
                    break;
                }
            }
            // ตรวจสอบการชนกับเอเลี่ยน
            for (AlienShip alienShip : alienShipList) {
                if (bombBullet.collision(alienShip)) {
                    bombsToExplode.add(bombBullet);
                    break;
                }
            }
        }
        for (BombBullet bombBullet : bombsToExplode) {
            bombBullet.explode(gameLayer, asteroidList, this, playerShip);
            // ลบกระสุนระเบิดออกจากเกม
            removeBombBullet(bombBullet);
            // นำยานของผู้เล่นไปไว้ด้านหน้าสุดหลังจากเกิดการระเบิด
            playerShip.getImageView().toFront();
        }
    }

    public List<AlienShip> getAlienShipList() {
        return alienShipList;
    }

    public void handlePlayerCollisions() {
        if (!isImmune.get() && playerShip.isAlive()) {
            // Collision with Alien Ships
            for (AlienShip alienShip : alienShipList) {
                if (playerShip.collision(alienShip)) {
                    logger.info("Player collided with Alien Ship.");
                    removeLife();
                    // Optionally, remove the alien ship
                    destroyAlienShip(alienShip);
                    break;
                }
            }

            // Collision with Asteroids
            for (AsteroidClass asteroid : asteroidList) {
                if (playerShip.collision(asteroid)) {
                    logger.info("Player collided with Asteroid.");
                    removeLife();
                    // Optionally, remove the asteroid or handle accordingly
                    destroyAsteroid(asteroid, false);
                    break;
                }
            }

            // Collision with Alien Bullets
            for (AlienBullet alienBullet : alienBulletList) {
                if (playerShip.collision(alienBullet)) {
                    logger.info("Player hit by Alien Bullet.");
                    removeLife();
                    // Remove the bullet after collision
                    removeAlienBullet(alienBullet);
                    break;
                }
            }
        }
    }
    public List<AsteroidClass> getAsteroidList() {
        return asteroidList;
    }

    public List<AlienBullet> getAlienBulletList() {
        return alienBulletList;
    }
    public void setYouDiedText(TextClass youDiedText) {
        this.youDiedText = youDiedText;
    }


    public void removeLife() {
        if (!playerLives.isEmpty()) {
            ImageView lastLifeIcon = playerLives.remove(playerLives.size() - 1);
            uiLayer.getChildren().remove(lastLifeIcon);
            logger.debug("Removed life icon at position: (" + lastLifeIcon.getTranslateX() + ", " + lastLifeIcon.getTranslateY() + ")");

            // Create explosion effect
            createPlayerExplosion();

            playerShip.setAlive(false); // ใช้เมธอด setter แทนการตั้งค่าโดยตรง
            logger.debug("Player marked as not alive.");

            // Remove the player's ImageView from the game layer
            if (gameLayer.getChildren().contains(playerShip.getImageView())) {
                gameLayer.getChildren().remove(playerShip.getImageView());
                logger.debug("Player's ImageView removed from game layer.");
            }

            youDiedText.mytext.setOpacity(1);
            logger.info("Player lost a life. Remaining lives: " + playerLives.size());
        }
    }


    public void setRoot(Group root) {
        this.root = root;
    }

    public void handleBulletAsteroidCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<AsteroidClass> asteroidsToRemove = new ArrayList<>();

        for (Bullet bullet : bulletList) {
            for (AsteroidClass asteroid : asteroidList) {
                if (asteroid.collision(bullet)) {
                    logger.info("Bullet hit asteroid at position: (" + asteroid.getImageView().getTranslateX() + ", " + asteroid.getImageView().getTranslateY() + ")");
                    bulletsToRemove.add(bullet);
                    asteroidsToRemove.add(asteroid);
                    break;
                }
            }
        }

        bulletsToRemove.forEach(this::removeBullet);
        asteroidsToRemove.forEach(asteroid -> destroyAsteroid(asteroid, false)); // ส่ง false เนื่องจากไม่ได้ถูกทำลายโดยกระสุนระเบิด
    }
    public Group getRoot() {
        return root;
    }
    public List<Bullet> getBulletList() {
        return bulletList;
    }

    public void handleBulletAlienCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<AlienShip> shipsToRemove = new ArrayList<>();

        for (Bullet bullet : bulletList) {
            for (AlienShip alienShip : alienShipList) {
                if (alienShip.collision(bullet)) {
                    logger.info("Bullet hit alien ship!");
                    bulletsToRemove.add(bullet);
                    shipsToRemove.add(alienShip);
                    break;
                }
            }
        }

        bulletsToRemove.forEach(this::removeBullet);
        shipsToRemove.forEach(this::destroyAlienShip); // เรียกใช้ destroyAlienShip
    }
    public List<Item> getItemList() {
        return itemList;
    }

    public void handleItemCollection() {
        List<Item> itemsToRemove = new ArrayList<>();

        for (Item item : itemList) {
            if (playerShip.collision(item)) {

                itemsToRemove.add(item);
                logger.info("Player collected an item of type: " + item.getItemType());

                switch (item.getItemType()) {
                    case SHOTGUN_AMMO:
                        playerShip.activateShotgunMode();
                        logger.info("Shotgun mode activated.");
                        break;
                    case LIFE_POTION:
                        increasePlayerLife();
                        logger.info("Life potion collected. Total lives: " + playerLives.size());
                        break;
                    case BOMB_AMMO:
                        playerShip.activateBombMode();
                        logger.info("Bomb mode activated.");
                        break;
                    // เพิ่มกรณีสำหรับไอเท็มอื่น ๆ ที่นี่
                }
            }


        }
        itemsToRemove.forEach(item -> {
            itemList.remove(item);
            uiLayer.getChildren().remove(item.getImageView()); // เปลี่ยนจาก root เป็น uiLayer
        });
    }

    private void increasePlayerLife() {
        if (playerLives.size() < 3) { // กำหนดจำนวนชีวิตสูงสุดที่ 3
            Image lifeImage = new Image(getClass().getResourceAsStream("/sprites/life_icon.png"));
            ImageView lifeIcon = new ImageView(lifeImage);

            // ปรับขนาดของไอคอนชีวิต
            lifeIcon.setFitWidth(30); // ปรับความกว้างตามที่คุณต้องการ
            lifeIcon.setFitHeight(30); // ปรับความสูงตามที่คุณต้องการ

            // ตั้งตำแหน่งของไอคอนชีวิต
            lifeIcon.setTranslateX(950 + 35 * playerLives.size());
            lifeIcon.setTranslateY(40);

            playerLives.add(lifeIcon);
            uiLayer.getChildren().add(lifeIcon);

            logger.info("Life increased. Total lives: " + playerLives.size());

        } else {
            logger.debug("Player already has maximum lives.");
        }
    }

    public void destroyAsteroid(AsteroidClass asteroid, boolean destroyedByBomb) {
        asteroidList.remove(asteroid);
        gameLayer.getChildren().remove(asteroid.getImageView());

        int scoreIncrement = 0;
        String asteroidSize = "";
        switch (asteroid.entityType) {
            case LARGE_ASTEROID:
                scoreIncrement = 1000;
                asteroidSize = "Large";
                break;
            case MEDIUM_ASTEROID:
                scoreIncrement = 500;
                asteroidSize = "Medium";
                break;
            case SMALL_ASTEROID:
                scoreIncrement = 250;
                asteroidSize = "Small";
                break;
        }
        score.addAndGet(scoreIncrement);
        scoreText.SetText("Score: " + score.get());
        logger.info("Asteroid destroyed. Size: " + asteroidSize + ". Score increased by " + scoreIncrement + ". Current score: " + score.get());

        if (asteroidSize.equals("")) {
            logger.warn("Asteroid size not identified.");
        }

        // ตรวจสอบว่าถูกทำลายโดยกระสุนระเบิดหรือไม่
        if (!destroyedByBomb) {
            if (asteroid.entityType == GameCharacter.EntityType.LARGE_ASTEROID) {
                try {
                    splitAsteroid(asteroid, GameCharacter.EntityType.MEDIUM_ASTEROID, 2);
                } catch (Exception e) {
                    ExceptionHandler.handle(e, "Failed to split large asteroid into medium asteroids.");
                }
            } else if (asteroid.entityType == GameCharacter.EntityType.MEDIUM_ASTEROID) {
                try {
                    splitAsteroid(asteroid, GameCharacter.EntityType.SMALL_ASTEROID, 2);
                } catch (Exception e) {
                    ExceptionHandler.handle(e, "Failed to split medium asteroid into small asteroids.");
                }
            }
        }

        createExplosion(asteroid);
        logger.debug("Explosion animation created for asteroid at ("
                + asteroid.getImageView().getTranslateX() + ", "
                + asteroid.getImageView().getTranslateY() + ")");
    }

    public void splitAsteroid(AsteroidClass originalAsteroid, GameCharacter.EntityType newType, int count) {
        for (int i = 0; i < count; i++) {
            double newScale = 1.5; // สำหรับ MEDIUM_ASTEROID
            if (newType == GameCharacter.EntityType.SMALL_ASTEROID) {
                newScale = 1.0; // สำหรับ SMALL_ASTEROID
            }
            AsteroidClass newAsteroid = new AsteroidClass(newType,
                    originalAsteroid.getImageView().getTranslateX(),
                    originalAsteroid.getImageView().getTranslateY(),
                    newScale);
            double newAngle = originalAsteroid.getAngle() + (-90 * i + 45);
            newAsteroid.setRotation(newAngle);
            newAsteroid.applyAcceleration(2);
            asteroidList.add(newAsteroid);
            gameLayer.getChildren().add(newAsteroid.getImageView());

        }
    }

    private void createExplosion(AsteroidClass asteroid) {
        Explosion explosion = new Explosion(
                asteroid.getImageView().getTranslateX(),
                asteroid.getImageView().getTranslateY(),
                "/sprites/explosion.png",
                14,
                14,
                65,
                65,
                Duration.millis(400)
        );
        gameLayer.getChildren().add(explosion);

    }

    private void createAlienExplosion(AlienShip alienShip) {
        AlienExplosion alienExplosion = new AlienExplosion(
                alienShip.getImageView().getTranslateX(),
                alienShip.getImageView().getTranslateY()
        );
        gameLayer.getChildren().add(alienExplosion);
       logger.debug("Alien explosion created at alien ship position.");
    }

    private void createPlayerExplosion() {
        try {


            PlayerExplosion playerExplosion = new PlayerExplosion(
                    playerShip.getImageView().getTranslateX(),
                    playerShip.getImageView().getTranslateY()
            );
            gameLayer.getChildren().add(playerExplosion); // เปลี่ยนจาก root เป็น gameLayer
            logger.debug("Player explosion created at player position.");
        }
        catch (NullPointerException e) {
            ExceptionHandler.handle(e,"Failed to create player explosion:");

        }
    }

    public void handleGameOver() {
        if (playerLives.isEmpty() && !isGameOver.get()) {
            gameLoop.stop();
            isGameOver.set(true);
            logger.info("Game Over!");

            saveHighScore();

            gameOverScore.SetText("Final Score: " + score.get());

            gameOverTitle.mytext.setOpacity(1);
            gameOverScore.mytext.setOpacity(1);
            gameOverInstruction.mytext.setOpacity(1);
            youDiedText.mytext.setOpacity(0);
        }
    }
    private void setupRestartHandler() {
        gameOverPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) { // Press Enter to restart
                resetGame();
            }
        });
    }


    public void resetGame() {

        if (gameLayer == null || uiLayer == null) {
            logger.error("Game layers are not initialized. Cannot reset the game.");
            return;
        }

        // หยุดเกมลูป
        if (gameLoop != null) {
            gameLoop.stop();
            logger.debug("Game loop stopped.");
        }

        // เคลียร์ gameLayer และ uiLayer
        gameLayer.getChildren().clear();
        uiLayer.getChildren().clear();
        logger.debug("Cleared gameLayer and uiLayer.");

        // เพิ่มภาพพื้นหลังกลับเข้าไป
        addBackground();
        logger.debug("Background added.");

        // รีเซ็ตสถานะเกม
        playerShip = null;
        initializePlayer();
        logger.debug("Player initialized.");

        score.set(0);
        gameLevel = 1;
        initializeUI();
        logger.debug("UI initialized.");

        playerLives.clear();
        initializeLives();
        logger.debug("Player lives initialized.");

        // เคลียร์ลิสต์เอนทิตีทั้งหมด
        asteroidList.clear();
        alienShipList.clear();
        alienBulletList.clear();
        bulletList.clear();
        itemList.clear();
        bombBulletList.clear();
        bossBulletList.clear();
        pressedKeys.clear();
        logger.debug("Cleared all entity lists.");

        // รีเซ็ตแฟล็กและเคาน์เตอร์
        isAlienSpawned = false;
        lastAlienBulletTime = 0;
        didHyperJump.set(false);
        replenishedLife.set(false);
        isGameOver.set(false);
        isImmune.set(false);
        lastItemSpawnTime = 0;
        alienSpawnCount = 0;
        lastAlienSpawnTime = 0;
        isBossLevel = false;
        logger.debug("Flags and counters reset.");

        // รีอินิทไลซ์อุกกาบาตและเริ่มเกมลูปใหม่
        initializeAsteroids();
        logger.debug("Asteroids initialized.");

        startGameLoop();
        logger.debug("Game loop started.");

        logger.info("Game has been reset and game loop started.");
    }


    public void spawnRandomItem() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastItemSpawnTime > 15000) { // สุ่มสร้างทุก ๆ 15 วินาที
            Random random = new Random();
            double x = random.nextDouble() * 1280;
            double y = random.nextDouble() * 832;

            Item.ItemType[] itemTypes = Item.ItemType.values();
            Item.ItemType randomItemType = itemTypes[random.nextInt(itemTypes.length)];

            Item item = new Item(randomItemType, x, y);
            itemList.add(item);
            uiLayer.getChildren().add(item.getImageView()); // เปลี่ยนจาก root เป็น uiLayer
            lastItemSpawnTime = currentTime;
        }
    }

    private AsteroidClass createLargeAsteroid() {
        double positionX;
        double positionY;
        Random random = new Random();
        int spawnSide = random.nextInt(4) + 1;

        switch (spawnSide) {
            case 1:
                positionX = random.nextInt(60) - 60;
                positionY = random.nextInt(832);
                break;
            case 2:
                positionX = random.nextInt(1280);
                positionY = random.nextInt(60) + 832;
                break;
            case 3:
                positionX = random.nextInt(60) + 1280;
                positionY = random.nextInt(832);
                break;
            default:
                positionX = random.nextInt(1280);
                positionY = random.nextInt(60) - 60;
                break;
        }

        AsteroidClass asteroid = (AsteroidClass) SpriteFactory.createEntity(GameCharacter.EntityType.LARGE_ASTEROID, positionX, positionY,gameLevel);
        asteroid.setRotation(random.nextDouble() * 360);
        asteroid.applyAcceleration(1);
        return asteroid;
    }

    private AlienShip createAlienShip() {
        double positionX;
        double positionY;
        Random random = new Random();
        int spawnSide = random.nextInt(4) + 1;

        switch (spawnSide) {
            case 1:
                positionX = random.nextInt(60);
                positionY = random.nextInt(832);
                break;
            case 2:
                positionX = random.nextInt(1280);
                positionY = 832;
                break;
            case 3:
                positionX = 1280;
                positionY = random.nextInt(832);
                break;
            default:
                positionX = random.nextInt(1280);
                positionY = random.nextInt(60);
                break;
        }

        AlienShip alienShip = (AlienShip) SpriteFactory.createEntity(GameCharacter.EntityType.ALIEN_SHIP, positionX, positionY,gameLevel);
        alienShip.setRotation(random.nextDouble() * 360);

        // ปรับความเร็วของเอเลี่ยนตามเลเวล
        double alienSpeed = 1 + (gameLevel * 0.1); // เพิ่มความเร็ว 0.2 ต่อเลเวล
        alienShip.applyAcceleration(alienSpeed);
        alienShip.applyMove(1280, 832);
        return alienShip;
    }
    public PlayerShip getPlayerShip() {
        return playerShip;
    }

    private void createBossHitAnimation(double x, double y) {
        BossHitAnimation hitAnimation = new BossHitAnimation(x, y);
        gameLayer.getChildren().add(hitAnimation);
        hitAnimation.toFront();
    }

    public void setIsImmune(AtomicBoolean b) {
        this.isImmune = b;
    }

    public Boss getBoss() {
            return boss;
    }

    // เพิ่มเมธอด getter สำหรับ gameLayer
    public Group getGameLayer() {
        return this.gameLayer;
    }

}