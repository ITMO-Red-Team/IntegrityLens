public class Members {
    int[] a = {};
    static int[][] g = new int[][]{{1}, {2, 3}};
    { a = new int[3]; }
    static class Nested { static int s; static { s = 7; } int m() { return s; } }
    enum E { A(1) { int v() { return 2; } }, B(2); final int x; E(int x) { this.x = x; } int v() { return x; } static E of(int k) { return k == 1 ? A : B; } }
    interface J { static int sm() { return 1; } default int dm() { return 2; } }
    abstract int abs(int q);
    <T> T gen(T t, int... rest) { return t; }
    static void Main() { int z = 1; }
    void main(int k) { k++; }
    int ops2(int z) { return ops2(z) + this.ops2(z) + Members.this.ops2(z); }
    Members() { super(); }
    Members(int v) { this(); }
}
class Two { int t(int n) { return n; } public static void main(String... a) { } }
interface Three { int q(); }
