import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

public class MonstersAndSpells {

    static class FastReader {

        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
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

    public static void main(String[] args) throws IOException {
        FastReader input = new FastReader();
        BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
        int t = input.nextInt();
        while (t-- > 0) {
            int n = input.nextInt();
            long k[] = new long[n];
            long h[] = new long[n];
            long ans = 0;
            for (int i = 0; i < n; i++) {
                k[i] = input.nextInt();
            }
            for (int i = 0; i < n; i++) {
                h[i] = input.nextInt();
            }
            for (int i = n-1; i >0; i--) {
                long d = k[i]-k[i-1];
                if(h[i-1]+d<h[i]){
                    h[i-1]+=(h[i]-(h[i-1]+d));
                }
            }
            ans += (h[0]) * (h[0] + 1) / 2;
            long last = h[0];
            for (int i = 1; i < n; i++) {
                if (k[i] - k[i - 1] >= h[i]) {
                    ans += (h[i]) * (h[i] + 1) / 2;
                    last = h[i];
                } else {
                    long x = (k[i] - k[i - 1]);
                    long y = last + x;
                    long sum1 = (last) * (last + 1) / 2;
                    long sum2 = (y) * (y + 1) / 2;
                    ans += (sum2 - sum1);
                    last = y;
                }
            }
            log.write(ans + "\n");
        }
        log.flush();
    }

}
