import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class EDU121C {
    public static void main(String[] args) {
        JS scan = new JS();
        int t = scan.nextInt();
        while(t-->0){
            int n = scan.nextInt();
            long[] k = new long[n];
            long[] h = new long[n];
            for(int i =0;i<n;i++){
                k[i] = scan.nextInt();
            }
            for(int i = 0;i<n;i++){
                h[i] = scan.nextInt();
            }
            long ans = 0;
            long lastD = 0;
            long lastHP = 0;
            for(int i = 0;i<n;i++){
                long dist = k[i]-lastD;
                long lo = h[i];
                long hi = lastHP+dist;
                long hpComingOut = 0;
                while(lo<=hi){
                    long mid = (lo+hi)/2;
                    boolean check = false;
                    for(int j = i+1;j<n;j++){
                        if(k[j]-k[i]<h[j]-mid){
                            check = true;
                            break;
                        }
                    }
                    if(check){
                        //we're in trouble
                        lo = mid+1;
                    }else{
                        hi = mid-1;
                        hpComingOut = mid;
                    }
                }
                if(hpComingOut>dist){
                    ans+=summ(dist+lastHP)-summ(lastHP);
                    lastHP+=dist;
                }else{
                    long diff = dist-hpComingOut;
                    ans+=summ(hpComingOut);
                    lastHP = hpComingOut;
                }
                lastD = k[i];
            }
            System.out.println(ans);
        }
    }
    static long summ(long a){
        return (a*(a+1))/2;
    }
    static class JS {
        public int BS = 1 << 16;
        public char NC = (char) 0;
        byte[] buf = new byte[BS];
        int bId = 0, size = 0;
        char c = NC;
        double num = 1;
        BufferedInputStream in;

        public JS() {
            in = new BufferedInputStream(System.in, BS);
        }

        public JS(String s) throws FileNotFoundException {
            in = new BufferedInputStream(new FileInputStream(new File(s)), BS);
        }

        public char nextChar() {
            while (bId == size) {
                try {
                    size = in.read(buf);
                } catch (Exception e) {
                    return NC;
                }
                if (size == -1) return NC;
                bId = 0;
            }
            return (char) buf[bId++];
        }

        public int nextInt() {
            return (int) nextLong();
        }

        public long nextLong() {
            num = 1;
            boolean neg = false;
            if (c == NC) c = nextChar();
            for (; (c < '0' || c > '9'); c = nextChar()) {
                if (c == '-') neg = true;
            }
            long res = 0;
            for (; c >= '0' && c <= '9'; c = nextChar()) {
                res = (res << 3) + (res << 1) + c - '0';
                num *= 10;
            }
            return neg ? -res : res;
        }

        public double nextDouble() {
            double cur = nextLong();
            return c != '.' ? cur : cur + nextLong() / num;
        }

        public String next() {
            StringBuilder res = new StringBuilder();
            while (c <= 32) c = nextChar();
            while (c > 32) {
                res.append(c);
                c = nextChar();
            }
            return res.toString();
        }

        public String nextLine() {
            StringBuilder res = new StringBuilder();
            while (c <= 32) c = nextChar();
            while (c != '\n') {
                res.append(c);
                c = nextChar();
            }
            return res.toString();
        }

        public boolean hasNext() {
            if (c > 32) return true;
            while (true) {
                c = nextChar();
                if (c == NC) return false;
                else if (c > 32) return true;
            }
        }
    }
}
