// package com.company;
import java.util.*;
import java.io.*;
import java.lang.*;
public class Main{

    public static void main(String args[]){
        InputReader in=new InputReader(System.in);
        TASK solver = new TASK();
        int t=1;
//        t = in.nextInt();
        for(int i=1;i<=t;i++)
        {
            solver.solve(in,i);
        }
    }
    static class TASK {
        static int mod = 1000000007;
        static long solve(int a[],ArrayList<Integer> al,int i,int j,long dp[][])
        {
            if(j<0 || i<0)
                return Integer.MAX_VALUE;
            if(i==0 && j==0)
                return 0;
            if(j==0)
                return 0;
            if(i==0)
                return Integer.MAX_VALUE;
            if(dp[i-1][j-1]==-1)
            {
                dp[i-1][j-1]=solve(a,al,i-1,j-1,dp);
            }
            if(dp[i-1][j]==-1)
                dp[i-1][j]=solve(a,al,i-1,j,dp);
            if(a[i-1]==1)
                return dp[i-1][j];
            dp[i][j]=Math.min(dp[i-1][j],dp[i-1][j-1]+Math.abs(i-al.get(j-1)));
            return dp[i][j];
        }
        void solve(InputReader in, int testNumber) {
            int n = in.nextInt();
            int a[] = new int[n+1];
            long dp[][] = new long[n+1][n+1];
            ArrayList<Integer> al = new ArrayList<>();
            for(int i=0;i<n;i++) {
                a[i] = in.nextInt();
                if(a[i]==1)
                    al.add(i+1);
                Arrays.fill(dp[i],-1);
            }
            Arrays.fill(dp[n],-1);
            System.out.println(solve(a,al,n,al.size(),dp));










        }
    }

    static class pair{
        int x ;
        int y;
        pair(int x,int y)
        {
            this.x=x;
            this.y=y;
        }
    }

    static class Maths {
        static long gcd(long a, long b) {
            if (a == 0)
                return b;
            return gcd(b % a, a);
        }
        public static long lcm(long a, long b) {
            return (a * b) / gcd(a, b);
        }
        public static long factorial(int n) {
            long fact = 1;
            for (int i = 1; i <= n; i++) {
                fact *= i;
            }
            return fact;
        }
    }
    static class InputReader {
        private InputStream stream;
        private byte[] buf = new byte[1024];
        private int curChar;
        private int numChars;
        private SpaceCharFilter filter;
        public InputReader(InputStream stream) {
            this.stream = stream;
        }
        public int read() {
            if (numChars == -1)
                throw new InputMismatchException();
            if (curChar >= numChars) {
                curChar = 0;
                try {
                    numChars = stream.read(buf);
                } catch (IOException e) {
                    throw new InputMismatchException();
                }
                if (numChars <= 0)
                    return -1;
            }
            return buf[curChar++];
        }
        public String nextLine() {
            BufferedReader br = new BufferedReader(new
                    InputStreamReader(System.in));
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
        public int nextInt() {
            int c = read();
            while (isSpaceChar(c))
                c = read();
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = read();
            }
            int res = 0;
            do {
                if (c < '0' || c > '9')
                    throw new InputMismatchException();
                res *= 10;
                res += c - '0';
                c = read();
            }
            while (!isSpaceChar(c));
            return res * sgn;
        }
        public long nextLong() {
            int c = read();
            while (isSpaceChar(c))
                c = read();
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = read();
            }
            long res = 0;
            do {
                if (c < '0' || c > '9')
                    throw new InputMismatchException();
                res *= 10;
                res += c - '0';
                c = read();
            }
            while (!isSpaceChar(c));
            return res * sgn;
        }
        public double nextDouble() {
            int c = read();
            while (isSpaceChar(c))
                c = read();
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = read();
            }
            double res = 0;
            while (!isSpaceChar(c) && c != '.') {
                if (c == 'e' || c == 'E')
                    return res * Math.pow(10, nextInt());
                if (c < '0' || c > '9')
                    throw new InputMismatchException();
                res *= 10;
                res += c - '0';
                c = read();
            }
            if (c == '.') {
                c = read();
                double m = 1;
                while (!isSpaceChar(c)) {
                    if (c == 'e' || c == 'E')
                        return res * Math.pow(10, nextInt());
                    if (c < '0' || c > '9')
                        throw new InputMismatchException();
                    m /= 10;
                    res += (c - '0') * m;
                    c = read();
                }
            }
            return res * sgn;
        }
        public String readString() {
            int c = read();
            while (isSpaceChar(c))
                c = read();
            StringBuilder res = new StringBuilder();
            do {
                res.appendCodePoint(c);
                c = read();
            }
            while (!isSpaceChar(c));
            return res.toString();
        }
        public boolean isSpaceChar(int c) {
            if (filter != null)
                return filter.isSpaceChar(c);
            return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c
                    == -1;
        }
        public String next() {
            return readString();
        }
        public interface SpaceCharFilter {
            public boolean isSpaceChar(int ch);
        }
    }
}
