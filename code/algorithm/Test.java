import java.util.Map;

public class Test {
    public static void main(String[] args) {

        String filename = "../data/samples_csv/play_count_sample_50users.csv";

        Map<User, Map<Song, Interaction>> data =
                DataLoader.loadUserData(filename);

        System.out.println("Users: " + data.size());

        // in thử
        for (User u : data.keySet()) {
            System.out.println("User: " + u.getUserId());

            for (Song s : data.get(u).keySet()) {
                Interaction i = data.get(u).get(s);
                System.out.println(s.getArtistName() + " - " + s.getTrackName()
                        + " | " + i.getPlayCount());
                break;
            }
            break;
        }
    }

}
