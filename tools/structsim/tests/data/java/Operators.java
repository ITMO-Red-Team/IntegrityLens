public class Operators {
    int ops(int x, int y) {
        x >>>= 2; x <<= 1; x ^= y; x |= 1; x &= 3; x %= 5; x /= 2; x *= 3; x -= 1;
        x = -(-x);
        boolean b = !(x > 0 && y < 0);
        char c = 'a'; c += 1; int ci = c - 'a';
        long big = 9223372036854775807L;
        int hex = 0x0, bin = 0b10, oct = 01, und = 1_000;
        double e1 = 1e0, h = 0x1p1, f2 = 2.f, dd = .5;
        x = Integer.MAX_VALUE;
        x = (y);
        return x > y ? x : y > 0 ? y : 0;
    }
}
