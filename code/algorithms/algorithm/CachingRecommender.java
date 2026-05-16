package algorithm;

import data.*;
import java.util.*;
import util.Scoring;
import util.ScoringVersion;
import util.Similarity;
import util.SimilarityVersion;

public class CachingRecommender implements Recommender {
    private Map<User, Map<Song, Interaction>> data;
    //V1
    private Map<String, Double> similarityCache;
    //V2 + 3
    private Map<User, Map<User, Double>> similarityTable;
    //V3
    private Map<User, Map<User, Double>> neighborTable;

    private CachingVersion version;


    private static final double SIM_THRESHOLD          = 0.05;
    //private static final double SIM_THRESHOLD_FALLBACK = 0.05;

    private boolean similarityTableBuilt = false;
    //private boolean neighborTableBuilt   = false;

    public CachingRecommender(Map<User, Map<Song, Interaction>> data) {
        this(data, CachingVersion.V2);
    }

    public CachingRecommender(Map<User, Map<Song, Interaction>> data, CachingVersion version) {
        this.data = data;
        this.similarityCache = new HashMap<>();
        this.version = version;
    }

    private String getCacheKey(User u1, User u2) {
        String id1 = u1.getUserId();
        String id2 = u2.getUserId();
        if (id1.compareTo(id2) < 0) {
            return id1 + "||" + id2;
        } else {
            return id2 + "||" + id1;
        }
    }

    @Override
    public List<Song> recommend(User targetUser, int k) {

        if(!data.containsKey(targetUser)) return new ArrayList<>();

        switch (this.version) {
            case V1:
                return recommendV1_TopDownMemo(targetUser, k);
            case V2:
                return recommendV2_BottomUpTable(targetUser, k);
            case V3:
                //return recommendV3_PrecomputedNeighbors(targetUser, k);
                return new ArrayList<>();
            default:
                return new ArrayList<>();
        }
    }

    private List<Song> scoreAndExtractTopK(User targetUser,
                                           Map<User, Double> qualifiedNeighbors,
                                           int k) {

        Map<Song, Double> songScores = Scoring.score(targetUser, data, qualifiedNeighbors);

        PriorityQueue<Map.Entry<Song, Double>> heapSongs = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );
        for (Map.Entry<Song, Double> entry : songScores.entrySet()) {
            heapSongs.offer(entry);
            if (heapSongs.size() > k) {
                heapSongs.poll();
            }
        }

        List<Song> results = new ArrayList<>();
        while (!heapSongs.isEmpty()) {
            results.add(0, heapSongs.poll().getKey());
        }
        return results;
    }

    private List<Song> recommendV1_TopDownMemo(User targetUser, int k){
        Map<Song, Interaction> targetHistory = data.get(targetUser);

        Map<User, Double> qualifiedNeighbors = new HashMap<>();

        for (Map.Entry<User, Map<Song, Interaction>> entry : data.entrySet()) {
            User otherUser = entry.getKey();
            if (otherUser.equals(targetUser)) continue;

            String cacheKey = getCacheKey(targetUser, otherUser);
            double sim;

            if (similarityCache.containsKey(cacheKey)) {
                sim = similarityCache.get(cacheKey);
            } else {
                sim = Similarity.cosine(targetHistory, entry.getValue());
                similarityCache.put(cacheKey, sim);
            }

            if (sim > SIM_THRESHOLD) {
                qualifiedNeighbors.put(otherUser, sim);
            }
        }
        return scoreAndExtractTopK(targetUser, qualifiedNeighbors, k);
    }

    private void buildSimilarityTable() {
        if (similarityTableBuilt) return;

        similarityTable = new HashMap<>();
        List<User> users = new ArrayList<>(data.keySet());
        int U = users.size();

        for (int i = 0; i < U; i++) {
            User u = users.get(i);
            Map<Song, Interaction> histU = data.get(u);

            for (int j = i + 1; j < U; j++) {
                User v = users.get(j);

                double sim = Similarity.cosine(histU, data.get(v));

                if (sim > SIM_THRESHOLD) {
                    similarityTable
                            .computeIfAbsent(u, x -> new HashMap<>())
                            .put(v, sim);
                    similarityTable
                            .computeIfAbsent(v, x -> new HashMap<>())
                            .put(u, sim);
                }
            }
        }
        similarityTableBuilt = true;
    }

    private List<Song> recommendV2_BottomUpTable(User targetUser, int k) {
        buildSimilarityTable();

        Map<User, Double> qualifiedNeighbors =
                similarityTable.getOrDefault(targetUser, Collections.emptyMap());

        return scoreAndExtractTopK(targetUser, qualifiedNeighbors, k);
    }
}
