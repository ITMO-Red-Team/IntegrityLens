import java.io.*;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class C {
    Reader source;
    BufferedReader br;
    StringTokenizer in;
    PrintWriter out;

    public String nextToken() throws Exception {
        while (in == null || !in.hasMoreTokens()) {
            in = new StringTokenizer(br.readLine());
        }
        return in.nextToken();
    }
    public int nextInt() throws Exception {
        return Integer.parseInt(nextToken());
    }
    public long nextLong() throws Exception {
        return Long.parseLong(nextToken());
    }
    public double nextDouble() throws Exception {
        return Double.parseDouble(nextToken());
    }
    public void run() throws Exception {
        source = OJ ? new InputStreamReader(System.in) : new FileReader("C.in");
        br = new BufferedReader(source);
        out = new PrintWriter(System.out);
        solve();
        out.flush();
    }
    public void sort(int[] a) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : a) list.add(i);
        Collections.sort(list);
        for (int i = a.length - 1, j = 0; i >= 0; i--, j++)
            a[j] = list.get(i);
    }
    public static void main(String[] args) throws Exception {
        new C().run();
    }
    private boolean OJ = System.getProperty("ONLINE_JUDGE") != null;

    public void solve() throws Exception {
        int t = nextInt();
        while (t-- > 0) {
            int n = nextInt();
            int A[] = new int [n];
            int B[] = new int [n];
            int C[] = new int [n];
            int D[] = new int [n];
            int E[] = new int [n];
            for (int i = 0; i < n; i++) {
                String s = nextToken();
                int sz = s.length();
                int a = 0, b = 0, c = 0, d = 0, e = 0;
                for (int j = 0; j < sz; j++) {
                    if (s.charAt(j) == 'a') a++;
                    if (s.charAt(j) == 'b') b++;
                    if (s.charAt(j) == 'c') c++;
                    if (s.charAt(j) == 'd') d++;
                    if (s.charAt(j) == 'e') e++;
                }
                A[i] = a - b - c - d - e;
                B[i] = b - a - c - d - e;
                C[i] = c - a - b - d - e;
                D[i] = d - a - b - c - e;
                E[i] = e - a - b - c - d;
            }
            int ans = 0, sum;
            sum = 0;
            sort(A);
            for (int j = 0; j < n; j++) {
                sum += A[j];
                if (sum <= 0) {
                    ans = max(ans, j);
                    break;
                }
            }
            if (sum > 0) ans = n;
            sum = 0;
            sort(B);
            for (int j = 0; j < n; j++) {
                sum += B[j];
                if (sum <= 0) {
                    ans = max(ans, j);
                    break;
                }
            }
            if (sum > 0) ans = n;
            sum = 0;
            sort(C);
            for (int j = 0; j < n; j++) {
                sum += C[j];
                if (sum <= 0) {
                    ans = max(ans, j);
                    break;
                }
            }
            if (sum > 0) ans = n;
            sum = 0;
            sort(D);
            for (int j = 0; j < n; j++) {
                sum += D[j];
                if (sum <= 0) {
                    ans = max(ans, j);
                    break;
                }
            }
            if (sum > 0) ans = n;
            sum = 0;
            sort(E);
            for (int j = 0; j < n; j++) {
                sum += E[j];
                if (sum <= 0) {
                    ans = max(ans, j);
                    break;
                }
            }
            if (sum > 0) ans = n;
            System.out.println(ans);
        }
    }
}