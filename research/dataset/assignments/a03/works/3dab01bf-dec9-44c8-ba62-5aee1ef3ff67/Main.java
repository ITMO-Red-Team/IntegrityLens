import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Test{

        static FastReader scan;

        static void solve(){
            scan.nextLine();
            int n=scan.nextInt();
            int k=scan.nextInt();
            int []arr=new int[n+1];

            int []block=new int[k];
            int []temp=new int[k];

            for(int i=0;i<k;i++){
                block[i]=scan.nextInt();
            }

            for(int i=0;i<k;i++){
                temp[i]=scan.nextInt();
            }

            for(int i=0;i<k;i++){
                arr[block[i]]=temp[i];
            }
            int []dp=new  int[n+1];
            int prev=Integer.MAX_VALUE-1000;
            for(int i=1;i<=n;i++){
                if(arr[i]==0){
                    if(prev==Integer.MAX_VALUE-1000){
                        dp[i]=prev;
                    }
                    else{
                        prev=prev+1;
                        dp[i]=prev;
                    }
                }
                else{
                    prev=Math.min(prev+1,arr[i]);
                    dp[i]=prev;
                }
            }

            prev=Integer.MAX_VALUE-1000;
            for(int i=n;i>=1;i--){
                if(arr[i]==0){
                    if(prev==Integer.MAX_VALUE-1000){
                        dp[i]=Math.min(prev,dp[i]);
                    }
                    else{
                        prev=prev+1;
                        dp[i]=Math.min(prev,dp[i]);
                    }
                }
                else{
                    prev=Math.min(prev+1,arr[i]);
                    dp[i]=Math.min(prev,dp[i]);
                }
            }

            for(int i=1;i<=n;i++){
                System.out.print(dp[i]+" ");
            }
            System.out.println();

        }

        public static void main (String[] args) throws java.lang.Exception{
            scan=new FastReader();
            int t=scan.nextInt();
            while(t-->0){
                solve();                  
            }
        }

        static class FastReader {
            BufferedReader br;
            StringTokenizer st;

            public FastReader()
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

            double nextDouble() { return Double.parseDouble(next()); }

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

        static class Pair implements Comparable<Pair>{
            long wt;
            long idx;
            Pair(long x,long y){
                this.wt=x;
                this.idx=y;
            }
            @Override
            public int compareTo(Pair x){
                return (int)(this.wt-x.wt);
            }

            public String toString(){
                return "( "+wt+" "+idx+" )";
            }
        }        

        static void printLong(long []arr){
            for(long x:arr)System.out.print(x+" ");
        }

        static void printInt(int []arr){
            for(int x:arr)System.out.print(x+" ");
        }

        static void scanInt(int []arr){
            for(int i=0;i<arr.length;i++){
                arr[i]=scan.nextInt();
            }
        }

        static void scanLong(long []arr){
            for(int i=0;i<arr.length;i++){
                arr[i]=scan.nextLong();
            }
        }

        static long gcd(long a, long b){
            if (b == 0)
                return a;
            
            return gcd(b, a % b);
        }   

        static long power(long x, long y, long mod){
            long res = 1; 
        
            x = x % mod; 
         
            if (x == 0)
              return 0; 
         
            while (y > 0){              
                if ((y & 1) != 0)
                res = (res * x) % mod;
              
                y = y >> 1; 
                x = (x * x) % mod;
            }
                return res;
        }

        static long add(long a,long b,long mod){
            a = a % mod; 
            b = b % mod; 
            return (((a + b) % mod) + mod) % mod;
        }

        static long sub(long a, long b,long mod){
            a = a % mod; 
            b = b % mod; 
            return (((a - b) % mod) + mod) % mod;
        }

        static long mul(long a, long b,long mod){
            a = a % mod; 
            b = b % mod; 
            return (((a * b) % mod) + mod) % mod;
        }
        

        static long mminvprime(long a, long b,long mod) {
            return power(a, b - 2,mod);
        }
}
