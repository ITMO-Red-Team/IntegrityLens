import java.io.*;
import java.util.*;

public class Code {

  public static void main(String[] args) {
    FastReader in = new FastReader();
    PrintWriter out = new PrintWriter(System.out);

    int t = in.nextInt();
    while (t-- > 0) {
      int n = in.nextInt();
      long a[] = new long[n];
      for (int i = 0; i < n; i++) {
        a[i] = in.nextLong();
      }
      long result = Long.MAX_VALUE;
      for (int i = 0; i < 2; i++) {
        long x = 0;
        long y = 0;
        long minX = Long.MAX_VALUE;
        long minY = Long.MAX_VALUE;
        long prefix = 0;
        for (int j = 0; j < n; j++) {
          if (j % 2 == i) {
            if (j > 0) {
              result = Math.min(result, (n - x) * a[j] + (n - y) * minY + prefix);
            }
            ++x;
            prefix += a[j];
            minX = Math.min(minX, a[j]);
          } else {
            if (j > 0) {
              result = Math.min(result, (n - y) * a[j] + (n - x) * minX + prefix);
            }
            ++y;
            prefix += a[j];
            minY = Math.min(minY, a[j]);
          }
        }
      }
      out.println(result);
    }

    out.close();
  }

  static void shuffle(int[] a) {
    Random get = new Random();
    for (int i = 0; i < a.length; i++) {
      int r = get.nextInt(a.length);
      int temp = a[i];
      a[i] = a[r];
      a[r] = temp;
    }
  }

  static void shuffle(long[] a) {
    Random get = new Random();
    for (int i = 0; i < a.length; i++) {
      int r = get.nextInt(a.length);
      long temp = a[i];
      a[i] = a[r];
      a[r] = temp;
    }
  }

  static class FastReader {
    BufferedReader br;
    StringTokenizer st;

    public FastReader() {
      br = new BufferedReader(new InputStreamReader(System.in));
    }

    public FastReader(String s) throws FileNotFoundException {
      br = new BufferedReader(new FileReader(new File(s)));
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

  static class Tuple implements Comparable<Tuple> {
    int a;
    int b;
    int c;
    public Tuple(int a, int b) {
      this.a = a;
      this.b = b;
      this.c = 0;
    }
    public Tuple(int a, int b, int c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }
    public int getA() { return a; }
    public int getB() { return b; }
    public int getC() { return c; }
    public int compareTo(Tuple other) {
      if (this.a == other.a) {
        if (this.b == other.b) return Long.compare(this.c, other.c);
        return Long.compare(this.b, other.b);
      }
      return Long.compare(this.a, other.a);
    }
    @Override
    public int hashCode() { return Arrays.deepHashCode(new Integer[] {a, b, c}); }
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Tuple)) return false;
      Tuple pairo = (Tuple) o;
      return (this.a == pairo.a && this.b == pairo.b && this.c == pairo.c);
    }
    @Override
    public String toString() { return String.format("(%d %d %d)  ", this.a, this.b, this.c); }
  }
}
