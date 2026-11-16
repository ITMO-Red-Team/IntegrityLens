import java.util.*;
import java.io.*;

public class codeforces {
    static class Pair {
        char type;
        int L;
        int R;

        Pair(char type, int L, int R) {
            this.type = type;
            this.L = L;
            this.R = R;
        }
    }

    static FastReader fr;
    static StringBuilder res;

    static class FastReader {

        BufferedReader br;
        StringTokenizer st;
        BufferedWriter bw;

        public FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
            bw = new BufferedWriter(new OutputStreamWriter(System.out));
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

        char nextChar() {
            return next().charAt(0);
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

        void write(String str) {

            try {
                bw.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void close() {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static void main(String[] args) throws java.lang.Exception {
        fr = new FastReader();
        boolean fixed = false;
        int t = !fixed ? fr.nextInt() : 1;
        res = new StringBuilder();
        while (t-- > 0) {
            solve();
        }
        fr.write(res.toString());
        fr.close();
    }

    static int M = 1000008;
    static boolean[] primes = new boolean[M];
    static int[] dp = new int[M];

    public static void sieve() {
        for (int i = 2; i * i <= M; i++) {
            if (!primes[i]) {
                for (int j = i * i; j < M; j += i) {
                    primes[j] = true;
                }
            }
        }

        for (int i = 5; i < 1000001; i++) {
            if (!primes[i] && !primes[i - 2])
                dp[i] = dp[i - 1] + 1;
            else
                dp[i] = dp[i - 1];
        }
    }

    public static void solve() {
        int n = fr.nextInt();
        String[] arr = new String[n];
        for (int i = 0; i < n; i++) {
            arr[i] = fr.nextLine();
        }
        char[] chars = { 'a', 'b', 'c', 'd', 'e' };
        int ans = 0;
        for (int i = 0; i < 5; i++) {
            char ch = chars[i];
            List<Integer> a = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                String s = arr[j];
                int c = 0;
                for (int k = 0; k < s.length(); k++) {
                    if (s.charAt(k) == ch)
                        c++;
                }
                a.add(2 * c - s.length());
            }
            // System.out.println(a);
            Collections.sort(a, Collections.reverseOrder());
            int sum = 0, len = 0;
            for (int k = 0; k < a.size(); k++) {
                if (sum + a.get(k) <= 0) {
                    break;
                } else {
                    len++;
                    sum += a.get(k);
                }
            }
            ans = Math.max(ans, len);
        }
        res.append(ans + "\n");
    }

    public static int[] readArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = fr.nextInt();
        }
        return arr;
    }

    public static long gcd(long a, long b) {
        if (a == 0)
            return b;
        return gcd(b % a, a);
    }

    public static List<Integer> readArrayList(int n) {
        List<Integer> arr = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            arr.add(fr.nextInt());
        }
        return arr;
    }
}
