import java.io.*;
import java.util.*;

public class Main {
    static class FastInput {
        BufferedReader br;
        StringTokenizer st;

        public FastInput() {
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

    public static long gcd(long a, long b) {
        if (b > a)
            return gcd(b, a);
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }

    public static void plist(ArrayList<Integer> arr) {
        for (int i = 0; i < arr.size(); i++) {
            System.out.print(arr.get(i) + " ");
        }
        System.out.println();
    }
    static long[][] dp = new long[5005][5005];
    static ArrayList<Integer> filled = new ArrayList<>();
    static ArrayList<Integer> vacant = new ArrayList<>();
    public static long calc(int i, int j){
        if(i >= filled.size())
            return 0;
        if(j >= vacant.size())
            return Integer.MAX_VALUE;
        if(dp[i][j] != -1)
            return dp[i][j];
        dp[i][j] = Math.min(Math.abs(filled.get(i) - vacant.get(j)) + calc(i+1, j+1), calc(i, j+1));
        return dp[i][j];
    }
    
    public static void run_case(FastInput s) {
        int n = s.nextInt();
        int[] arr = new int[n];
        for(int i = 0; i < n; i++){
            arr[i] = s.nextInt();
            if(arr[i] == 1){
                filled.add(i);
            } else {
                vacant.add(i);
            }
        }
        for(int i = 0; i < filled.size(); i++){
            for(int j = 0; j < vacant.size(); j++)
                dp[i][j] = -1;
        }
        System.out.println(calc(0, 0));
    }

    public static void main(String[] args) {
        FastInput s = new FastInput();
        run_case(s);
    }
}