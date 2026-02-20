public record Interval(int start, int end) {

    public int length() {
        return end - start + 1;
    }

    public int overlap(Interval other) {
        int s = Math.max(this.start, other.start);
        int e = Math.min(this.end, other.end);
        return Math.max(0, e - s + 1);
    }
}
