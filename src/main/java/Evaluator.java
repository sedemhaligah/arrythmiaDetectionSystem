import java.util.List;

public class Evaluator {

    public static void printMetrics(List<Interval> predicted, List<Interval> truth) {
        int TP = 0;
        boolean[] used = new boolean[truth.size()];

        for (Interval p : predicted) {
            for (int i = 0; i < truth.size(); i++) {
                if (!used[i] && p.overlap(truth.get(i)) >= 40) {
                    TP++;
                    used[i] = true;
                    break;
                }
            }
        }

        int FP = predicted.size() - TP;
        int FN = truth.size() - TP;

        double precision = (TP + FP == 0) ? 0 : (double) TP / (TP + FP);
        double recall    = (TP + FN == 0) ? 0 : (double) TP / (TP + FN);
        double f1        = (precision + recall == 0) ? 0 :
                2 * precision * recall / (precision + recall);

        System.out.printf("precision: %.4f\n", precision);
        System.out.printf("recall:    %.4f\n", recall);
        System.out.printf("f1-score:  %.4f\n", f1);
    }
}
