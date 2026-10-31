import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

// Своя задача и шаблонные алгоритмы: быстрый ввод, нижняя граница, степень по модулю.
public class Main {
    static class FastReader {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }
    }

    static int lowerBound(int[] sorted, int key) {
        int low = 0;
        int high = sorted.length;
        while (low < high) {
            int mid = (low + high) / 2;
            if (sorted[mid] < key) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    static long modPow(long base, long exp, long mod) {
        long result = 1;
        base %= mod;
        while (exp > 0) {
            if (exp % 2 == 1) {
                result = result * base % mod;
            }
            base = base * base % mod;
            exp /= 2;
        }
        return result;
    }

    static int spiralWeight(int[][] grid, int size) {
        int total = 0;
        for (int layer = 0; layer < (size + 1) / 2; layer++) {
            for (int step = layer; step < size - layer; step++) {
                total += grid[layer][step] * (layer + 1);
                if (step != layer && step != size - layer - 1) {
                    total -= grid[size - layer - 1][step] / (layer + 1);
                }
            }
            if (total > 1000) {
                total = total % 997 + layer;
            }
        }
        return total;
    }

    public static void main(String[] args) {
        FastReader in = new FastReader();
        int size = in.nextInt();
        int[][] grid = new int[size][size];
        for (int[] row : grid) {
            for (int j = 0; j < size; j++) {
                row[j] = in.nextInt();
            }
        }
        System.out.println(modPow(spiralWeight(grid, size), size, 1_000_000_007L) + lowerBound(grid[0], size));
    }
}
