import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Trial {
    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        PrintWriter pw = new PrintWriter(System.out);

        int q = sc.nextInt();
        while (q-- > 0) {
            int n = sc.nextInt();
            int k = sc.nextInt();
            int[] arr = new int[k];
            int[] t = new int[k];
            HashMap<Integer, Integer> hm = new HashMap<>();
            for (int i = 0; i < k; i++) {
                arr[i] = sc.nextInt() - 1;
            }

            for (int i = 0; i < k; i++) {
                t[i] = sc.nextInt();
                hm.put(arr[i], t[i]);
            }

            int[] left = new int[n];
            int[] right = new int[n];
            left[0] = hm.getOrDefault(0, -1);
            right[n - 1] = hm.getOrDefault(n - 1, -1);
            for (int i = 1; i < n; i++) {
                if (hm.containsKey(i)) {
                    if (left[i - 1] < 0) {
                        left[i] = hm.get(i);
                    } else {
                        left[i] = Math.min(hm.get(i), left[i - 1] + 1);
                    }
                } else {
                    left[i] = left[i - 1] < 0 ? -1 : left[i - 1] + 1;
                }
            }
            for (int i = n - 2; i >= 0; i--) {
                if (hm.containsKey(i)) {
                    if (right[i + 1] < 0) {
                        right[i] = hm.get(i);
                    } else {
                        right[i] = Math.min(hm.get(i), right[i + 1] + 1);
                    }
                } else {
                    right[i] = right[i + 1] < 0 ? -1 : right[i + 1] + 1;
                }
            }
            for (int i = 0; i < n; i++) {
                if (left[i] < 0) {
                    pw.print(right[i] + " ");
                } else if (right[i] < 0) {
                    pw.print(left[i] + " ");
                } else {
                    pw.print(Math.min(left[i], right[i]) + " ");
                }
            }
            pw.println();
        }
        pw.flush();
        pw.close();
    }

    // inclusive
    private static void rotate(int[] arr, int l, int r) {
        int temp = arr[r];
        for (int i = r - 1; i >= l; i--) {
            arr[i + 1] = arr[i];
        }
        arr[l] = temp;
    }

    static class Scanner {
        StringTokenizer st;
        BufferedReader br;

        public Scanner(InputStream System) throws FileNotFoundException {
            br = new BufferedReader(new InputStreamReader(System));
        }

        public String next() throws IOException {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(br.readLine());
            return st.nextToken();
        }

        public String nextLine() throws IOException {
            return br.readLine();
        }

        public int nextInt() throws IOException {
            return Integer.parseInt(next());
        }

        public double nextDouble() throws IOException {
            return Double.parseDouble(next());
        }

        public char nextChar() throws IOException {
            return next().charAt(0);
        }

        public Long nextLong() throws IOException {
            return Long.parseLong(next());
        }

        public boolean ready() throws IOException {
            return br.ready();
        }

        public void waitForInput() {
            for (long i = 0; i < 3e9; i++)
                ;
        }
    }

    static class Pair {
        int a;
        int b;
        boolean asc;

        Pair(int a, int b, boolean asc) {
            this.a = a;
            this.b = b;
            this.asc = asc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return a == pair.a &&
                    b == pair.b &&
                    asc == pair.asc;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, asc);
        }
    }
}
