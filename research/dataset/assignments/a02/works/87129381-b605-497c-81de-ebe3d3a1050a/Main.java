import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;

public class C {
    public static void main(String[] args) {
        FastScanner scanner = new FastScanner();
        int t = scanner.nextInt();
        for (int i=0; i < t;i++) {
            int n = scanner.nextInt();
            String start = scanner.nextLine().trim();
            String finish = scanner.nextLine().trim();
            int ans = solve(start, finish, n);
            System.out.println(ans);
        }
    }

    public static int solve(String start, String finish, int n) {
        int nOnesInA = 0;
        for (char c : start.toCharArray()) {
            if (c == '1') {
                nOnesInA++;
            }
        }
        int nOnesInB = 0;
        for (char c : finish.toCharArray()) {
            if (c == '1') {
                nOnesInB++;
            }
        }
        int ans = Integer.MAX_VALUE;
        if (nOnesInA == nOnesInB) {
            ans = simpleSolve(start, finish, n);
        }
        if ((n - nOnesInA) + 1 == nOnesInB) {
            int newAns = simpleSolve(flip(start, n), finish, n) + 1;
            if (newAns < ans) {
                ans = newAns;
            }
        }
        if (ans == Integer.MAX_VALUE) {
            return -1;
        }
        return ans;
    }

    public static int simpleSolve(String start, String finish, int n) {
        int ans = 0;
        for (int i=0; i < n; i++) {
            if ((start.charAt(i) == '1') && (finish.charAt(i) == '0')) {
                ans+=2;
            }
        }
        return ans;
    }

    public static String flip(String start, int n) {
        char[] flipped = new char[n];
        for(int i=0; i<n; i++) {
            if (start.charAt(i) == '1') {
                flipped[i] = '0';
            } else {
                flipped[i] = '1';
            }
        }
        return String.valueOf(flipped);
    }

    public static class FastScanner {
        BufferedReader br;
        StringTokenizer st;

        public FastScanner() {
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