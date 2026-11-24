import java.util.function.*;
public class Statements {
    int ops(int x, int y) {
        Class<?> k1 = int.class; Class<?> k2 = String.class;
        Supplier<int[]> mk = int[]::new;
        Function<Integer, Integer> self = this::ops2;
        for (int i = 0, j = 1; i < j; i++, j--) { }
        for (int i = 0; ; ) { break; }
        for (; ; x++) { break; }
        for (; x < 3; x++) { }
        do x--; while (x > 0);
        lbl: { if (x > 0) break lbl; }
        ;;
        assert x > 0 : "msg";
        if (x > 0) { } else /* c */ { if (y > 0) { } }
        if (x > 0) { } else { { if (y > 0) { } } }
        if (x > 0) { } else if (y > 0) { } else if (y < 0) { } else { }
        return x;
    }
}
