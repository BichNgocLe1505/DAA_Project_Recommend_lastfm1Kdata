import data.*;
import algorithm.*;
import benchmark.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {

        String filename = "../data/samples_csv/play_count_sample_500users.csv";

        Map<User, Map<Song, Interaction>> data =
                DataLoader.loadUserData(filename);

        System.out.println("Users: " + data.size());

        // in thử
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

        // benchmark thử
        List<Recommender> recommenders = List.of(
                new BruteForceRecommender(data),
                new GreedyRecommender(data),
                new HeuristicRecommender(data)
        );

        List<User> testUsers = new ArrayList<>(data.keySet()).subList(0, 100);

        List<BenchmarkResult> results =
                BenchmarkRunner.run(recommenders, testUsers, 5);

        for (BenchmarkResult r : results) {
            System.out.println(r.getAlgorithmName() + " : " + r.getTimeNano());
        }
    }

}
