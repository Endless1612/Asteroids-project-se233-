package com.example.iceteriod.controller;

public class HighScoreEntry implements Comparable<HighScoreEntry> {
    private String name;
    private int score;
    private int level;

    public HighScoreEntry(String name, int score, int level) {
        this.name = name;
        this.score = score;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public int compareTo(HighScoreEntry other) {
        return Integer.compare(other.score, this.score); // เรียงจากมากไปน้อย
    }

    @Override
    public String toString() {
        return name + "," + score + "," + level;
    }
}