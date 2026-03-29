package service;

import hero.Hero;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for managing high scores.
 * Handles saving, loading, and calculating player scores.
 */
public class ScoreManager {
    private static final String SCORE_FILE = "highscores.txt";
    private static final int MAX_ENTRIES = 10;

    /**
     * Represents a single high score entry.
     *
     * @param name  The player's name.
     * @param score The score value.
     * @param floor The last floor reached.
     * @param date  The date of the score.
     */
    public record ScoreEntry(String name, int score, int floor, String date) implements Serializable {
        /** Returns a string representation suitable for saving to a file. */
        public String toFileString() {
            return name + ";" + score + ";" + floor + ";" + date;
        }

        /** Parses a file line and returns a ScoreEntry object, or null if invalid. */
        public static ScoreEntry fromFileString(String line) {
            String[] parts = line.split(";");
            if (parts.length < 4)
                return null;
            return new ScoreEntry(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[3]);
        }
    }

    /**
     * Calculates the score for a hero at a given floor.
     *
     * @param hero  The hero object.
     * @param floor The current floor reached.
     * @return The calculated score.
     */
    public static int calculateScore(Hero hero, int floor) {
        Objects.requireNonNull(hero, "hero cannot be null");
        int floorScore = floor * 2000;
        int inventoryValue = hero.getBackPack().getTotalItemValue() * 2;
        int goldScore = hero.getGold();
        int hpScore = hero.getMaxHp() * 10;

        return floorScore + inventoryValue + goldScore + hpScore;
    }

    /**
     * Saves a player's score to the highscore file.
     * Keeps only the top MAX_ENTRIES scores.
     *
     * @param name  Player's name.
     * @param score Score value.
     * @param floor Last floor reached.
     */
    public static void saveScore(String name, int score, int floor) {
        Objects.requireNonNull(name, "name cannot be null");
        List<ScoreEntry> scores = loadScores();
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        scores.add(new ScoreEntry(name, score, floor, date));

        // Sort descending and keep top MAX_ENTRIES
        List<ScoreEntry> topScores = scores.stream()
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(MAX_ENTRIES)
                .collect(Collectors.toList());

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(SCORE_FILE))) {
            for (ScoreEntry entry : topScores) {
                writer.write(entry.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du score : " + e.getMessage());
        }
    }

    /**
     * Loads high scores from the file.
     *
     * @return A list of ScoreEntry objects.
     */
    public static List<ScoreEntry> loadScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        Path path = Paths.get(SCORE_FILE);
        if (!Files.exists(path)) {
            return scores;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                ScoreEntry entry = ScoreEntry.fromFileString(line);
                if (entry != null) {
                    scores.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des scores : " + e.getMessage());
        }
        return scores;
    }
}
