import java.io.*;
import java.util.*;

public class P1529C {
  public static final int MAX = 100000;

  private static final List<Integer>[] adjacency = new List[MAX];
  private static final int[][] pair = new int[MAX][2];

  public static void main(String[] args) {
    SpeedScanner in = new SpeedScanner();

    for(int i = 0; i < MAX; ++i)
      adjacency[i] = new ArrayList<>();

    int t = in.nextInt();
    while(t-- != 0) {
      int nv = in.nextInt();
      for(int i = 0; i < nv; ++i) {
        pair[i][0] = in.nextInt();
        pair[i][1] = in.nextInt();
      }
      for(int i = 1; i < nv; ++i) {
        int x = in.nextInt() - 1, y = in.nextInt() - 1;
        adjacency[x].add(y);
        adjacency[y].add(x);
      }

      long[] alts = dfsTraverse(0, -1);
      System.out.println(alts[0] >= alts[1] ? alts[0] : alts[1]);

      for(int i = 0; i < nv; ++i)
        adjacency[i].clear();
    }
  }

  private static long[] dfsTraverse(int vertex, int parent) {
    long sumDiffL = 0, sumDiffR = 0;
    for(int child: adjacency[vertex]) {
      if(child == parent)
        continue;
      long[] temp = dfsTraverse(child, vertex);
      sumDiffL += Math.max(Math.abs(pair[vertex][0] - pair[child][0]) + temp[0],
          Math.abs(pair[vertex][0] - pair[child][1]) + temp[1]);
      sumDiffR += Math.max(Math.abs(pair[vertex][1] - pair[child][0]) + temp[0],
          Math.abs(pair[vertex][1] - pair[child][1]) + temp[1]);
    }
    return new long[]{sumDiffL, sumDiffR};
  }
}

class SpeedScanner {
  BufferedReader in;
  StringTokenizer splitter;

  public SpeedScanner() {
    in = new BufferedReader(new InputStreamReader(System.in));
  }

  String next() {
    while(splitter == null || !splitter.hasMoreElements())
      try {
        splitter = new StringTokenizer(in.readLine());
      } catch (IOException e) {
        e.printStackTrace();
      }
    return splitter.nextToken();
  }

  String nextLine() {
    String s = "";
    try {
      s = in.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return s;
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
}
