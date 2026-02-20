import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Usage: java -jar isys3.jar train.txt eval.txt");
            return;
        }

        // Load data
        var trainLines = DataLoader.loadTrain(Paths.get(args[0]));
        var evalValues = DataLoader.loadEvalValues(Paths.get(args[1]));
        Path evalPath = Paths.get(args[1]);


        // Train and classify
        MarkovClassifier clf = new MarkovClassifier();
        clf.train(trainLines);
        List<Interval> found = clf.detect(evalValues);

        // Output intervals
        for (Interval iv : found)
            System.out.println("[" + iv.start() + "," + iv.end() + "]");

        //only for metrics. ask Sahilesh if to leave this in the jar
        List<Interval> truth = DataLoader.loadEvalTruth(evalPath);
        Evaluator.printMetrics(found, truth);
    }

}
