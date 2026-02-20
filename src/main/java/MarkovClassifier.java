import java.util.*;


public class MarkovClassifier {

    // Number of discrete heart-rate states (bins)
    private final int NUM_STATES = 8;

    // Length of sliding window used for detection
    private final int WINDOW = 50;

    // ---------------------- MODEL PARAMETERS -----------------------

    // Transition counts and initial-state counts for Arrhythmia model
    private long[][] countA = new long[NUM_STATES][NUM_STATES];
    private long[] initA = new long[NUM_STATES];

    // Log-transition probabilities and log-initial probabilities
    private double[][] probA;
    private double[] probInitA;

    // Transition counts and initial-state counts for Normal model
    private long[][] countN = new long[NUM_STATES][NUM_STATES];
    private long[] initN = new long[NUM_STATES];

    // Log-transition probabilities and log-initial probabilities
    private double[][] probN;
    private double[] probInitN;

    // Prior probabilities (class imbalance: arrhythmia is rare)
    private final double logPA = Math.log(0.05);
    private final double logPN = Math.log(0.95);

    // ---------------------- STATE ENCODING -------------------------

    /**
     * Assigns a heart-rate value to a discrete state (bin).
     */
    private int encode(int v) {
        int min = 20, max = 80;
        v = Math.max(min, Math.min(max, v));
        double ratio = (v - min) / (double)(max - min + 1);
        return Math.min((int)(ratio * NUM_STATES), NUM_STATES - 1);
    }

    /**
     * Encodes an entire signal into discrete Markov states.
     */
    private int[] encodeAll(int[] vals) {
        int[] s = new int[vals.length];
        for (int i = 0; i < vals.length; i++)
            s[i] = encode(vals[i]);
        return s;
    }

    // ---------------------- TRAINING ------------------------------

    /**
     * Trains two first-order Markov models (A and N) from labeled sequences.
     * Counts state transitions and initial states separately per class.
     */
    public void train(List<String[]> trainLines) {

        for (String[] parts : trainLines) {
            boolean isA = parts[0].equals("A");

            // Encode sequence into discrete states
            int[] seq = new int[parts.length - 1];
            for (int i = 1; i < parts.length; i++)
                seq[i - 1] = encode(Integer.parseInt(parts[i]));

            if (seq.length == 0) continue;

            // Count initial state
            if (isA) initA[seq[0]]++;
            else     initN[seq[0]]++;

            // Count state transitions
            for (int i = 1; i < seq.length; i++) {
                if (isA) countA[seq[i-1]][seq[i]]++;
                else     countN[seq[i-1]][seq[i]]++;
            }
        }

        // Convert counts to log-probabilities with Laplace smoothing
        probA = normalize(countA);
        probN = normalize(countN);
        probInitA = normalizeInit(initA);
        probInitN = normalizeInit(initN);
    }

    // ---------------------- NORMALIZATION --------------------------

    /**
     * Converts transition counts into log-probabilities.
     * Laplace smoothing avoids zero probabilities.
     */
    private double[][] normalize(long[][] counts) {
        double[][] prob = new double[NUM_STATES][NUM_STATES];
        for (int i = 0; i < NUM_STATES; i++) {
            long sum = NUM_STATES;
            for (int j = 0; j < NUM_STATES; j++)
                sum += counts[i][j];

            for (int j = 0; j < NUM_STATES; j++)
                prob[i][j] = Math.log((counts[i][j] + 1.0) / sum);
        }
        return prob;
    }

    /**
     * Converts initial-state counts into log-probabilities.
     */
    private double[] normalizeInit(long[] init) {
        double[] p = new double[NUM_STATES];
        long sum = NUM_STATES;
        for (long x : init) sum += x;
        for (int i = 0; i < NUM_STATES; i++)
            p[i] = Math.log((init[i] + 1.0) / sum);
        return p;
    }

    // ---------------------- LOG-LIKELIHOOD -------------------------

    /**
     * Computes log-likelihood of a window under the Arrhythmia model.
     */
    private double logLikA(int[] win) {
        double log = probInitA[win[0]];
        for (int i = 1; i < win.length; i++)
            log += probA[win[i-1]][win[i]];
        return log + logPA;
    }

    /**
     * Computes log-likelihood of a window under the Normal model.
     */
    private double logLikN(int[] win) {
        double log = probInitN[win[0]];
        for (int i = 1; i < win.length; i++)
            log += probN[win[i-1]][win[i]];
        return log + logPN;
    }

    // ---------------------- DETECTION ------------------------------

    /**
     * Slides a fixed-length window over the signal and detects
     * non-overlapping intervals classified as arrhythmia
     * using a positive log-likelihood ratio.
     */
    public List<Interval> detect(int[] raw) {

        int[] states = encodeAll(raw);
        int maxStart = states.length - WINDOW;
        if (maxStart < 0) return List.of();

        // Compute log-likelihood ratios for all windows
        List<double[]> scores = new ArrayList<>();
        for (int t = 0; t <= maxStart; t++) {
            int[] win = Arrays.copyOfRange(states, t, t + WINDOW);
            double llr = logLikA(win) - logLikN(win);
            scores.add(new double[]{t, llr});
        }

        // Sort windows by confidence (highest LLR first)
        scores.sort((a,b)->Double.compare(b[1], a[1]));

        List<Interval> out = new ArrayList<>();

        // Greedy selection of positive, non-overlapping detections
        for (double[] sc : scores) {
            int t = (int)sc[0];
            if (sc[1] <= 0) break;

            Interval cand = new Interval(t, t + WINDOW - 1);

            boolean overlaps = out.stream()
                    .anyMatch(iv -> iv.overlap(cand) > 0);

            if (!overlaps) out.add(cand);
        }

        return out;
    }
}
