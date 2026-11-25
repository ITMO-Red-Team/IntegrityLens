import java.util.*;
import java.io.*;
public class Mixed {
    static int A = 1, B;
    static final int[] DX = {1, -1};
    static { B = 5; }
    int fld;
    Mixed() { this(1); }
    Mixed(int v) { this.fld = v; }
    abstract static class Base { abstract int h(); int k() { return 2; } }
    interface I { int q(int a); default int w() { return 0; } }
    static int f(int a, int b) {
        int x = 0, y, z = 2;
        int arr[] = new int[a];
        x = y = 0;
        x = -1;
        x = a + b * (a - b);
        long w = 0L + 1L + 2L + 3L + 0x1F;
        double d = 2.0 + 0.5;
        char c = 'a';
        String s = "a" + x + 'b';
        x = arr[a][b];
        x = this2.fld;
        x = sc.next().charAt(0);
        x = Collections.<Integer>max(l);
        Collections.sort(list, new Comparator<Integer>() { public int compare(Integer p, Integer q) { return p - q; } });
        outer:
        for (int i = 0; i < a; i++) { for (int j = 0; j < b; j++) { if (j == 2) break outer; if (j == 3) continue outer; } }
        assert x > 0;
        synchronized (s) { x = 1; }
        var v = 5;
        switch (s) { case "a" -> x = 1; case "b" -> { x = 2; } default -> x = 3; }
        x = switch (a) { case 1 -> 5; default -> 6; };
        switch (a) { case 1: { x = 1; } }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) { x = 1; } catch (IOException | RuntimeException e) { x = 2; }
        x = a > 0 ? (b > 0 ? 1 : 2) : 3;
        if (a > 0) x = 1;
        if (a > 0) { } else { if (b > 0) { x = 1; } }
        if (a > 0) { x = 1; } else { if (b > 0) { x = 1; } x = 2; }
        return x;
    }
    public static void main(String[] args) throws IOException {
        new Mixed().run();
    }
    void run() { System.out.println(f(1, 2)); }
}
class Second {
    static int g(int n) { return n > 1 ? g(n - 1) : 1; }
}
