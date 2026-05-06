import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataLoader {

    public static Map<User, Map<Song, Interaction>> loadUserData(String filePath) {

        Map<User, Map<Song, Interaction>> userData = new HashMap<>();

        // Cache để tránh tạo object trùng
        Map<String, User> userCache = new HashMap<>();
        Map<String, Song> songCache = new HashMap<>();

        int totalLines = 0;
        int errorLines = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;

            // bỏ header
            br.readLine();

            while ((line = br.readLine()) != null) {

                totalLines++;

                // split theo |
                String[] parts = line.split("\\|", -1);

                if (parts.length < 4) {
                    errorLines++;
                    continue;
                }

                try {
                    String userId = parts[0].trim();
                    String trackId = parts[1].trim();
                    String trackName = parts[2].trim();
                    String artistId = parts[3].trim();
                    String artistName = parts[4].trim();
                    int PlayCount = Integer.parseInt(parts[5].trim());
                    double playCountLog = Double.parseDouble(parts[6].trim());

                    if (userId.isEmpty() || trackId.isEmpty()) {
                        errorLines++;
                        continue;
                    }

                    // ===== USER CACHE =====
                    User user = userCache.computeIfAbsent(userId, User::new);

                    // ===== SONG CACHE =====
                    String songKey = trackId + "||" + trackName + "||" + artistId + "||" + artistName;
                    Song song = songCache.computeIfAbsent(songKey,
                            k -> new Song(trackId, trackName, artistId, artistName));

                    // ===== USER DATA =====
                    Map<Song, Interaction> songMap =
                            userData.computeIfAbsent(user, k -> new HashMap<>());

                    // ===== MERGE INTERACTION =====
                    Interaction existing = songMap.get(song);

                    if (existing == null) {
                        songMap.put(song, new Interaction(user, song, playCountLog));
                    } else {
                        // update trực tiếp, KHÔNG tạo object mới
                        existing.setPlayCount(existing.getPlayCount() + playCountLog);
                    }

                } catch (Exception e) {
                    errorLines++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Total lines: " + totalLines);
        System.out.println("Error lines: " + errorLines);
        System.out.println("Users loaded: " + userData.size());

        return userData;
    }
}