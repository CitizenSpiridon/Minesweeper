package game;

import java.io.*;
import java.util.Properties;

public class Statistics {
    private Properties props = new Properties();
    private String filePath;

    public Statistics() {
        filePath = System.getProperty("user.home") + "/minesweeper_stats.properties";
        load();
    }

    private void load() {
        try (FileInputStream in = new FileInputStream(filePath)) {
            props.load(in);
        } catch (IOException e) {
        }
    }

    private void save() {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            props.store(out, "Minesweeper Statistics");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordGame(int difficulty, boolean win, int timeSeconds) {
        int games = getInt(difficulty, "games") + 1;
        int wins = getInt(difficulty, "wins") + (win ? 1 : 0);

        setInt(difficulty, "games", games);
        setInt(difficulty, "wins", wins);

        if (win) {
            int bestTime = getInt(difficulty, "bestTime");
            if (bestTime == 0 || timeSeconds < bestTime) {
                setInt(difficulty, "bestTime", timeSeconds);
            }
        }

        save();
    }

    public int getGames(int difficulty) {
        return getInt(difficulty, "games");
    }

    public int getWins(int difficulty) {
        return getInt(difficulty, "wins");
    }

    public String getWinRate(int difficulty) {
        int games = getGames(difficulty);
        if (games == 0) return "0%";
        return String.format("%.1f%%", (getWins(difficulty) * 100.0) / games);
    }

    public String getBestTime(int difficulty) {
        int seconds = getInt(difficulty, "bestTime");
        if (seconds == 0) return "--:--";
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private String key(int difficulty, String field) {
        String[] names = {"beginner", "amateur", "expert"};
        return "stats." + names[difficulty] + "." + field;
    }

    private int getInt(int difficulty, String field) {
        try {
            return Integer.parseInt(props.getProperty(key(difficulty, field), "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setInt(int difficulty, String field, int value) {
        props.setProperty(key(difficulty, field), String.valueOf(value));
    }
}