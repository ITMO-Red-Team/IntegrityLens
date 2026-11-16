import javax.print.DocFlavor;
import java.util.*;
import java.lang.*;
import java.io.*;

public class Solution {
    static int N = 5005;
    static int[] arr = new int[N];
    static long[][] memo = new long[N][N];
    static List<Integer> occupiedSeats = new ArrayList<>();
    static List<Integer> emptySeats = new ArrayList<>();
    static int n, occSize, empSize;
    public static void main(String[] args) throws java.lang.Exception {
        out = new PrintWriter(new BufferedOutputStream(System.out));
        sc = new FastReader();
        int test = 1;
        for (int t = 0; t < test; t++) {
            solve();
        }
        out.close();
    }

    private static void solve() {
        n = sc.nextInt();
        for (int i = 1; i <= n; i++) {
            arr[i] = sc.nextInt();
            if (arr[i] == 1) {
                occupiedSeats.add(i);
            }else {
                emptySeats.add(i);
            }
        }
        occSize = occupiedSeats.size();
        empSize = emptySeats.size();
        for (long[] memset : memo) {
            Arrays.fill(memset, -1);
        }
        out.println(minimumTime(0, 0));
    }

    private static long minimumTime(int occupied, int empty) {
        if (occupied == occSize) {
            return 0;
        }
        if (empty == empSize) {
            return Integer.MAX_VALUE;
        }
        if (memo[occupied][empty] != -1) {
            return memo[occupied][empty];
        }
        long curr = Math.abs(occupiedSeats.get(occupied) - emptySeats.get(empty)) + minimumTime(occupied + 1, empty + 1);
        curr = Math.min(curr, minimumTime(occupied, empty + 1));
        memo[occupied][empty] = curr;
        return curr;
    }

    public static FastReader sc;
    public static PrintWriter out;
    static class FastReader
    {
        BufferedReader br;
        StringTokenizer st;

        public FastReader()
        {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
        }

        String next()
        {
            while (st == null || !st.hasMoreElements())
            {
                try
                {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException  e)
                {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt()
        {
            return Integer.parseInt(next());
        }

        long nextLong()
        {
            return Long.parseLong(next());
        }

        double nextDouble()
        {
            return Double.parseDouble(next());
        }

        String nextLine()
        {
            String str = "";
            try
            {
                str = br.readLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return str;
        }
    }
}