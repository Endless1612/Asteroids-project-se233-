import com.example.iceteriod.controller.*;
import com.example.iceteriod.controller.AsteroidClass;
import com.example.iceteriod.model.GameLogic;
import com.example.iceteriod.view.TextClass;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import com.example.Exception.ExceptionHandler;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameLogicTest {

    private GameLogic gameLogic;
    private PlayerShip playerShip;
    private Group realRoot;
    private Group realGameLayer;
    private Group realUiLayer;

    @BeforeAll
    static void initJavaFX() {
        Platform.startup(() -> {
        });
    }

    @BeforeEach
    void setUp() throws Exception {

        ExceptionHandler mockExceptionHandler = mock(ExceptionHandler.class);
        ExceptionHandler.setHandlerInstance(mockExceptionHandler);

        gameLogic = new GameLogic();

        realRoot = new Group();
        realGameLayer = new Group();
        realUiLayer = new Group();

        gameLogic.setRoot(realRoot);
        gameLogic.setGameLayer(realGameLayer);
        gameLogic.setUiLayer(realUiLayer);

        realRoot.getChildren().addAll(realGameLayer, realUiLayer);

        // Initialize UI elements
        TextClass mockScoreText = new TextClass("Score: 0", 30, 50, Color.WHITE, 40);
        gameLogic.setScoreText(mockScoreText);

        // Initialize youDiedText to avoid NullPointerException
        TextClass mockYouDiedText = new TextClass("You Died", 200, 300, Color.RED, 80);
        gameLogic.setYouDiedText(mockYouDiedText);

        // Initialize player lives and player in the game
        runAndCatch(() -> {
            gameLogic.initializeLives();
            gameLogic.initializePlayer();
        });

        // Retrieve the PlayerShip instance from GameLogic
        playerShip = gameLogic.getPlayerShip();
    }


    private void runAndCatch(Runnable runnable) throws Exception {
        AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                exceptionRef.set(t);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
        if (exceptionRef.get() != null) {
            if (exceptionRef.get() instanceof Exception) {
                throw (Exception) exceptionRef.get();
            } else {
                throw new RuntimeException(exceptionRef.get());
            }
        }
    }

    @Test
    void testInitializePlayer() throws Exception {
        runAndCatch(() -> {
            gameLogic.initializePlayer();

            assertNotNull(gameLogic.getPlayerShip(), "PlayerShip should be initialized.");
            assertTrue(realGameLayer.getChildren().contains(gameLogic.getPlayerShip().getImageView()),
                    "PlayerShip's ImageView should be added to the game layer.");
        });
    }

    @Test
    void testScoreIncreaseAfterDestroyingAsteroid() throws Exception {
        runAndCatch(() -> {
            int initialScore = gameLogic.getScore().get();
            AsteroidClass asteroid = new AsteroidClass(GameCharacter.EntityType.LARGE_ASTEROID, 100, 100, 1.5);
            gameLogic.getAsteroidList().add(asteroid);
            realGameLayer.getChildren().add(asteroid.getImageView());

            gameLogic.destroyAsteroid(asteroid, false);

            int updatedScore = gameLogic.getScore().get();
            assertTrue(updatedScore > initialScore, "Score should increase after destroying an asteroid.");
            assertFalse(gameLogic.getAsteroidList().contains(asteroid),
                    "Asteroid should be removed from the asteroid list.");
            assertFalse(realGameLayer.getChildren().contains(asteroid.getImageView()),
                    "Asteroid's ImageView should be removed from the game layer.");
        });
    }

    @Test
    void testReplenishLife() throws Exception {
        runAndCatch(() -> {
            int initialLives = gameLogic.getPlayerLives().size();
            gameLogic.replenishLife();
            int updatedLives = gameLogic.getPlayerLives().size();
            assertEquals(initialLives + 1, updatedLives, "Player lives should increase by 1.");
            // ตรวจสอบว่าไอคอนชีวิตถูกเพิ่มใน UI layer
            assertTrue(realUiLayer.getChildren().contains(gameLogic.getPlayerLives().get(updatedLives - 1)),
                    "Life icon should be added to the UI layer.");
        });
    }

    @Test
    void testPlayerCollisionWithAlien() throws Exception {
        runAndCatch(() -> {
            // สร้างและเพิ่ม AlienShip
            AlienShip alienShip = new AlienShip(GameCharacter.EntityType.ALIEN_SHIP, 200, 200, 1.0);
            gameLogic.getAlienShipList().add(alienShip);
            realGameLayer.getChildren().add(alienShip.getImageView());

            // ตั้งตำแหน่งผู้เล่นให้ชนกับ AlienShip
            playerShip.getImageView().setTranslateX(200);
            playerShip.getImageView().setTranslateY(200);

            // ตรวจสอบให้แน่ใจว่าผู้เล่นยังมีชีวิตอยู่และมีชีวิต
            playerShip.alive = true;
            gameLogic.replenishLife(); // ใช้เมธอด replenishLife แทนการเพิ่มโดยตรง
            int initialLives = gameLogic.getPlayerLives().size();

            // เรียกใช้การจัดการการชน
            gameLogic.handlePlayerCollisions();

            // ตรวจสอบว่าไลฟ์ถูกลดลง
            int updatedLives = gameLogic.getPlayerLives().size();
            assertEquals(initialLives - 1, updatedLives, "Player's lives should decrease by 1 when colliding with an alien ship.");
            assertFalse(gameLogic.getAlienShipList().contains(alienShip),
                    "Alien ship should be removed from the alien ship list.");
            assertFalse(realGameLayer.getChildren().contains(alienShip.getImageView()),
                    "Alien ship's ImageView should be removed from the game layer.");
        });
    }

    @Test
    void testPlayerCollisionWithAsteroid() throws Exception {
        runAndCatch(() -> {
            AsteroidClass asteroid = new AsteroidClass(GameCharacter.EntityType.LARGE_ASTEROID, 300, 300, 1.5);
            gameLogic.getAsteroidList().add(asteroid);
            realGameLayer.getChildren().add(asteroid.getImageView());

            // ตั้งตำแหน่งผู้เล่นและอุกกาบาตให้ชนกัน
            playerShip.getImageView().setTranslateX(300);
            playerShip.getImageView().setTranslateY(300);

            // เพิ่มไลฟ์สำหรับผู้เล่น
            gameLogic.replenishLife();
            int initialLives = gameLogic.getPlayerLives().size();

            gameLogic.handlePlayerCollisions();

            int updatedLives = gameLogic.getPlayerLives().size();
            assertEquals(initialLives - 1, updatedLives, "Player's lives should decrease by 1 when colliding with an asteroid.");
            assertFalse(gameLogic.getAsteroidList().contains(asteroid),
                    "Asteroid should be removed after collision.");
            assertFalse(realGameLayer.getChildren().contains(asteroid.getImageView()),
                    "Asteroid's ImageView should be removed from the game layer.");
        });
    }

    @Test
    void testPlayerCollisionWithAlienBullet() throws Exception {
        runAndCatch(() -> {
            AlienBullet alienBullet = new AlienBullet(GameCharacter.EntityType.ALIEN_BULLET, 400, 400);
            gameLogic.getAlienBulletList().add(alienBullet);
            realGameLayer.getChildren().add(alienBullet.getImageView());

            // ตั้งตำแหน่งผู้เล่นและกระสุนให้ชนกัน
            playerShip.getImageView().setTranslateX(400);
            playerShip.getImageView().setTranslateY(400);

            // เพิ่มไลฟ์สำหรับผู้เล่น
            gameLogic.replenishLife();
            int initialLives = gameLogic.getPlayerLives().size();

            // ตรวจสอบให้แน่ใจว่าผู้เล่นยังมีชีวิตอยู่
            playerShip.alive = true;

            // เรียกใช้การจัดการการชน
            gameLogic.handlePlayerCollisions();

            // ตรวจสอบว่าไลฟ์ถูกลดลง
            int updatedLives = gameLogic.getPlayerLives().size();
            assertEquals(initialLives - 1, updatedLives, "Player's lives should decrease by 1 when hit by an alien bullet.");
            assertFalse(gameLogic.getAlienBulletList().contains(alienBullet),
                    "Alien bullet should be removed after collision.");
            assertFalse(realGameLayer.getChildren().contains(alienBullet.getImageView()),
                    "Alien bullet's ImageView should be removed from the game layer.");
        });
    }

    @Test
    void testPlayerMoveForwardWithWKey() throws Exception {
        runAndCatch(() -> {
            // Initial move vector
            Point2D initialMove = playerShip.getMove();
            System.out.println("Initial Move: " + initialMove);

            // Simulate pressing the W key
            gameLogic.setKeyPressed(KeyCode.W, true);
            gameLogic.handleInput();

            // Apply move (simulate a game loop tick)
            playerShip.applyMove(1280, 832);

            // Get updated move vector
            Point2D updatedMove = playerShip.getMove();
            System.out.println("Updated Move: " + updatedMove);

            // Verify that the move vector has changed
            assertNotEquals(initialMove, updatedMove, "Player's acceleration should change when W is pressed.");

            // ตรวจสอบทิศทางการเคลื่อนที่
            assertTrue(updatedMove.getY() < initialMove.getY() || updatedMove.getX() > initialMove.getX(),
                    "Player should accelerate forward when W is pressed.");
        });
    }

    @Test
    void testPlayerMoveBackwardWithSKey() throws Exception {
        runAndCatch(() -> {
            // Initial move vector
            Point2D initialMove = playerShip.getMove();
            System.out.println("Initial Move: " + initialMove);

            // Simulate pressing the S key
            gameLogic.setKeyPressed(KeyCode.S, true);
            gameLogic.handleInput();

            // Apply move (simulate a game loop tick)
            playerShip.applyMove(1280, 832);

            // Get updated move vector
            Point2D updatedMove = playerShip.getMove();
            System.out.println("Updated Move: " + updatedMove);

            // Verify that the move vector has changed
            assertNotEquals(initialMove, updatedMove, "Player's acceleration should change when S is pressed.");

            // ตรวจสอบทิศทางการเคลื่อนที่
            assertTrue(updatedMove.getY() > initialMove.getY() || updatedMove.getX() < initialMove.getX(),
                    "Player should decelerate or move backward when S is pressed.");
        });
    }

    @Test
    void testPlayerMoveLeftWithAKey() throws Exception {
        runAndCatch(() -> {
            // Initial move vector
            Point2D initialMove = playerShip.getMove();

            // Simulate pressing the A key
            gameLogic.setKeyPressed(KeyCode.A, true);
            gameLogic.handleInput();

            // Apply move (simulate a game loop tick)
            playerShip.applyMove(1280, 832);

            // Get updated move vector
            Point2D updatedMove = playerShip.getMove();

            // Verify that the move vector has changed
            assertNotEquals(initialMove, updatedMove, "Player's acceleration should change when A is pressed.");

            // ตรวจสอบทิศทางการเคลื่อนที่
            assertTrue(updatedMove.getX() < initialMove.getX(),
                    "Player should move left when A is pressed.");
        });
    }

    @Test
    void testPlayerMoveRightWithDKey() throws Exception {
        runAndCatch(() -> {
            // Initial move vector
            Point2D initialMove = playerShip.getMove();
            System.out.println("Initial Move: " + initialMove);

            // Simulate pressing the D key
            gameLogic.setKeyPressed(KeyCode.D, true);
            gameLogic.handleInput();

            // Apply move (simulate a game loop tick)
            playerShip.applyMove(1280, 832);

            // Get updated move vector
            Point2D updatedMove = playerShip.getMove();
            System.out.println("Updated Move: " + updatedMove);

            // Verify that the move vector has changed
            assertNotEquals(initialMove, updatedMove, "Player's acceleration should change when D is pressed.");

            // ตรวจสอบทิศทางการเคลื่อนที่
            assertTrue(updatedMove.getX() > initialMove.getX(),
                    "Player should move right when D is pressed.");
        });
    }

    @Test
    void testBulletHitsAsteroid() throws Exception {
        runAndCatch(() -> {
            // Create a real Asteroid
            AsteroidClass asteroid = new AsteroidClass(GameCharacter.EntityType.LARGE_ASTEROID, 100, 100, 1.5);
            gameLogic.getAsteroidList().add(asteroid);
            realGameLayer.getChildren().add(asteroid.getImageView());

            // Create a real Bullet and position it to collide with the asteroid
            Bullet bullet = new Bullet(GameCharacter.EntityType.BULLET, 100, 100);
            bullet.setRotation(0);
            bullet.applyAcceleration(10.0);
            gameLogic.getBulletList().add(bullet);
            realGameLayer.getChildren().add(bullet.getImageView());

            // Simulate collision
            gameLogic.handleBulletAsteroidCollisions();

            // Verify that asteroid and bullet are removed
            assertFalse(gameLogic.getAsteroidList().contains(asteroid),
                    "Asteroid should be removed after being hit by a bullet.");
            assertFalse(gameLogic.getBulletList().contains(bullet),
                    "Bullet should be removed after hitting an asteroid.");
            assertFalse(realGameLayer.getChildren().contains(asteroid.getImageView()),
                    "Asteroid's ImageView should be removed from the game layer.");
            assertFalse(realGameLayer.getChildren().contains(bullet.getImageView()),
                    "Bullet's ImageView should be removed from the game layer.");
        });
    }

    @Test
    void testBulletHitsAlien() throws Exception {
        runAndCatch(() -> {
            // Create a real AlienShip
            AlienShip alienShip = new AlienShip(GameCharacter.EntityType.ALIEN_SHIP, 100, 100, 1.5);
            gameLogic.getAlienShipList().add(alienShip);
            realGameLayer.getChildren().add(alienShip.getImageView());

            // Create a real Bullet and position it to collide with the alien ship
            Bullet bullet = new Bullet(GameCharacter.EntityType.BULLET, 100, 100);
            bullet.setRotation(0);
            bullet.applyAcceleration(10.0);
            gameLogic.getBulletList().add(bullet);
            realGameLayer.getChildren().add(bullet.getImageView());

            // Simulate collision
            gameLogic.handleBulletAlienCollisions();

            // Verify that alien ship and bullet are removed
            assertFalse(gameLogic.getAlienShipList().contains(alienShip),
                    "Alien ship should be removed after being hit by a bullet.");
            assertFalse(gameLogic.getBulletList().contains(bullet),
                    "Bullet should be removed after hitting an alien ship.");
            assertFalse(realGameLayer.getChildren().contains(alienShip.getImageView()),
                    "Alien ship's ImageView should be removed from the game layer.");
            assertFalse(realGameLayer.getChildren().contains(bullet.getImageView()),
                    "Bullet's ImageView should be removed from the game layer.");
        });
    }

    @Test
    void testDestroyBossIncreasesScore() throws Exception {
        runAndCatch(() -> {
            gameLogic = new GameLogic();

            Group realRoot = new Group();
            Group realGameLayer = new Group();
            Group realUiLayer = new Group();

            gameLogic.setRoot(realRoot);
            gameLogic.setGameLayer(realGameLayer);
            gameLogic.setUiLayer(realUiLayer);

            realRoot.getChildren().addAll(realGameLayer, realUiLayer);

            TextClass mockScoreText = new TextClass("Score: 0", 30, 50, Color.WHITE, 40);
            gameLogic.setScoreText(mockScoreText);

            TextClass mockYouDiedText = new TextClass("You Died", 200, 300, Color.RED, 80);
            gameLogic.setYouDiedText(mockYouDiedText);

            gameLogic.initializeLives();
            gameLogic.initializePlayer();

            PlayerShip playerShip = gameLogic.getPlayerShip();

            gameLogic.initializeBoss();

            Boss boss = gameLogic.getBoss();
            boss.setHp(0); // ตั้งค่า HP ของบอสเป็น 0 เพื่อให้บอสถูกทำลาย

            int initialScore = gameLogic.getScore().get();

            gameLogic.handleBossCollisions();

            int finalScore = gameLogic.getScore().get();

            assertEquals(initialScore + 5000, finalScore, "Score should increase by 5000 after the boss is destroyed.");
        });
    }
    @Test
    void testWrapAroundRightEdge() {
        // ตั้งค่าตำแหน่งยานไปที่ขอบขวาของหน้าจอ
        playerShip.getImageView().setTranslateX(1280 + playerShip.getRadius());

        // เรียกใช้ applyMove เพื่อจำลองการเคลื่อนที่และ wrap-around
        playerShip.applyMove(1280, 832);

        // ตรวจสอบว่าตำแหน่งของยานถูกวาดกลับไปทางซ้ายของหน้าจอ
        assertTrue(playerShip.getImageView().getTranslateX() < 0,
                "PlayerShip should wrap around to the left side when moving past the right edge.");
    }
    @Test
    void testWrapAroundLeftEdge() {
        // ตั้งค่าตำแหน่งยานไปที่ขอบซ้ายของหน้าจอ
        playerShip.getImageView().setTranslateX(-playerShip.getRadius());

        // เรียกใช้ applyMove เพื่อจำลองการเคลื่อนที่และ wrap-around
        playerShip.applyMove(1280, 832);

        // ตรวจสอบว่าตำแหน่งของยานถูกวาดกลับไปทางขวาของหน้าจอ
        assertTrue(playerShip.getImageView().getTranslateX() > 1280,
                "PlayerShip should wrap around to the right side when moving past the left edge.");
    }

    @Test
    void testWrapAroundTopEdge() {
        // ตั้งค่าตำแหน่งยานไปที่ขอบบนของหน้าจอ
        playerShip.getImageView().setTranslateY(-playerShip.getRadius());

        // เรียกใช้ applyMove เพื่อจำลองการเคลื่อนที่และ wrap-around
        playerShip.applyMove(1280, 832);

        // ตรวจสอบว่าตำแหน่งของยานถูกวาดกลับไปทางล่างของหน้าจอ
        assertTrue(playerShip.getImageView().getTranslateY() > 832,
                "PlayerShip should wrap around to the bottom side when moving past the top edge.");
    }

    @Test
    void testWrapAroundBottomEdge() {
        // ตั้งค่าตำแหน่งยานไปที่ขอบล่างของหน้าจอ
        playerShip.getImageView().setTranslateY(832 + playerShip.getRadius());

        // เรียกใช้ applyMove เพื่อจำลองการเคลื่อนที่และ wrap-around
        playerShip.applyMove(1280, 832);

        // ตรวจสอบว่าตำแหน่งของยานถูกวาดกลับไปทางบนของหน้าจอ
        assertTrue(playerShip.getImageView().getTranslateY() < 0,
                "PlayerShip should wrap around to the top side when moving past the bottom edge.");
    }

    @Test
    public void testPlayerRespawnAfterDeath() throws Exception {
        runAndCatch(() -> {
            // เพิ่มชีวิตให้ผู้เล่น
            gameLogic.getPlayerLives().add(new ImageView());
            gameLogic.getPlayerLives().add(new ImageView());

            // จำลองสถานการณ์ที่ผู้เล่นตายโดยการชนกับ AlienShip
            AlienShip alienShip = new AlienShip(GameCharacter.EntityType.ALIEN_SHIP, playerShip.getPositionX(), playerShip.getPositionY(), 1.0);
            gameLogic.getAlienShipList().add(alienShip);
            gameLogic.getGameLayer().getChildren().add(alienShip.getImageView());

            // เรียกใช้การจัดการการชน
            gameLogic.handlePlayerCollisions();

            // ตรวจสอบว่าผู้เล่นถูกทำให้ตาย
            assertFalse(gameLogic.getPlayerShip().isAlive(), "Player should be dead after collision.");

            // ตรวจสอบว่า ImageView ของผู้เล่นถูกลบออกจาก gameLayer
            assertFalse(gameLogic.getGameLayer().getChildren().contains(gameLogic.getPlayerShip().getImageView()),
                    "Player's ImageView should be removed from the game layer after death.");

            // กดปุ่ม Enter เพื่อให้ผู้เล่นเกิดใหม่
            gameLogic.setKeyPressed(KeyCode.ENTER, true);
            gameLogic.handlePlayerStatus(); // เรียกใช้การจัดการสถานะผู้เล่น

            // รีเซ็ตการกดปุ่ม Enter
            gameLogic.setKeyPressed(KeyCode.ENTER, false);

            // ตรวจสอบว่าผู้เล่นเกิดใหม่แล้วและถูกเพิ่มกลับเข้ามาใน gameLayer
            assertTrue(gameLogic.getPlayerShip().isAlive(), "Player should respawn and be alive.");
            assertEquals(600, gameLogic.getPlayerShip().getImageView().getTranslateX(), "Player should respawn at the initial X position.");
            assertEquals(400, gameLogic.getPlayerShip().getImageView().getTranslateY(), "Player should respawn at the initial Y position.");
            assertTrue(gameLogic.getGameLayer().getChildren().contains(gameLogic.getPlayerShip().getImageView()),
                    "Player's ImageView should be added back to the game layer.");
        });
    }




    @AfterEach
    void tearDown() {
        // Reset the mock ExceptionHandler after each test
        ExceptionHandler.resetHandlerInstance();
    }


}