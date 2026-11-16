import java.io.*;
import java.util.*;

public class AirConditioners {
    public static long[] solve(int n, int k, HashMap<Integer, Integer> store) {
        long[] result = new long[n];
        Arrays.fill(result, Integer.MAX_VALUE);
        long current = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            current++;
            if (store.containsKey(i)) {
                current = Math.min(current, store.get(i));
            }

            result[i] = Math.min(result[i], current);
        }
        current = Integer.MAX_VALUE;
        for (int i = n - 1; i >= 0; i--) {
            current++;
            if (store.containsKey(i)) {
                current = Math.min(current, store.get(i));
            }
            result[i] = Math.min(result[i], current);
        }
        return result;
    }

    public static void main(String[] args) {
        FastScanner fs = new FastScanner();
        PrintWriter out = new PrintWriter(System.out);
        int test = fs.nextInt();
        for (int t = 0; t < test; t++) {
            int n = fs.nextInt();
            int k = fs.nextInt();
            int[] array = fs.readArray(k);
            int[] temperature = fs.readArray(k);
            HashMap<Integer, Integer> store = new HashMap<>();
            for (int i = 0; i < k; i++) {
                store.put(array[i] - 1, temperature[i]);
            }
            long[] ans = solve(n, k, store);
            for (int i = 0; i < n; i++) {
                out.print(ans[i] + " ");
            }
            out.println();
        }
        out.println();
        out.close();
    }

    static void sort(int[] a) {
        ArrayList<Integer> l = new ArrayList<>();
        for (int i : a)
            l.add(i);
        Collections.sort(l);
        for (int i = 0; i < a.length; i++)
            a[i] = l.get(i);
    }

    static class FastScanner {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");

        String next() {
            while (!st.hasMoreTokens())
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        int[] readArray(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++)
                a[i] = nextInt();
            return a;
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }

}