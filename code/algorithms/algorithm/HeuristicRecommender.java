package algorithm;

import data.*;
import util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeuristicRecommender implements Recommender {
    private Map<User, Map<Song, Interaction>> data;

    public HeuristicRecommender(Map<User, Map<Song, Interaction>> data) {
        this.data = data;
    }

    @Override
    public List<Song> recommend(User user, int k) {
        return new ArrayList<>();
    }
}
