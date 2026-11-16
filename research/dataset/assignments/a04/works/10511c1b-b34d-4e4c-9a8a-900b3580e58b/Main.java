
import java.io.*;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.compare;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.Collections.max;
import static java.util.Collections.min;

public class Main {
    private static final int MOD = (int) (1E9 + 7);
    private static final int INF = (int) (1E9);
    static FastScanner scanner = new FastScanner(in);

    public static void main(String[] args) throws IOException {
        // Write your solution here
        StringBuilder answer = new StringBuilder();
        int t = 1;
        t = parseInt(scanner.nextLine());
        while (t-- > 0) {
            answer.append(solve()).append("\n");
        }
//        out.println(answer);
    }

    private static Object solve() throws IOException {
       int n = scanner.nextInt();
       int m = scanner.nextInt();
       int x = scanner.nextInt();
       Integer[] h = new Integer[n];
       for (int i = 0; i < n; i++) {
          h[i] = scanner.nextInt();
       }
       TreeSet<Pair> q = new TreeSet<>();
       PriorityQueue<Pair> height = new PriorityQueue<>();
       for (int i = 0; i < m; i++) {
          q.add(new Pair(i + 1,0));
       }
       for (int i = 0; i < n; i++) {
          height.add(new Pair(i,h[i]));
       }
       boolean shift = false;
       int size = 0;
       while(!height.isEmpty()){
          Pair p = height.poll();
          Pair building  = (shift) ? q.pollLast() : q.pollFirst();
          h[p.index] = building.index;
          q.add(new Pair(building.index,building.value + p.value));
           //out.println("q = " + q);
          if(++size == n){
              size = 0;
              shift = !shift;
          }
       }
       if(safe(q,x)){
          out.println("YES");
          StringBuilder stringBuilder = new StringBuilder();
          for (Object o : h)
             stringBuilder.append(o).append(" ");
          out.println(stringBuilder);
       }
       else {
           out.println("NO");
       }
        return "";
    }

   private static boolean safe(TreeSet<Pair> q, int x) {
       int last = q.pollFirst().value;
       while (!q.isEmpty()){
          Pair p = q.pollFirst();
          if(p.value - last > x) return false;
          last = p.value;
       }
       return true;
   }

   private static class Pair implements Comparable<Pair> {
        int index, value;

        public Pair(int index, int value) {
            this.index = index;
            this.value = value;
        }

        public int compareTo(Pair o) {
            if (value != o.value) return compare(value, o.value);
            return compare(index, o.index);
        }

       @Override
       public String toString() {
           return "Pair{" +
                   "index=" + index +
                   ", value=" + value +
                   '}';
       }
   }

    private static int maxx(Integer... a) {
        return max(asList(a));
    }

    private static int minn(Integer... a) {
        return min(asList(a));
    }

    private static long maxx(Long... a) {
        return max(asList(a));
    }

    private static long minn(Long... a) {
        return min(asList(a));
    }

    private static int add(int a, int b) {
        long res = ((long) (a + MOD) % MOD + (b + MOD) % MOD) % MOD;
        return (int) res;
    }

    private static int mul(int a, int b) {
        long res = ((long) a % MOD * b % MOD) % MOD;
        return (int) res;
    }


    private static int gcd(int a, int b) {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }

    private static int lcm(int a, int b) {
        return (a / gcd(a, b)) * b;
    }

    static class FastScanner {
        BufferedReader br;
        StringTokenizer st;

        FastScanner(File f) {
            try {
                br = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        FastScanner(InputStream f) {
            br = new BufferedReader(new InputStreamReader(f));
        }

        String nextLine() {
            try {
                return br.readLine();
            } catch (IOException e) {
                return null;
            }
        }

        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return parseInt(next());
        }

        long nextLong() {
            return parseLong(next());
        }

        double nextDouble() {
            return parseDouble(next());
        }

    }
}
