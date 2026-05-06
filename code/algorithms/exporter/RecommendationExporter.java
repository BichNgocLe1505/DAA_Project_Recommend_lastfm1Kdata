package exporter;

import benchmark.*;
import algorithm.*;
import data.*;

import java.io.*;
import java.util.List;
import java.util.Map;

public class RecommendationExporter {

    public static void exportToCSV(
            String filePath,
            Map<User, List<Song>> recommendations) {

        try {
            File file = new File(filePath);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

                writer.write("User|Artist|Track\n");

                for (Map.Entry<User, List<Song>> entry : recommendations.entrySet()) {

                    User user = entry.getKey();

                    List<Song> songs = entry.getValue();
                    if (songs == null) continue;

                    for (Song song : songs) {

                        writer.write(
                                sanitize(user.getUserId()) + "|" +
                                        sanitize(song.getArtistName()) + "|" +
                                        sanitize(song.getTrackName()) + "\n"
                        );
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace("\n", " ");
    }
}