import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

public class Main {
  public static void main(String[] args) {
    InputStream inputStream = System.in;
    OutputStream outputStream = System.out;
    FastScanner in = new FastScanner(inputStream);
    PrintWriter out = new PrintWriter(outputStream);
    DArmchairs solver = new DArmchairs();
    solver.solve(1, in, out);
    out.close();
  }

  static class DArmchairs {
    public void solve(int testNumber, FastScanner in, PrintWriter out) {
      int n = in.nextInt();
      int[] a = in.nextIntArray(n);

      int[] pos = new int[n];
      int ptr = 0;
      for (int i = 0; i < n; ++i) {
        if (a[i] == 1) {
          pos[ptr++] = i;
        }
      }
      pos = Arrays.copyOf(pos, ptr);

      int[][] dp = new int[n + 1][ptr + 1];
      ArrayUtils.fill(dp, Integer.MAX_VALUE);
      dp[0][0] = 0;
      for (int i = 0; i < n; ++i) {
        for (int j = 0; j <= ptr; ++j) {
          if (dp[i][j] == Integer.MAX_VALUE) continue;
          dp[i + 1][j] = Math.min(dp[i + 1][j], dp[i][j]);
          if (j < ptr && a[i] == 0) {
            dp[i + 1][j + 1] = Math.min(dp[i + 1][j + 1], dp[i][j] + Math.abs(pos[j] - i));
          }
        }
      }
      out.println(dp[n][ptr]);
    }
  }

  static class ArrayUtils {
    public static void fill(int[][] array, int value) {
      for (int[] row : array) {
        Arrays.fill(row, value);
      }
    }
  }

  static class FastScanner {
    BufferedReader reader;
    StringTokenizer tokenizer;

    public FastScanner(InputStream inputStream) {
      reader = new BufferedReader(new InputStreamReader(inputStream), 32768);
      tokenizer = null;
    }

    public String next() {
      while (tokenizer == null || !tokenizer.hasMoreTokens()) {
        try {
          tokenizer = new StringTokenizer(reader.readLine());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return tokenizer.nextToken();
    }

    public int nextInt() {
      return Integer.parseInt(next());
    }

    public int[] nextIntArray(int n) {
      int[] arr = new int[n];
      for (int i = 0; i < n; i++) {
        arr[i] = nextInt();
      }
      return arr;
    }
  }
}
