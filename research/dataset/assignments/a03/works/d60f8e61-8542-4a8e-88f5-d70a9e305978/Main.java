import java.io.*;
import java.util.*;

public class E {
  public static void main(String[] args) throws IOException {
    FastReader fr = new FastReader();
    PrintWriter pr = new PrintWriter(new OutputStreamWriter(System.out));
    int t = fr.nextInt();

    while (t-- > 0) {
      int n = fr.nextInt(), k = fr.nextInt();
      String[] arr = fr.nextLine().split(" ");
      String[] temp = fr.nextLine().split(" ");

      long[] L = new long[n];
      long[] R = new long[n];

      long[] c = new long[n];
      Arrays.fill(c, Long.MAX_VALUE);

      for (int i = 0; i < k; i++) {
        c[toInt(arr[i]) - 1] = toInt(temp[i]);
      }

      long p = Integer.MAX_VALUE;

      for (int i = 0; i < R.length; i++) {
        p = Math.min(p + 1, c[i]);
        L[i] = p;
      }

      p = Integer.MAX_VALUE;

      for (int i = n - 1; i >= 0; i--) {
        p = Math.min(p + 1, c[i]);
        R[i] = p;
      }

      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < R.length; i++) {
        sb.append(Math.min(R[i], L[i]) + " ");
      }

      pr.println(sb.toString().trim());
    }

    pr.close();
  }

  static class Pair {
    int x, y;

    public Pair(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  static int toInt(String s) {
    return Integer.parseInt(s);
  }

  static class FastReader {
    BufferedReader br;
    StringTokenizer st;

    public FastReader() throws FileNotFoundException {
      br = new BufferedReader(new InputStreamReader(System.in));
    }

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

    long nextLong() {
      return Long.parseLong(next());
    }

    double nextDouble() {
      return Double.parseDouble(next());
    }

    String nextLine() {
      String str = "";
      try {
        str = br.readLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return str;
    }
  }
}
