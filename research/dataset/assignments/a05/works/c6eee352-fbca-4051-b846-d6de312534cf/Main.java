/*
    COLLECTIONS FRAMEWORK TUTORIAL

    * HashMap

    .add(key, value)
    .get(key)
    .containsKey(key) : true/false
    .size()
*/


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.*;

public class Main {

    static final long M = 1000000007;

    // use main for only io
    public static void main(String args[]) {
        FastReader io = new FastReader();
        new Solver().solve(io);
    }
}

//class Pair{
//    int key;String value;
//
//    public Pair(int key, String value){
//        this.key = key;
//        this.value = value;
//    }
//}

class SparseTable{
    int dp[][] = new int[300005][20];

    int log[] = new int[300005];

    public SparseTable(long a[], int n){
        for(int i =0;i<n;i++)dp[i][0] = i;

        for(int i = 1;i < 20;i++){
            for(int j = 0;j + (1 << i) < n;j++){
                if(a[dp[j][i-1]] < a[dp[j + (1 << (i-1))][i-1]]){
                    dp[j][i] = dp[j][i-1];
                }
                else{
                    dp[j][i] = dp[j + (1 << (i - 1))][i-1];
                }
            }
        }

        log[1] = 0;
        for(int i = 2;i <= n;i++){
            log[i] = log[i/2] + 1;
        }
    }

    int getMin(int L, int R, long a[]){
        if(L > R)return 0;
        int j = log[R - L + 1];
        if(a[dp[L][j]] < a[dp[R - (1 << j) + 1][j]])return dp[L][j];

        return dp[R-(1 << j) + 1][j];
    }
}

class Solver {
    static final int M = 998244353;

    void solve(FastReader io) {
       int t = io.nextInt();

       while(t-- > 0){
           int n = io.nextInt();

           String s[] = new String[n];

           for(int i = 0;i < n;i++)s[i] = io.nextLine();

           int ans = 0;

           for(int i = 0;i < 5;i++){
               int count[] = new int[n];

               for(int j = 0;j < n;j++){
                   int freq = 0;
                   for(int k = 0;k < s[j].length();k++){
//                       System.out.println(s[j].charAt(k) - 'a');
                       if((s[j].charAt(k) - 'a') == i){

                           freq++;
                       }
                   }
//                    System.out.println(i + " " + freq);
                   count[j] = 2*freq - s[j].length();
               }


               Arrays.sort(count);
//                for(int it : count)System.out.print(it + " ");
//                System.out.println();

               int curr = 0;
               int j = n - 1;
               for(;j >= 0 && (curr + count[j] > 0);j--){
                   curr += count[j];
               }

               ans = Math.max(ans, n - j - 1);

           }

           System.out.println(ans);
       }
    }

    // returns the first key greater than or equal to val
    int lower_bound(int a[], int val) {
        int low = 0, high = a.length - 1, ret = -1;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (a[mid] < val) low = mid + 1;
            else {
                ret = mid;
                high = mid - 1;
            }

        }

        return ret;
    }

    // returns the first key strictly greater than val
    int upper_bound(int a[], int val) {
        int low = 0, high = a.length - 1, ret = -1;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (a[mid] <= val) low = mid + 1;
            else {
                ret = mid;
                high = mid - 1;
            }

        }

        return ret;
    }
    long modexp(long n, int m) {
        if (m == 0)
            return 1;
        else if (m == 1)
            return n;
        else {
            long p = modexp(n, m / 2);

            if (m % 2 == 1)
                return (((p * p) % M) * n) % M;
            else
                return (p * p) % M;
        }
    }

    long exp(long n, long m) {
        if (m == 0)
            return 1;
        if (m == 1)
            return n;

        long p = exp(n, m / 2);

        if (m % 2 == 1)
            return p * p * n;
        else
            return p * p;
    }

    long gcd(long a, long b) {
        if (a == 0)
            return b;

        return gcd(b % a, a);
    }

    long inv(long n) {
        return modexp(n, M - 2);
    }

    long lcm(long a, long b) {
        return a * b / gcd(a, b);
    }

    long factorial(long fact[], int n) {
        fact[0] = 1;
        fact[1] = 1;

        long prod = 1;

        for (int i = 2; i <= n; i++) {
            prod = (prod * i) % M;

            fact[i] = prod;
        }

        return prod;
    }

    boolean isPrime(long n) {
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0)
                return false;
        }
        return true;
    }
}


class FastReader {
    BufferedReader br;
    StringTokenizer st;

    public FastReader() {
        br = new BufferedReader(new
                InputStreamReader(System.in));
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