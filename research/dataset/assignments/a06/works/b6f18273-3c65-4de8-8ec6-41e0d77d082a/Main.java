import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class Solution {

    //

    public static void main(String[] args) {
        PrintStream out = System.out;
        FastScanner fs = new FastScanner();

        int T = fs.nextInt();


        while (T-- > 0) {

            int n = fs.nextInt();
            long[] arr = new long[n];

            for (int i = 0; i < n; i++) {
                arr[i] = fs.nextLong();
            }
            long[] ps = new long[n];
            ps[0] = arr[0];
            for (int i = 1; i < n; i++) {
                ps[i] = ps[i - 1] + arr[i];
            }

            long min = arr[0] * n + arr[1] * n;

            int even = 0;
            int odd = 1;

            for (int i = 2; i < n; i++) {

                if (i % 2 == 0) {
                    if (arr[i] < arr[even]) {
                        even = i;
                    }
                } else {
                    if (arr[i] < arr[odd]) {
                        odd = i;
                    }
                }

                int tmp = i + 1;
                int oddCount = tmp / 2;
                int evenCount = (tmp + 1) / 2;


                long local = ps[i];
                local += (n - oddCount) * arr[odd];
                local += (n - evenCount) * arr[even];

                min = Math.min(min, local);
            }
            out.println(min);
        }
    }

    // if these strings are both balanced then as one or zero goes out of the window one or zero must come in
    //

    static class FastScanner {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");

        public String next() {
            while (!st.hasMoreElements())
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int[] readArray(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = nextInt();
            return a;
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }


}
