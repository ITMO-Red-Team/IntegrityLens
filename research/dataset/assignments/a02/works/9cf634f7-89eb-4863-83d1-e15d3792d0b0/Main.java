import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CF1 {
    public static void main(String[] args) {
        FastScanner sc=new FastScanner();
        int T=sc.nextInt();
//        int T=1;
        for (int tt=0; tt<T; tt++){
           int n =sc.nextInt();
           String a = sc.next();
           String b= sc.next();
           int candles1=0;
           int candles2=0;
           int inPlace=0;
           for (int i=0; i<n; i++){
               if (a.charAt(i)=='1'){
                   candles1++;
               }
               if (b.charAt(i)=='1') candles2++;
               if (b.charAt(i)=='1' && a.charAt(i)=='1') inPlace++;
           }
           if (candles1==candles2 || n+1-candles2==candles1){
             int ans=Math.min(help(candles1-inPlace, inPlace,n+1,candles2,0,0), help(candles1-inPlace, inPlace,n+1,candles2,0,1));
             if (ans==Integer.MAX_VALUE) System.out.println(-1);
             else System.out.println(ans);
           }
           else System.out.println(-1);
        }
    }
    static int help(int i, int j, int max, int cands, int moves, int flip){
        if (j==cands && i==0) return moves;
        else if (j==0 && flip==0 || flip==1 && i==0 ) return Integer.MAX_VALUE;

            if (flip==0){
                int x=0;
                int y=cands-j+1;
                if (i+j==max-cands){
                    x=cands-y;
                }
                else x=max-cands-y;
                return help(x,y,max,cands,moves+1,1);
            }
            else {
                int x=0;
                int y=cands-j;
                if (i+j==max-cands){
                    x=cands-y;
                }
                else x=max-cands-y;
                return help(x,y,max,cands,moves+1,0);
            }
        
    }
    static long mod =998244353L;
    static long power (long a, long b){
        long res=1;
        while (b>0){
            if ((b&1)== 1){
                res= (res * a % mod)%mod;
            }
            a=(a%mod * a%mod)%mod;
            b=b>>1;
        }
        return res;
    }

    boolean[] sieveOfEratosthenes(int n)
    {

        boolean prime[] = new boolean[n+1];
        for(int i=0;i<=n;i++)
            prime[i] = true;

        for(int p = 2; p*p <=n; p++)
        {
            if(prime[p] == true)
            {
                for(int i = p*p; i <= n; i += p)
                    prime[i] = false;
            }
        }
        return prime;
    }
    static void sort(int[] a) {
        ArrayList<Integer> l=new ArrayList<>();
        for (int i:a) l.add(i);
        Collections.sort(l);
        for (int i=0; i<a.length; i++) a[i]=l.get(i);
    }
    static void sortLong(long[] a) {
        ArrayList<Long> l=new ArrayList<>();
        for (long i:a) l.add(i);
        Collections.sort(l);
        for (int i=0; i<a.length; i++) a[i]=l.get(i);
    }
    static long gcd (long n, long m){
        if (m==0) return n;
        else return gcd(m, n%m);
    }

    static class Pair implements Comparable<Pair>{
        int x,y;
        public Pair(int x, int y){
            this.x = x;
            this.y = y;
        }
        public int compareTo(Pair o){
            return -this.x+o.x;
        }
    }
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
}