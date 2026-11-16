
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Armchairs {
    static  ArrayList<Integer> f;
    static ArrayList<Integer> u;
    static int dp[][];
    static int fun(int i, int j){
        if(i == f.size()) return 0;
        if(j == u.size()) return 99999999;
        if(dp[i][j] != -1) return dp[i][j];
        int ans1 = fun(i, j+1);
        int ans2 = fun(i+1, j+1) + Math.abs(f.get(i)-u.get(j));
        return dp[i][j] = Math.min(ans1, ans2);
    }
    private static int solve(int n, int a[]) {
        for (int i = 0; i < n; i++) {
            if (a[i]==0)
                u.add(i);
            else
                f.add(i);
        }

        return fun(0,0);
    }

    public static void main(String[] args)
            throws IOException {
        Scanner s = new Scanner();
        int t = 1;
        StringBuilder ans = new StringBuilder();
        int count = 0;
        while (t-- > 0) {
            int n = s.nextInt();
            int a[] = new int[n];
            dp=new int[n][n];
            for (int i = 0; i < n; i++) {
                a[i]=s.nextInt();
            }
            f=new ArrayList<>();
            u=new ArrayList<>();
            for( int i=0; i<n; i++) Arrays.fill(dp[i],-1);

            ans.append(solve(n, a)).append("\n");
        }
        System.out.println(ans.toString());
    }

    static class Scanner {
        final private int BUFFER_SIZE = 1 << 16;
        private DataInputStream din;
        private byte[] buffer;
        private int bufferPointer, bytesRead;

        public Scanner() {
            din = new DataInputStream(System.in);
            buffer = new byte[BUFFER_SIZE];
            bufferPointer = bytesRead = 0;
        }

        public Scanner(String file_name) throws IOException {
            din = new DataInputStream(
                    new FileInputStream(file_name));
            buffer = new byte[BUFFER_SIZE];
            bufferPointer = bytesRead = 0;
        }

        public String readLine() throws IOException {
            byte[] buf = new byte[64]; // line length
            int cnt = 0, c;
            while ((c = read()) != -1) {
                if (c == '\n') {
                    if (cnt != 0) {
                        break;
                    } else {
                        continue;
                    }
                }
                buf[cnt++] = (byte) c;
            }
            return new String(buf, 0, cnt);
        }

        public int nextInt() throws IOException {
            int ret = 0;
            byte c = read();
            while (c <= ' ') {
                c = read();
            }
            boolean neg = (c == '-');
            if (neg)
                c = read();
            do {
                ret = ret * 10 + c - '0';
            } while ((c = read()) >= '0' && c <= '9');

            if (neg)
                return -ret;
            return ret;
        }

        public long nextLong() throws IOException {
            long ret = 0;
            byte c = read();
            while (c <= ' ')
                c = read();
            boolean neg = (c == '-');
            if (neg)
                c = read();
            do {
                ret = ret * 10 + c - '0';
            } while ((c = read()) >= '0' && c <= '9');
            if (neg)
                return -ret;
            return ret;
        }

        public double nextDouble() throws IOException {
            double ret = 0, div = 1;
            byte c = read();
            while (c <= ' ')
                c = read();
            boolean neg = (c == '-');
            if (neg)
                c = read();

            do {
                ret = ret * 10 + c - '0';
            } while ((c = read()) >= '0' && c <= '9');

            if (c == '.') {
                while ((c = read()) >= '0' && c <= '9') {
                    ret += (c - '0') / (div *= 10);
                }
            }

            if (neg)
                return -ret;
            return ret;
        }

        private void fillBuffer() throws IOException {
            bytesRead = din.read(buffer, bufferPointer = 0,
                    BUFFER_SIZE);
            if (bytesRead == -1)
                buffer[0] = -1;
        }

        private byte read() throws IOException {
            if (bufferPointer == bytesRead)
                fillBuffer();
            return buffer[bufferPointer++];
        }

        public void close() throws IOException {
            if (din == null)
                return;
            din.close();
        }
    }

    public static long norm(long a, long MOD) {
        return ((a % MOD) + MOD) % MOD;
    }

    public static long msub(long a, long b, long MOD) {
        return norm(norm(a, MOD) - norm(b, MOD), MOD);

    }

    public static long madd(long a, long b, long MOD) {
        return norm(norm(a, MOD) + norm(b, MOD), MOD);

    }

    public static long mMul(long a, long b, long MOD) {
        return norm(norm(a, MOD) * norm(b, MOD), MOD);


    }

    public static long mDiv(long a, long b, long MOD) {
        return norm(norm(a, MOD) / norm(b, MOD), MOD);
    }
}
