import java.io.*;
import java.util.*;
public class C {
    public static void main (String[] args) throws IOException {
        BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(f.readLine());
        int t = Integer.parseInt(st.nextToken());
        while (t-->0) {
            st = new StringTokenizer(f.readLine());
            int n = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(f.readLine());
            long[] arr = new long[n];
            for (int i = 0; i < n; i++) {
                arr[i] = Long.parseLong(st.nextToken());
            }
            solve(n, arr);
        }
    }
    static void solve(long n, long[] arr) {
        long minEven = Integer.MAX_VALUE;
        long minOdd = arr[0];
        long evenSum = 0;
        long oddSum = arr[0];
        long finans = Long.MAX_VALUE;
        long oddAns, evenAns;
        long oddcount=1;
        long evencount=0;
        for (int k = 1; k < n; k++) {
            if (k%2==1) {
                evenSum+=arr[k];
                evencount++;
                minEven = Math.min(minEven, arr[k]);
            } else {
                oddSum+=arr[k];
                oddcount++;
                minOdd = Math.min(minOdd, arr[k]);
            }
            oddAns = oddSum+(n-oddcount)*minOdd;
            evenAns = evenSum+(n-evencount)*minEven;
            finans = Math.min(finans, oddAns+evenAns);
        }
        System.out.println(finans);
    }
}

