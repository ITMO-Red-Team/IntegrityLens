public class Records {
    String t() {
        String tb = """
            text
            """;
        return tb;
    }
}
record Pt(int x, int y) { Pt { if (x < 0) throw new IllegalArgumentException(); } int sum() { return x + y; } }
