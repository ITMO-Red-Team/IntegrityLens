import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        SolveQue solveQue = new SolveQue();
        solveQue.ques();
    }
}

class SolveQue {
    static class FastScanner {
        BufferedReader br;
        StringTokenizer st;

        public FastScanner()
        {
            br = new BufferedReader(
                    new InputStreamReader(System.in));
        }

        String next()
        {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() { return Integer.parseInt(next()); }

        long nextLong() { return Long.parseLong(next()); }

        double nextDouble()
        {
            return Double.parseDouble(next());
        }

        String nextLine()
        {
            String str = "";
            try {
                str = br.readLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
    private static final FastScanner fs = new FastScanner();
    private static final Scanner sc = new Scanner(System.in);
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private static final long MOD = (long) (1e9 + 7);
    private static PrintWriter out = new PrintWriter(System.out);
    private static BufferedWriter output = new BufferedWriter(new OutputStreamWriter(System.out));
    private static final int iInf = Integer.MAX_VALUE - (int) (1e6);
    private static final long lInf = (long)(1e17);
    private static int[] phi;

    /** Optimal(Maximum,Minimum) Answers
     * 1. Binary search
     * 2. Prefix Suffix
     * 3. Greedy (sorting searching)
     * 4. DP
     **/
    long[] getP(long[] arr,int n) {
        long mn = lInf;
        long[] P = new long[n];
        Arrays.fill(P,lInf);
        long cnt = 0;
        for(int i = 0 ; i < n; i++){
            long curr = arr[i];
            if(mn + cnt < curr){
                P[i] = mn + cnt;
            } else {
                mn = arr[i];
                P[i] = arr[i];
                cnt  = 0;
            }
            cnt++;
        }
        return  P;
    }
    long[] getS(long[] arr,int n) {
        long mn = lInf;
        long[] S = new long[n];
        Arrays.fill(S,lInf);
        long cnt = 0;
        for(int i = n - 1; i >= 0; i--){
            long curr = arr[i];
            if(mn + cnt < curr){
                S[i] = mn + cnt;
            }else{
                mn = arr[i];
                S[i] = arr[i];
                cnt  = 0;
            }
            cnt++;
        }
        return  S;
    }
    void solve(int T) throws IOException {
        int n = fs.nextInt();
        int k = fs.nextInt();
        int[] a = new int[k];
        long[] t = new long[k];
        long[] arr = new long[n];
        Arrays.fill(arr,lInf);
        for (int i = 0; i < k; i++) {
            a[i] = fs.nextInt();
            a[i]--;
        }
        for (int i = 0; i < k; i++) {
            t[i] = fs.nextLong();
            arr[a[i]] = t[i];
        }
//        Arrays.fill(arr,lInf);
        long[] P = getP(arr, n);
//        Arrays.fill(arr,lInf);
        long[] S = getS(arr, n);
        for(int i = 0 ; i < n ; i++){
            System.out.print(Math.min(P[i],S[i]) + " ");
        }
        System.out.println();
    }
    void ques() throws IOException {
        int t = 1;
//        t = sc.nextInt();
        t = fs.nextInt();
//        t = Integer.parseInt(br.readLine());
        int tt = 1;
        while (t-- > 0) {
            solve(tt);
            tt++;
        }
        System.gc();
    }
}