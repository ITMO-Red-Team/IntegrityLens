import java.util.*;
public class Patterns {
    sealed interface Shape permits Circle, Square {}
    record Circle(double r) implements Shape {}
    record Square(double s) implements Shape {}
    static double area(Shape s) {
        if (s instanceof Circle c) { return c.r(); }
        if (s instanceof Square sq && sq.s() > 0) { return sq.s(); }
        return switch (s) {
            case Circle c when c.r() > 1 -> 1;
            case Circle(double r) -> r;
            case Square q -> { yield q.s(); }
        };
    }
    static int days(int m) {
        switch (m) { case 1, 3, 5 -> { return 31; } case 2 -> { return 28; } default -> { return 30; } }
    }
    static String old(String s) {
        switch (s) { case "a": case "b": return "x"; default: break; }
        int r = switch (s) { case "a": yield 1; default: yield 2; };
        try (var in = new Scanner(System.in); Scanner in2 = new Scanner(System.in)) { r = 1; } catch (IllegalStateException | NoSuchElementException e) { r = 2; } finally { r = 3; }
        synchronized (Patterns.class) { r++; }
        class Local { int z() { return 1; } }
        record LR(int q) {}
        interface LI {}
        throw new IllegalStateException("x" + r);
    }
}
