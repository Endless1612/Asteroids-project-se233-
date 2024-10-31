package com.example.iceteriod.view;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TextClass extends Text {
    int x;
    int y;
    int size;
    public Text mytext = new Text();

    public TextClass(String textString, int x, int y, Color fillColor, int size){
        this.x = x;
        this.y = y;
        this.size = size;

        this.mytext.setText(textString);
        this.mytext.setFill(fillColor);
        this.mytext.setX(this.x);
        this.mytext.setY(this.y);

        // โหลดฟอนต์จากโฟลเดอร์ทรัพยากร
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/VeniteAdoremusStraight-Yzo6v.ttf"), size);
        if (customFont != null) {
            this.mytext.setFont(customFont);
        } else {
            System.out.println("Could not load custom font. Using default font.");
            this.mytext.setFont(new Font(size));
        }
    }

    public void SetText(String newTextString) {
        this.mytext.setText(newTextString);
    }
}