// package com.company;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.jar.JarEntry;

public class Main{
    /* || श्री गणेशाय नमः ||
       @𝐚𝐮𝐭𝐡𝐨𝐫 𝐉𝐢𝐠𝐚𝐫 𝐍𝐚𝐢𝐧𝐮𝐣𝐢
       𝐒𝐕𝐍𝐈𝐓- 𝐒𝐮𝐫𝐚𝐭
    */
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


        void solve(InputReader in, int testNumber) {
           int n = in.nextInt();
           int a[][] = new int[n][5];
           for(int i=0;i<n;i++)
           {
               char[] s = in.next().toCharArray();
               for(int j=0;j<s.length;j++)
               {
                   a[i][s[j]-'a']++;
               }
           }
           int max=0;
           int x[] = new int[n];
           for(int j=0;j<=4;j++)
           {
               for(int i=0;i<n;i++)
               {
                   x[i]=2*a[i][j];
                   for(int k=0;k<5;k++)
                   {
                       x[i]-=a[i][k];
                   }
               }
               Arrays.sort(x);
               int c=0,sum=0;
               for(int i=n-1;i>=0;i--)
               {
                   sum+=x[i];
                   if(sum<=0)
                       break;
                   c++;
               }
               max=Math.max(max,c);
           }
            System.out.println(max);
















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
            if (a < b)
                return gcd(b,a);
            if (b == 0)
                return a;
            else
                return gcd(b,a%b);
        }
        public static long lcm(long a, long b) {
            return (a * b) / gcd(a, b);
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


