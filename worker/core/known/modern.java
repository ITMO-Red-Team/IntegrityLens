import java.util.*;
import java.util.function.*;

public class Modern {
    sealed interface Shape permits Circle, Square { double area(); }
    record Circle(double r) implements Shape {
        Circle { if (r < 0) throw new IllegalArgumentException(); }
        public double area() { return Math.PI * r * r; }
        static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
    }
    record Square(double s) implements Shape { public double area() { return s * s; } }

    enum Mode {
        FAST(1) { @Override int cost() { return 1; } },
        SLOW(2) { @Override int cost() { return 2; } };
        final int level;
        Mode(int level) { this.level = level; }
        abstract int cost();
        static long modPow(long base, long exp, long mod) {
            long result = 1;
            base %= mod;
            while (exp > 0) {
                if (exp % 2 == 1) { result = result * base % mod; }
                base = base * base % mod;
                exp /= 2;
            }
            return result;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static int lowerBound(int[] sorted, int key) {
        int low = 0;
        int high = sorted.length;
        while (low < high) {
            int mid = (low + high) / 2;
            if (sorted[mid] < key) { low = mid + 1; } else { high = mid; }
        }
        return low;
    }

    @FunctionalInterface
    interface Op<T extends Comparable<T>> { T apply(T a, T b); default T twice(T a) { return apply(a, a); } }

    static final Map<String, Integer> TABLE = new HashMap<>() {{ put("a", 1); put("}", 2); }};
    static final Comparator<int[]> ORDER = new Comparator<>() {
        @Override public int compare(int[] a, int[] b) { return a[0] - b[0]; }
    };
    static final int[][] GRID = {{1, 2}, {3, 4}};
    static { TABLE.put("{", 3); }

    static void bubbleSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
    }

    static String describe(Object o) {
        String block = """
            текст } с { скобками "
            """;
        char brace = '{';
        return switch (o) {
            case Integer i when i > 0 -> "positive" + block + brace;
            case String s -> { String t = s.strip(); yield t + "}"; }
            default -> { Runnable r = () -> { System.out.print("x"); }; r.run(); yield "other"; }
        };
    }

    static class Inner<T> {
        private final List<T> items = new ArrayList<>();
        <R> List<R> map(Function<? super T, ? extends R> f) throws Exception {
            List<R> out = new ArrayList<>();
            for (T item : items) { out.add(f.apply(item)); }
            return out;
        }
        static boolean isPalindrome(String s) {
            int i = 0;
            int j = s.length() - 1;
            while (i < j) {
                if (s.charAt(i) != s.charAt(j)) { return false; }
                i++;
                j--;
            }
            return true;
        }
    }

    public static void main(String[] args) {
        Object anonymous = new Object() { int value() { return 7; } };
        System.out.println(describe(anonymous) + new Square(2).area() + Mode.FAST.cost());
    }
}
