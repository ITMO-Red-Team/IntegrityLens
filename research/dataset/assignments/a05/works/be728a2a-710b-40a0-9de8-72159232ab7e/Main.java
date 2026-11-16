import java.io.*;
import java.util.*;

public class Codeforce {

    static class Reader {
        final private int BUFFER_SIZE = 1 << 16;
        private DataInputStream din;
        private byte[] buffer;
        private int bufferPointer, bytesRead;

        public Reader() {
            din = new DataInputStream(System.in);
            buffer = new byte[BUFFER_SIZE];
            bufferPointer = bytesRead = 0;
        }

        public Reader(String file_name) throws IOException {
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

    static int mod = (int) (1e9 + 7);

    public static class pair implements Comparator<pair> {
        int x;
        int y;

        public pair() {

        }

        public pair(int x, int y) {
            this.x = x;
            this.y = y;

        }

        @Override
        public int compare(pair o1, pair o2) {
            return o1.y - o2.y;
        }
    }

    public static long modularpow(long a, long b) {
        long res = 1;
        if (b == 0)
            return res;
        else {
            while (b > 0) {
                if (b % 2 == 1) {
                    res *= a;
                    res %= mod;

                }
                a = a * a;
                a %= mod;
                b /= 2;
            }
            return res % mod;
        }
    }

    public static int gcd(int a, int b) {
        if (b == 0)
            return a;
        else
            return gcd(b, a % b);
    }

    public static String binary(int a) {
        String s1 = "";
        while (a > 0) {
            s1 = a % 2 + s1;
            a /= 2;
        }
        return s1;
    }

    public static int Lower_Bound(long a[], int l, int r, long k) {


        while (r - l > 1) {
            int mid = l + (r - l) / 2;
            if (a[mid] <= k) l = mid;
            else
                r = mid;

        }
        return l;
    }

    public static int Upper_Bound(int a[], int l, int r, int k) {

        while (r - l > 1) {
            int mid = (l + r) / 2;
            if (a[mid] <= k)
                l = mid;
            else
                r = mid;
        }
        return l + 1;
    }
public static  int fun(int ch,int a[][],int tot[],int n){
        ArrayList<Integer>ar=new ArrayList<>();
        for(int i=0;i<n;i++){
            ar.add((a[i][ch]-(tot[i]-a[i][ch])));
        }
        Collections.sort(ar,Collections.reverseOrder());
        int cou=0;
        int ans=0;
        for(int i:ar){
            cou+=i;
            if(cou>0){
                ans++;
            }
            else
                break;
        }
return ans;
}
    public static void main(String arg[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine());
        StringBuilder sb = new StringBuilder();
        while (t-- > 0) {
               int n=Integer.parseInt(br.readLine());
               int a[][]=new int[n][5];
               int tot[]=new int[n];
               for(int i=0;i<n;i++){
                   String s=br.readLine();
                   tot[i]+=s.length();
                   for(int j=0;j<s.length();j++){
                       a[i][s.charAt(j)-'a']++;
                   }
               }
               int ans=0;
               for(int i=0;i<5;i++){
                   ans=Math.max(ans,fun(i,a,tot,n));
               }
               sb.append(ans+"\n");
                       }
        System.out.println(sb);
    }
}