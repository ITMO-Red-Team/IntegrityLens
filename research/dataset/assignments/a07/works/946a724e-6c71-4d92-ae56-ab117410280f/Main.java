import java.util.*;
import java.io.*;

public class _109 {
    public static void main(String[] args) {
        MyScanner sc = new MyScanner();
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
        int t = 1;
        while (t-- > 0) {
            int n = sc.nextInt();
            int [] a = new int[n];
            ArrayList<Integer> one = new ArrayList<>();
            ArrayList<Integer> zero = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                a[i] = sc.nextInt();
                if (a[i] == 0) zero.add(i);
                else one.add(i);
            }
            if (one.size() == 0) {
                out.println(0);
                continue;
            }
            int zeros = zero.size();
            int ones = one.size();
            long [][] dp = new long[zeros + 1][ones + 1];
            for (long [] arr: dp) Arrays.fill(arr, Integer.MAX_VALUE);
            for (int i = 0; i <= zeros; i++) dp[i][0] = 0;
            long res = Long.MAX_VALUE;
            for (int i = 1; i <= zeros; i++) {
                for (int j = 1; j <= ones; j++) {
                    int zeroIndex = zero.get(i - 1);
                    int oneIndex = one.get(j - 1);
                    dp[i][j] = Math.min(dp[i][j], dp[i - 1][j]);
                    dp[i][j] = Math.min(dp[i][j], dp[i - 1][j - 1] + Math.abs(zeroIndex - oneIndex));
                    if (j == ones) res = Math.min(res, dp[i][j]);
                }
            }
            out.println(res);
        }
        out.close();
    }



    static void sort(int[] a) {
        ArrayList<Integer> q = new ArrayList<>();
        for (int i : a) q.add(i);
        Collections.sort(q);
        for (int i = 0; i < a.length; i++) a[i] = q.get(i);
    }

    static void sort(long[] a) {
        ArrayList<Long> q = new ArrayList<>();
        for (long i : a) q.add(i);
        Collections.sort(q);
        for (int i = 0; i < a.length; i++) a[i] = q.get(i);
    }


    //-----------MyScanner class for faster input----------
    public static class MyScanner {
        BufferedReader br;
        StringTokenizer st;

        public MyScanner() {
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