


import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.io.InputStreamReader;
import static java.lang.Math.*;
import static java.lang.System.*;

public class Main1 {







    public static void main(String[] args) throws IOException {

//        try {
//             Scanner in = new Scanner(System.in) ;
        FastScanner in = new FastScanner();
        PrintWriter out = new PrintWriter(System.out);



            int t = in.nextInt() ;
            while (t-- > 0){
                int n = in.nextInt() ;
                int dp[][] = new int[n][5] ;
                String tt[] = new String[n] ;

                for (int i = 0; i <n ; i++) {
                    String  s= in.next() ;
                    tt[i] = s ;
                    for (int j = 0; j <s.length() ; j++) {
                        dp[i][s.charAt(j)-'a']++ ;
                    }
                }
                int max = 0 ;


                for (int i = 0; i <5 ; i++) {
                    ArrayList<Integer>list = new ArrayList<>() ;
                    for (int j = 0; j <n ; j++) {
                        list.add(dp[j][i] - (tt[j].length()-dp[j][i]) );
                    }
                    list.sort(Collections.reverseOrder());
                    int ans = 0 ;
                    int sum = 0 ;
                    for (int curr : list){
                        sum+= curr ;
                        if (sum > 0){
                            ans++ ;
                            max = max(max , ans) ;
                        }
                        else break;
                    }

                }

                System.out.println(max);
            }










            out.flush();
            out.close();


//        } catch (Exception e) {
//            return;
//        }

}

    static long gcd(long a, long b) {
        return (b == 0) ? a : gcd(b, a % b);
    }

    static int gcd(int a, int b) {
        return (b == 0) ? a : gcd(b, a % b);
    }

    static void sort(int ar[]) {
        int n = ar.length;
        ArrayList<Integer> a = new ArrayList<>();
        for (int i = 0; i < n; i++)
            a.add(ar[i]);
        Collections.sort(a);
        for (int i = 0; i < n; i++)
            ar[i] = a.get(i);
    }

    static void sort1(long ar[]) {
        int n = ar.length;
        ArrayList<Long> a = new ArrayList<>();
        for (int i = 0; i < n; i++)
            a.add(ar[i]);
        Collections.sort(a);
        for (int i = 0; i < n; i++)
            ar[i] = a.get(i);
    }

    static long ncr(long n, long r, long mod) {
        if (r == 0)
            return 1;
        long val = ncr(n - 1, r - 1, mod);
        val = (n * val) % mod;
        val = (val * modInverse(r, mod)) % mod;
        return val;
    }

    static long fast_pow(long base, long n, long M) {
        if (n == 0)
            return 1;
        if (n == 1)
            return base % M;
        long halfn = fast_pow(base, n / 2, M);
        if (n % 2 == 0)
            return (halfn * halfn) % M;
        else
            return (((halfn * halfn) % M) * base) % M;
    }

    static long modInverse(long n, long M) {
        return fast_pow(n, M - 2, M);
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


}







