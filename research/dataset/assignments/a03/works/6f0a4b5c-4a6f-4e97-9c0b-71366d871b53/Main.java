import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class AirConditioners {

    static FastReader read  =new FastReader();
    static int INF = (int)(2e9);
    static int mxn = (int)(3e5 + 10);
    public static void main(String[] args) {

        int t = read.nextInt();
        while (t-- > 0)
            solve();
    }

    private static void solve() {

        int n = read.nextInt(), k = read.nextInt();

        int[] a = new int[n];
        int[] t = new int[n];

        Arrays.fill(t, Integer.MAX_VALUE);

        for (int i=0;i<k;++i){
            a[i] = read.nextInt();
        }

        for (int i=0;i<k;++i){
            t[--a[i]] = read.nextInt();
        }

        int[] L = new int[n];
        int[] R = new int[n];

        int tmp = INF;
        for (int i=0;i<n;++i){
            tmp = Math.min(tmp+1, t[i]);
            L[i] = tmp;
        }

        tmp = INF;

        for (int i=n-1;i>=0;--i){
            tmp = Math.min(tmp+1, t[i]);
            R[i] = tmp;
        }


        for (int i=0;i<n;++i){
            int ans = Math.min(L[i], R[i]);
            System.out.print(ans + " ");
        }

        System.out.println();

    }

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
        }

        public String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }

        public long nextLong() {
            return Long.parseLong(next());
        }

        public double nextDouble() {
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

        String[] strArray() {
            String[] str = nextLine().split(" ");
            return str;
        }

        int[] intArray(int n) {
            String[] str = strArray();
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) {
                arr[i] = Integer.parseInt(str[i]);
            }
            return arr;
        }
    }

}
