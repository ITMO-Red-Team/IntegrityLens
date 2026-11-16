import java.io.*;
import java.util.*;

public class Codeforces {

    public static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader(String s) {
            try {
                br = new BufferedReader(new FileReader(s));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }

        String nextToken() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(nextToken());
        }

        long nextLong() {
            return Long.parseLong(nextToken());
        }

        double nextDouble() {
            return Double.parseDouble(nextToken());
        }
    }


    static FastReader f = new FastReader();
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringTokenizer st;
    static StringBuilder sb = new StringBuilder("");
    private static int m = (int) 1e9 + 7;
    static int MAX = 500005;
    static long[] fact;

    static int[] inputArray(int n) throws IOException {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = f.nextInt();
        }
        return a;
    }

    private static class SegmentTree {
        int[] array;
        int[] tree;
        int n;
        int max;

        SegmentTree(int a[], int n) {
            this.tree = new int[4 * n + 1];
            this.array = a;
            this.n = n;
            buildTree();
        }

        void buildTree() {
            buildTreeHelper(0, 0, n - 1);
        }

        void buildTreeHelper(int i, int start, int end) {
            if (start > end) {
                return;
            }
            if (start == end) {
                tree[i] = array[start];
                return;
            }
            int mid = (start + end) / 2;
            buildTreeHelper(2 * i + 1, start, mid);
            buildTreeHelper(2 * i + 2, mid + 1, end);
            tree[i] = Math.max(tree[2 * i + 1], tree[2 * i + 2]);
        }

        char overlap(int start, int end, int qs, int qe) {
            if (qe < start || qs > end || start > end) {
                return 0;
            }
            if (qs <= start && qe >= end) {
                return 2;
            }
            return 1;
        }

        int query(int start, int end) {
            return andQueryHelper(0, 0, n - 1, start, end);
        }

        int andQueryHelper(int i, int start, int end, int qs, int qe) {
            if (overlap(start, end, qs, qe) == 0) {
                return 0;
            }
            if (overlap(start, end, qs, qe) == 1) {
                int mid = (start + end) / 2;
                return Math.max(andQueryHelper(2 * i + 1, start, mid, qs, qe),
                        andQueryHelper(2 * i + 2, mid + 1, end, qs, qe));
            } else {
                return tree[i];
            }
        }
    }

    static int query(int l, int r) {
        System.out.println("? " + l + " " + r);
        System.out.flush();
        int res = f.nextInt();
        System.out.flush();
        return res;
    }

    static long gcd(long a , long b) {
        if(a == 0 || b == 0) {
            return Math.max(a , b);
        }
        //System.out.println("a - " + a + " b - " + b);
        if(a % b == 0) {
            return b;
        }
        return gcd(b , a % b);
    }

    public static void main(String[] args) throws IOException {

//        System.out.println(gcd(11, 3));

        int t = 1;
        while(t-- != 0) {
            int N = f.nextInt();
            int a[] = inputArray(N);
            ArrayList<Integer> space = new ArrayList<>();
            ArrayList<Integer> fill = new ArrayList<>();
            for(int i = 0 ; i < N;  i++) {
                if(a[i] == 0) {
                    space.add(i);
                }
                else {
                    fill.add(i);
                }
            }
            int n = fill.size(), m = space.size();
            int[][] dp = new int[n+1][m+1];
            // i - no of filled
            // j is no of space
            Arrays.fill(dp[0], 0);
            for(int i = 1 ; i <= n ; i++) {
                dp[i][0] = -1;
            }
            for(int i = 1 ; i <= n ; i++) {
                for(int j = 1 ; j <= m ; j++) {
                    if(i > j) {
                        dp[i][j] = -1;
                        continue;
                    }
                    dp[i][j] = Integer.MAX_VALUE;
                    if(dp[i-1][j-1] != -1) {
                        dp[i][j] = Math.min(dp[i][j], dp[i-1][j-1]) + Math.abs(fill.get(i-1) - space.get(j-1));
                    }
                    if(dp[i][j-1] != -1) {
                        dp[i][j] = Math.min(dp[i][j], dp[i][j-1]);
                    }
                }
            }

            System.out.println(dp[n][m]);

        }

        System.out.println(sb);

    }

}

/*
5
2 1
1 1
500 4
217871987498122 10
100000000000000001 1
 */


