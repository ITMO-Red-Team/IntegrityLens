import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.*;

public class ArmChairs {
    static final Random random = new Random();

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
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

        int[] readArray(int n, int size) {
            int[] a = new int[size];
            for (int i = 0; i < n; i++) {
                a[i] = this.nextInt();
            }

            return a;
        }

        long[] readLongArray(int n, int size) {
            long[] a = new long[size];
            for (int i = 0; i < n; i++) {
                a[i] = this.nextLong();
            }
            return a;
        }
    }

    static void ruffleSort(long[] a) {
        int n = a.length;//shuffle, then sort
        for (int i = 0; i < n; i++) {
            int oi = random.nextInt(n);
            long temp = a[oi];
            a[oi] = a[i];
            a[i] = temp;
        }
        Arrays.sort(a);
    }

    static void ruffleSort(int[] a) {
        int n = a.length;//shuffle, then sort
        for (int i = 0; i < n; i++) {
            int oi = random.nextInt(n), temp = a[oi];
            a[oi] = a[i];
            a[i] = temp;
        }
        Arrays.sort(a);
    }

    public static void main(String[] args) {
        FastReader fs = new FastReader();
        int t = 1;

        for (int z = 0; z < t; z++) {
            int n = fs.nextInt();
            List<Integer> empty = new ArrayList<>();
            List<Integer> chairs = new ArrayList<>();
            for(int i = 0; i < n; i++) {
                int status = fs.nextInt();
                if(status == 1) chairs.add(i+1);
                else empty.add(i+1);
            }
            int[][] dp = new int[empty.size() + 1][chairs.size() + 1];
            dp[0][0] = 0;
            for(int i = 1; i <= chairs.size(); i++) dp[0][i] = (int)3e+8;
            for(int i = 1; i <= empty.size(); i++) {
                for(int j = 1; j <= chairs.size(); j++) {
                    // Shift jth person to ith chair
                    dp[i][j] = dp[i-1][j-1] + Math.abs(empty.get(i-1) - chairs.get(j-1));
                    dp[i][j] = Math.min(dp[i][j], dp[i-1][j]);
                }
                //System.out.println(i + " " + Arrays.toString(dp[i]));
            }
            //System.out.println(empty.size() + " " + chairs.size());
            System.out.println(dp[empty.size()][chairs.size()]);
        }
    }
}