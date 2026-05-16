package algorithm;

import data.*;
import util.*;
import java.util.*;
import java.util.stream.Collectors;

public class BruteForceRecommender implements Recommender {
    private Map<User, Map<Song, Interaction>> data;

    public BruteForceRecommender(Map<User, Map<Song, Interaction>> data) {
        this.data = data;
    }

    @Override
    public List<Song> recommend(User user, int k) {
        Map<Song, Interaction> userHistory = data.getOrDefault(user, new HashMap<>());
        Set<Song> heardSongs = userHistory.keySet();

        Map<User, Double> neighbours = new HashMap<>();
        for (User other : data.keySet()) {
            if (!other.equals(user)) {
                double sim = Similarity.cosine(userHistory, data.get(other));
                neighbours.put(other, sim);
            }
        }

        Map<Song, Double> scores = Scoring.score(user, data, neighbours);
        
        if (scores == null || scores.isEmpty()) {
            return new ArrayList<>();
        }

        return scores.entrySet().stream()
                .filter(entry -> !heardSongs.contains(entry.getKey()))
                .sorted(Map.Entry.<Song, Double>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
