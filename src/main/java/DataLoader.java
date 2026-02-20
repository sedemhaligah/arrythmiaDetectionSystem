import java.nio.file.*;
import java.util.*;

public class DataLoader {

    // ----- TRAINING -----
    public static List<String[]> loadTrain(Path p) throws Exception {
        List<String[]> lines = new ArrayList<>();
        for (String line : Files.readAllLines(p)) {
            lines.add(line.trim().split("\\s+"));
        }
        return lines;
    }

    // ----- EVALUATION -----
    public static int[] loadEvalValues(Path p) throws Exception {
        String first = Files.readAllLines(p).get(0).trim();
        return Arrays.stream(first.split("\\s+"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    public static List<Interval> loadEvalTruth(Path p) throws Exception {
        String second = Files.readAllLines(p).get(1).trim();
        List<Interval> truth = new ArrayList<>();

        for (String tok : second.split("\\s+")) {
            tok = tok.replace("[", "").replace("]", "");
            String[] xy = tok.split(",");
            truth.add(new Interval(Integer.parseInt(xy[0]),
                    Integer.parseInt(xy[1])));
        }
        return truth;
    }
}
