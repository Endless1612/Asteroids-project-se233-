package com.example.iceteriod;

import java.util.*;

import com.example.iceteriod.controller.HighScoreEntry;
import com.example.iceteriod.model.GameLogic;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AsteroidsGame extends Application {
    static Stage primaryStage;

    Font customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/Starjhol.ttf"), 90);
    Font buttonFont = Font.loadFont(getClass().getResourceAsStream("/fonts/VeniteAdoremusStraight-Yzo6v.ttf"), 30);

    // ฟอนต์และ UI อื่นๆ...

    private boolean isGameStarted = false; // เพิ่มตัวแปรนี้


    private static final AsteroidsGame instance = new AsteroidsGame();

    public static AsteroidsGame getInstance() {
        return instance;
    }

    private static final Logger logger = LogManager.getLogger(AsteroidsGame.class);

    final Scene menuScene = new Scene(new VBox(), 1280, 832);
    final Scene highScoresScene = new Scene(new VBox(), 1280, 832);
    final Scene enterNameScene = new Scene(new VBox(), 1280, 832);
    private TextField playerNameField;

    private final GameLogic gameLoop = new GameLogic();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        AsteroidsGame.primaryStage = primaryStage;
        primaryStage.setTitle("Asteroids");
        showMainMenu();
        logger.info("Program started");
    }
    private void showMainMenu() {
        Label title = new Label("Asteroids");
        title.setFont(customFont);
        title.setTextFill(Color.web("#00ffea"));
        title.setEffect(new DropShadow(20, Color.web("#00ffea")));

        // สร้างปุ่มเมนูโดยใช้ฟอนต์ที่ต้องการ
        Button startButton = createButton("Start", "#ddff00", buttonFont);
        Button highScoresButton = createButton("Scores", "#ff6a00", buttonFont);
        Button exitButton = createButton("Exit", "#ff9900", buttonFont);

        startButton.setOnAction(event -> showEnterNameScreen());
        highScoresButton.setOnAction(event -> showHighScores());
        exitButton.setOnAction(event -> primaryStage.close());

        VBox menuLayout = new VBox(40);
        menuLayout.getChildren().addAll(title, startButton, highScoresButton, exitButton);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setStyle("-fx-background-color: #000000;");

        menuScene.setRoot(menuLayout);
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Asteroids");
        primaryStage.show();
    }

    private Button createButton(String text, String borderColor, Font font) {
        Button button = new Button(text);

        // ตั้งค่าฟอนต์ที่รับมาจากพารามิเตอร์
        button.setFont(font);

        // ตั้งค่าสไตล์ของปุ่ม
        button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: " + borderColor + ";");

        // ตั้งค่าเอฟเฟกต์เมื่อเมาส์ hover
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: black; -fx-text-fill: #00ffea; -fx-border-color: #00ffea;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: " + borderColor + ";"));

        return button;
    }




    public void showHighScores() {
        List<HighScoreEntry> highScores = gameLoop.readHighScores();

        VBox highScoresLayout = new VBox(20); // ลดระยะห่างระหว่างองค์ประกอบ
        highScoresLayout.setAlignment(Pos.CENTER);
        highScoresLayout.setStyle("-fx-background-color: #000000; -fx-padding: 50;");

        // สร้างหัวข้อ High Scores
        Label highScoresTitle = new Label("High Scores");
        highScoresTitle.setFont(customFont);
        highScoresTitle.setTextFill(Color.web("#ff6a00"));
        highScoresTitle.setEffect(new DropShadow(20, Color.web("#ff6a00")));

        highScoresLayout.getChildren().add(highScoresTitle);

        // เพิ่มรายการคะแนนสูงสุด
        for (HighScoreEntry entry : highScores) {
            Label scoreLabel = new Label(entry.getName() + " - " + entry.getScore() + " - Level " + entry.getLevel());
            scoreLabel.setFont(buttonFont); // ใช้ฟอนต์เดียวกับปุ่ม
            scoreLabel.setTextFill(Color.WHITE);
            highScoresLayout.getChildren().add(scoreLabel);
        }

        Button backButton = createButton("Back to Main Menu", "#00ffea", buttonFont);
        backButton.setOnAction(event -> showMainMenu());

        HBox buttonBox = new HBox(backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(20); // ระยะห่างระหว่างปุ่มถ้ามีหลายปุ่ม

        highScoresLayout.getChildren().add(buttonBox);

        highScoresScene.setRoot(highScoresLayout);
        primaryStage.setScene(highScoresScene);
        primaryStage.setTitle("High Scores");
    }


    private void showEnterNameScreen() {
        Label enterNameLabel = new Label("Enter Name");
        enterNameLabel.setFont(customFont);
        enterNameLabel.setTextFill(Color.WHITE);
        enterNameLabel.setEffect(new DropShadow(20, Color.WHITE));

        playerNameField = new TextField();
        playerNameField.setPromptText("Player Name");
        playerNameField.setMaxWidth(240);

        Button submitButton = createButton("Submit", "#00ffea", buttonFont);
        submitButton.setOnAction(event -> {
            String playerName = playerNameField.getText().trim();
            if (!playerName.isEmpty()) {
                gameLoop.setPlayerName(playerName); // ตั้งชื่อผู้เล่นใน GameLogic
                showGame();
            }
        });

        VBox enterNameLayout = new VBox(30);
        enterNameLayout.getChildren().addAll(enterNameLabel, playerNameField, submitButton);
        enterNameLayout.setAlignment(Pos.CENTER);
        enterNameLayout.setStyle("-fx-background-color: #000000;");

        enterNameScene.setRoot(enterNameLayout);
        primaryStage.setScene(enterNameScene);
        primaryStage.setTitle("Enter Your Name");
    }



    private void showGame() {
        if (!isGameStarted) {
            gameLoop.start(primaryStage); // เริ่มเกมครั้งแรก
            isGameStarted = true;
        } else {
            gameLoop.resetGame(); // รีเซ็ตเกมสำหรับครั้งถัดไป
        }
    }

}