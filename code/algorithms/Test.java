import data.*;
import algorithm.*;
import benchmark.*;
import exporter.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {

        String filename = "../data/samples_csv/play_count_sample_50users.csv";

        Map<User, Map<Song, Interaction>> data =
                DataLoader.loadUserData(filename);

        System.out.println("Users: " + data.size());

        // ===== TEST PRINT =====
        for (User u : data.keySet()) {
            System.out.println("data.User: " + u.getUserId());

            for (Song s : data.get(u).keySet()) {
                Interaction i = data.get(u).get(s);
                System.out.println(s.getArtistName() + " - " + s.getTrackName()
                        + " | " + i.getPlayCount());
                break;
            }
            break;
        }

        // ===== RECOMMENDER =====
        Recommender rec = new BruteForceRecommender(data);

        Map<User, List<Song>> recResults = new HashMap<>();

        int n = Math.min(100, data.size());
        List<User> testUsers = new ArrayList<>(data.keySet()).subList(0, n);

        for (User u : testUsers) {
            recResults.put(u, rec.recommend(u, 5));
        }

        /*
        // ===== EXPORT RECOMMENDATION =====
        RecommendationExporter.exportToCSV(
                "output/recommendations.csv",
                recResults
        );
        */

        // ===== BENCHMARK =====
        List<Recommender> recommenders = List.of(
                new BruteForceRecommender(data),
                new GreedyRecommender(data),
                new HeuristicRecommender(data)
        );

        List<BenchmarkResult> results =
                BenchmarkRunner.run(recommenders, testUsers, 5);

        for (BenchmarkResult r : results) {
            System.out.println(r.getAlgorithmName() + " : " + r.getTimeNano());
        }

        /*
        // ===== EXPORT BENCHMARK =====
        BenchmarkExporter.exportToCSV(
                "output/benchmark.csv",
                results
        );
        */



        // ===== Mock Test =====
        // ===== Mocktest Recommeder =====
        Map<User, List<Song>> fakeRecommendations = new HashMap<>();

        User u1 = new User("user1");
        User u2 = new User("user2");

        Song s1 = new Song("trackId_1", "Track X", "artistId_1", "Artist A");
        Song s2 = new Song("trackId_2", "Track Y", "artistId_2", "Artist B");
        Song s3 = new Song("trackId_3", "Track Z", "artistId_3", "Artist C");

        fakeRecommendations.put(u1, List.of(s1, s2));
        fakeRecommendations.put(u2, List.of(s3));

        // test exporter
        RecommendationExporter.exportToCSV(
                "output/test_recommendations.csv",
                fakeRecommendations
        );

        // ===== Mocktest Benchmark =====
        List<BenchmarkResult> fakeResults = List.of(
                new BenchmarkResult("BruteForce", 1000000L, 5),
                new BenchmarkResult("Greedy", 500000L, 5)
        );

        BenchmarkExporter.exportToCSV(
                "output/test_benchmark.csv",
                fakeResults
        );

        // ===== test case =====
        fakeRecommendations.put(new User("user3"), null);

        Song s4 = new Song("fakeId", "Song\nstrange", "fakeId", "Artist | weird");
        fakeRecommendations.put(new User("user4"), List.of(s4));
        RecommendationExporter.exportToCSV(
                "output/test_recommendations.csv",
                fakeRecommendations
        );
    }

}
