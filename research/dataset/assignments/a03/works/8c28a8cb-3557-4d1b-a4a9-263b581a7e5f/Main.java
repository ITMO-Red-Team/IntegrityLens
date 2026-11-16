// package com.company;
import java.util.*;
import java.io.*;
import java.lang.*;
public class Main{

    public static void main(String args[]){
        InputReader in=new InputReader(System.in);
        TASK solver = new TASK();
        int t=1;
        t = in.nextInt();
        for(int i=1;i<=t;i++)
        {
            solver.solve(in,i);
        }
    }
    static class TASK {
        static int dp[] = new int[31];
        static {
            dp[0]=1;
            for(int i=1;i<31;i++)
            {
                dp[i]=dp[i-1]*2;
            }
        }
        void solve(InputReader in, int testNumber) {
            int n = in.nextInt();
            int k = in.nextInt();
            int a[] = new int[k];
            int l[] = new int[n+1];
            long pre[] = new long[n+2];
            long suff[] = new long[n+2];
            Arrays.fill(pre,Long.MAX_VALUE/2);
            Arrays.fill(suff,Long.MAX_VALUE/2);
            for(int i=0;i<k;i++)
            {
                a[i]=in.nextInt();
            }
            for(int i=0;i<k;i++)
            {
                int x = in.nextInt();
                l[a[i]]=x;
            }
            for(int i=1;i<=n;i++)
            {
                pre[i]=pre[i-1];
                if(l[i]!=0)
                {
                    pre[i]=Math.min(pre[i],l[i]-i);
                }
            }
            for(int i=n;i>=1;i--)
            {
                suff[i]=suff[i+1];
                if(l[i]!=0)
                {
                    suff[i]=Math.min(suff[i],l[i]+i);
                }
            }
            for(int i=1;i<=n;i++)
            {
                System.out.print(Math.min(pre[i]+i,suff[i]-i)+" ");
            }
            System.out.println();







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


