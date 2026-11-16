import java.util.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner();
        PrintWriter out = new PrintWriter(System.out);
        int t = sc.nextInt();
        while(t-- > 0) {
            int n = sc.nextInt();
            int[] k = new int[n];
            int[] h = new int[n];
            for (int i = 0; i < n; ++i) k[i] = sc.nextInt();
            for (int i = 0; i < n; ++i) h[i] = sc.nextInt();
            Stack<Pair> stk = new Stack<>();
            stk.push(new Pair(0, 0));

            for (int i = 0; i < n; ++i) {
                int cur_pos = k[i];
                int cur_inc = h[i];
                while (!stk.isEmpty() && !(cur_pos - cur_inc + 1 > stk.peek().pos)) {
                    cur_inc = Math.max(cur_inc, stk.peek().inc + cur_pos - stk.peek().pos);
                    stk.pop();
                }
                stk.add(new Pair(cur_pos, cur_inc));
            }
            long answer = 0;
            while (!stk.isEmpty()) {
                answer += (1L * stk.peek().inc * (stk.peek().inc + 1) / 2);
                stk.pop();
            }
            out.println(answer);
        }
        out.flush();
    }

    static class Pair {
        int pos, inc;
        public Pair (int pos, int inc) {
            this.pos = pos;
            this.inc = inc;
        }
    }


    static class Scanner {
        BufferedReader in;
        StringTokenizer st;

        public Scanner() {
            this.in = new BufferedReader(new InputStreamReader(System.in));
        }

        public Scanner(FileReader f) {
            this.in = new BufferedReader(f);
        }

        public String nextToken() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(in.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(nextToken());
        }

        public long nextLong() {
            return Long.parseLong(nextToken());
        }

        public double nextDouble() {
            return Double.parseDouble(nextToken());
        }

        public void close() throws IOException {
            in.close();
        }
    }
}
