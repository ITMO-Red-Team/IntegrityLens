import java.io.*;
import java.util.*;


public class Main {
    static int i, j, k, n, m, t, y, x, sum = 0;
    static long mod = 998244353;
    static FastScanner fs = new FastScanner();
    static PrintWriter out = new PrintWriter(System.out);
    static String str;
    static long ans;

    public static void main(String[] args) {

        t = fs.nextInt();

        while (t-- > 0) {

            n = fs.nextInt();

            int [] k = fs.readArray(n);

            int [] h = fs.readArray(n);

            List<Pair> arr = new ArrayList<>();

            for(i=0;i<n;i++){
                int a = k[i]-h[i]+1;

                arr.add(new Pair(a,k[i]));
            }

            Collections.sort(arr);

            ans=0;

            List<Pair> comp = new ArrayList<>();
            int temp = arr.get(0).x;
            int tempy = arr.get(0).y;
            for(i=1;i<n;i++){

                if(arr.get(i).x > tempy){
                    comp.add(new Pair(temp, tempy));
                    temp = arr.get(i).x;
                    tempy = arr.get(i).y;
                }

                else tempy = Math.max(tempy, arr.get(i).y);
            }

            comp.add(new Pair(temp, tempy));


            for(i=0;i<comp.size();i++){

                long a = comp.get(i).y - comp.get(i).x +1 ;

                ans+=(((a)*(a+1))/2);
            }

            out.println(ans);


        }

        out.close();
    }

   /*static long nck(int n , int k){
        long a = fact[n];
        long b = modInv(fact[k]);
        b*= modInv(fact[n-k]);
        b%=mod;

        return (a*b)%mod;
    }

   static void populateFact(){
        fact[0]=1;

        fact[1] = 1;

        for(i=2;i<300005;i++){
            fact[i]=i*fact[i-1];
            fact[i]%=mod;
        }
    }
    */


    static long gcd(long a, long b) {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }

    static long exp(long base, long pow) {
        if (pow == 0) return 1;
        long half = exp(base, pow / 2);
        if (pow % 2 == 0) return mul(half, half);
        return mul(half, mul(half, base));
    }

    static long mul(long a, long b) {
        return ((a % mod) * (b % mod)) % mod;
    }

    static long add(long a, long b) {
        return ((a % mod) + (b % mod)) % mod;
    }

    static long modInv(long x) {
        return exp(x, mod - 2);
    }

    static void ruffleSort(int[] a) {
        //ruffle
        int n = a.length;
        Random r = new Random();
        for (int i = 0; i < a.length; i++) {
            int oi = r.nextInt(n), temp = a[i];
            a[i] = a[oi];
            a[oi] = temp;
        }

        //then sort
        Arrays.sort(a);
    }

    static void ruffleSort(long[] a) {
        //ruffle
        int n = a.length;
        Random r = new Random();
        for (int i = 0; i < a.length; i++) {
            int oi = r.nextInt(n);
            long temp = a[i];
            a[i] = a[oi];
            a[oi] = temp;
        }

        Arrays.sort(a);
    }

    static class FastScanner {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");

        String next() {
            while (!st.hasMoreTokens())
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        int[] readArray(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = nextInt();
            return a;
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }


    static class Pair implements Comparable<Pair> {
        public int x, y;

        Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Pair o) {

            if (x == o.x)
                return Integer.compare(y, o.y);

            return Integer.compare(x, o.x);
        }


    }

}