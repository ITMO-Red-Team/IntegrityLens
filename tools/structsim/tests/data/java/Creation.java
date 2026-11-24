import java.util.*;
import java.util.function.*;
public class Creation {
    int ops(int x, int y) {
        Object o = new Object() { int f = 1; };
        List<Integer> li = new ArrayList<>(List.of(1, 2));
        Comparator<Integer> cmp = new Comparator<Integer>() { public int compare(Integer p, Integer q) { return p - q; } };
        Runnable r = () -> { int t = 1; };
        BiFunction<Integer, Integer, Integer> add = (var p, var q) -> p + q;
        x = this.<Integer>gen(1);
        x = new int[]{1, 2}[0];
        x = g[0].length;
        x = "abc".length();
        x = (int) Math.sqrt(4);
        return x;
    }
}
