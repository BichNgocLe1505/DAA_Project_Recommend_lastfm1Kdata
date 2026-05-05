import java.util.List;
import java.util.Map;

public class BruteForceRecommender implements Recommender {
    private Map<User, Map<Song, Interaction>> data;

    public BruteForceRecommender(Map<User, Map<Song, Interaction>> data) {
        this.data = data;
    }

    @Override
    public List<Song> recommend(User user, int k) {
        return null;
    }
}
