import java.util.*;
import java.io.*;

public class Test {
    // global variables
    static final long INF = 1000000000000000000L;

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() throws FileNotFoundException {
            br = new BufferedReader(
                    new InputStreamReader(System.in));
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

    static FastReader s;

    public static void main(String[] args)
            throws IOException {
        if (System.getProperty("ONLINE_JUDGE") == null) {

            PrintStream ps = new PrintStream(new File("output.txt"));
            InputStream is = new FileInputStream("input.txt");
            System.setIn(is);

            System.setOut(ps);
        }
        long t = 1;
        s = new FastReader();

        t = s.nextLong();

        for (int i = 1; i <= t; i++)
            solve();

    }

    public static void solve() throws IOException {
        long n = s.nextLong();
        ArrayList<Long> k = new ArrayList<Long>((int)n + 1);
        k.add(0L);
        ArrayList<Long> h = new ArrayList<Long>((int) n + 1);
        h.add(0L);
        ArrayList<Long> dp = new ArrayList<Long>((int) n + 1);
        dp.add(0L);
        for (int i = 1; i <= n; i++) {
            long k_ = s.nextLong();
            k.add(k_);
        }
        for (int i = 1; i <= n; i++) {
            long h_ = s.nextLong();
            h.add(h_);
            dp.add(0L);
        }
        for (int i = 1; i <= n; i++) {
            long var = k.get(i) - h.get(i) + 1;
            for (int j = i - 1; (j > 0 && k.get(j) >= var); j--) {
                long var2 = k.get(j) - h.get(j) + 1;
                if (var2 <= var) {
                    var = var2;
                }
            }
            int j = i - 1;
            for (; j > 0; j--) {
                if (k.get(j) < var) {
                    break;
                }
            }
            long var3 = k.get(i) - var + 1;
            if (j != 0) {
                dp.set(i, (dp.get(j) + (var3 * (var3 + 1)) / 2));
            } else
            {
                dp.set(i, ((var3 * (var3 + 1)) / 2));
            }

        }
        System.out.println(dp.get((int) n));
    }
}
