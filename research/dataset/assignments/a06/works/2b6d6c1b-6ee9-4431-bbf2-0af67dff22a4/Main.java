import java.io.*;
import java.util.*;
public class A {
    static FastScanner fs;
    public static void main(String[] args) {
        fs=new FastScanner();
        int t = fs.nextInt();
        while (t-->0)
            solve();
    }
    public static void solve() {
        int n = fs.nextInt();
        int[] c = fs.readArray(n);
        long sumEven = c[0];
        long minEven = c[0];
        long sumOdd = c[1];
        long minOdd = c[1];
        long ans = n*minOdd+n*minEven;
        for (int i=2; i<n; i++) {
            if (i%2==0) {
                sumEven+=c[i];
                minEven = Math.min(minEven, c[i]);
            }
            else {
                sumOdd+=c[i];
                minOdd = Math.min(minOdd, c[i]);
            }
            ans = Math.min(ans, sumEven+(n-i/2-1)*minEven+sumOdd+(n-(i+1)/2)*minOdd);
        }
        System.out.println(ans);
    }
    static void ruffleSort(int[] a) {
        int n=a.length;//shuffle, then sort
        for (int i=0; i<n; i++) {
            int oi=random.nextInt(n), temp=a[oi];
            a[oi]=a[i]; a[i]=temp;
        }
        Arrays.sort(a);
    }
    static final Random random =new Random();
    static class FastScanner {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st=new StringTokenizer("");
        String next() {
            while (!st.hasMoreTokens())
                try {
                    st=new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }
        int[] readArray(int n) {
            int[] a=new int[n];
            for (int i=0; i<n; i++) a[i]=nextInt();
            return a;
        }
        long nextLong() {
            return Long.parseLong(next());
        }
    }

    static int[] reverse(int[] a) {
        int n=a.length;
        int[] res=new int[n];
        for (int i=0; i<n; i++) res[i]=a[n-1-i];
        return res;
    }
}