package algorithm;

import data.*;
import java.util.*;
import util.Scoring;
import util.Similarity;

public class TopKRecommender implements Recommender {
    private Map<User, Map<Song, Interaction>> data;
    private static final double SIM_THRESHOLD          = 0.1;
    private static final double SIM_THRESHOLD_FALLBACK = 0.05;

    public TopKRecommender(Map<User, Map<Song, Interaction>> data) {
        this.data = data;
    }

    @Override
    public List<Song> recommend(User targetUser, int k) {
        if(!data.containsKey(targetUser)){
            return new ArrayList<>();
        }

        return recommendV2_MinHeap(targetUser, k);
    }

    //O(U * log U)
    private List<Song> recommendV1_NaiveSort(User target, int k){
        Map<Song, Interaction> targetHistory = data.get(target);

//        List<Map.Entry<User, Double>> allNeighbors = new ArrayList<>();
//
//        for(Map.Entry<User, Map<Song, Interaction>> entry : data.entrySet()){
//            if(entry.getKey().equals(target)) continue;
//            double sim = Similarity.cosine(targetHistory, entry.getValue());
//
//            if(sim > SIM_THRESHOLD){
//                allNeighbors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), sim));
//            }
//        }
//
//        allNeighbors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
//
//        Map<User, Double> topNeighbors = new HashMap<>();
//        for(Map.Entry<User, Double> e : allNeighbors){
//            topNeighbors.put(e.getKey(), e.getValue());
//        }

        Map<User, Double> topNeighbors = new HashMap<>();

        for (Map.Entry<User, Map<Song, Interaction>> entry : data.entrySet()) {
            if (entry.getKey().equals(target)) continue;

            double sim = Similarity.cosine(targetHistory, entry.getValue());

            if (sim > SIM_THRESHOLD) {
                topNeighbors.put(entry.getKey(), sim);
            }
        }

        Map<Song, Double> songScores = Scoring.score(target, data, topNeighbors);

        List<Map.Entry<Song, Double>> allSongs = new ArrayList<>(songScores.entrySet());

        allSongs.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<Song> results = new ArrayList<>();
        int songLimit = Math.min(k, allSongs.size());
        for(int i = 0; i < songLimit; i++){
            results.add(allSongs.get(i).getKey());
        }
        return results;
    }

    //O(U * log N)
    private List<Song> recommendV2_MinHeap(User target, int k) {
        Map<Song, Interaction> targetHistory = data.get(target);

        Map<User, Double> topNeighbors = new HashMap<>();
        for(Map.Entry<User, Map<Song, Interaction>> entry : data.entrySet()){
            if(entry.getKey().equals(target)) continue;
            double sim = Similarity.cosine(targetHistory, entry.getValue());

            if(sim > SIM_THRESHOLD){
                topNeighbors.put(entry.getKey(), sim);
            }
        }

        Map<Song, Double> songScores = Scoring.score(target, data, topNeighbors);

        PriorityQueue<Map.Entry<Song, Double>> heapSongs = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );
        for(Map.Entry<Song, Double> entry : songScores.entrySet()){
            heapSongs.offer(entry);
            if(heapSongs.size() > k){
                heapSongs.poll();
            }
        }

        List<Song> results = new ArrayList<>();
        while(!heapSongs.isEmpty()){
            results.add(0, heapSongs.poll().getKey());
        }
        return results;
    }

    // O(U * log K_neighbors)
    private List<Song> recommendV3_DynamicFloor(User target, int k){
        Map<Song, Interaction> targetHistory = data.get(target);

        final int MAX_NEIGHBORS = 20;

        PriorityQueue<Map.Entry<User, Double>> heapNeighbors = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );

        double dynamicFloor = SIM_THRESHOLD;

        for(Map.Entry<User, Map<Song, Interaction>> entry : data.entrySet()){
            if(entry.getKey().equals(target)) continue;

            double sim = Similarity.cosine(targetHistory, entry.getValue());

            if(sim <= dynamicFloor) continue;

            heapNeighbors.offer(new AbstractMap.SimpleEntry<>(entry.getKey(), sim));

            if(heapNeighbors.size() > MAX_NEIGHBORS){
                heapNeighbors.poll();
                dynamicFloor = heapNeighbors.peek().getValue();
            }
        }

        if(heapNeighbors.isEmpty()){
            for(Map.Entry<User, Map<Song, Interaction>> entry : data.entrySet()){
                if(entry.getKey().equals(target)) continue;

                double sim = Similarity.cosine(targetHistory, entry.getValue());

                if(sim > SIM_THRESHOLD_FALLBACK){
                    heapNeighbors.offer(
                            new AbstractMap.SimpleEntry<>(entry.getKey(), sim));
                }
            }
        }

        if(heapNeighbors.isEmpty()) return new ArrayList<>();

        Map<User, Double> topNeighbors = new HashMap<>();
        for(Map.Entry<User, Double> e : heapNeighbors){
            topNeighbors.put(e.getKey(), e.getValue());
        }

        Map<Song, Double> songScores = Scoring.score(target, data, topNeighbors);

        PriorityQueue<Map.Entry<Song, Double>> heapSongs = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );

        for(Map.Entry<Song, Double> entry : songScores.entrySet()){
            heapSongs.offer(entry);
            if(heapSongs.size() > k){
                heapSongs.poll();
            }
        }

        List<Song> results = new ArrayList<>(k);
        while(!heapSongs.isEmpty()){
            results.add(heapSongs.poll().getKey());
        }
        Collections.reverse(results);

        return results;
    }
}
