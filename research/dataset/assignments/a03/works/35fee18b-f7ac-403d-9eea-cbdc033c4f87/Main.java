import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {

    static String solve(int n, int k, int[] a, int[] t) {
        Pair[] pairs = new Pair[k];
        for (int i = 0; i < k; i++) {
            pairs[i] = new Pair(a[i], t[i]);
        }
        Arrays.sort(pairs);
        int[] ret = new int[n + 1];
        Arrays.fill(ret, Integer.MAX_VALUE);

        int pIdx = 0;
        int ct = pairs[pIdx].t;
        ret[pairs[pIdx].a] = ct;
        for (int i = pairs[pIdx].a + 1; i <= n; i++) {
            ct++;
            if (pIdx + 1 < k && pairs[pIdx + 1].a == i) {
                if (ct > pairs[pIdx + 1].t) {
                    ct = pairs[pIdx + 1].t;
                }
                pIdx++;
            }
            ret[i] = ct;
            // System.out.println(Arrays.toString(ret));
        }
        // System.out.println();
        pIdx = k - 1;
        ct = pairs[pIdx].t;
        for (int i = pairs[pIdx].a - 1; i > 0; i--) {
            ct++;
            if (pIdx - 1 >= 0 && pairs[pIdx - 1].a == i) {
                if (ct > pairs[pIdx - 1].t) {
                    ct = pairs[pIdx - 1].t;
                }
                pIdx--;
            }
            if (ct < ret[i]) {
                ret[i] = ct;
            }
            // System.out.println(Arrays.toString(ret));
        }
        StringBuilder out = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            out.append(ret[i]).append(" ");
        }
        // System.out.println();
        // System.out.println();
        return out.toString();
    }

    static class Pair implements Comparable<Pair> {
        int a, t;

        public Pair(int a, int t) {
            this.a = a;
            this.t = t;
        }

        @Override public int compareTo(Pair o) {
            return this.a - o.a;
        }
    }


    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        StringBuilder out = new StringBuilder();
        int T = Integer.parseInt(st.nextToken());
        while (T-- > 0) {
            st = new StringTokenizer(br.readLine());
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(br.readLine());
            int[] a = new int[k];
            for (int i = 0; i < k; i++) {
                a[i] = Integer.parseInt(st.nextToken());
            }
            st = new StringTokenizer(br.readLine());
            int[] t = new int[k];
            for (int i = 0; i < k; i++) {
                t[i] = Integer.parseInt(st.nextToken());
            }

            out.append(solve(n, k, a, t)).append("\n");
        }
        System.out.println(out);
    }
}