import java.util.*;
import java.lang.*;
import java.io.*;


public class Main {
    void solve() {
        int n = in.nextInt();
        char[] a = in.nextLine().toCharArray();
        char[] b = in.nextLine().toCharArray();

        int ans = MAX;
        ans = Math.min(ans, operations(a, b));
        ans = Math.min(ans, change(a, b, '1'));
        ans = Math.min(ans, change(a, b, '0'));

        if (ans == MAX)ans = -1;
        out.append(ans + "\n");
    }

    int operations(char[] a, char[] b) {
        int count01 = 0 , count10 = 0;
        int n = a.length;
        for (int i = 0 ; i < n; i++) {
            if (a[i] != b[i]) {
                if (a[i] == '0')count01++;
                else count10++;
            }
        }
        if (count01 != count10)return MAX;
        return count01 + count10;
    }

    int change(char[] a, char[] b , char ch) {
        int n = a.length;
        char[] c = new char[n];
        for (int i = 0 ; i < n; i++)c[i] = a[i];
        int index = -1;
        for (int i = 0; i < n; i++) {
            if (c[i] == '1' && b[i] == ch) {
                index = i;
                break;
            }
        }
        if (index == -1)return MAX;
        for (int i = 0 ; i < n; i++) {
            if (i == index)continue;
            c[i] = (char)( '0' + ('1' - c[i]) );
        }
        int  local = operations(c, b);
        if (local == MAX)return MAX;
        return 1 + local;
    }

    public static void main (String[] args) {
        // It happens - Syed Mizbahuddin
        Main sol = new Main();
        int t = 1;
        t = in.nextInt();
        while (t-- != 0) {
            sol.solve();
        }
        System.out.print(out);
    }

    <T> void println(T[] s) {
        if (err == null)return;
        err.println(Arrays.toString(s));
    }

    <T> void println(T s) {
        if (err == null)return;
        err.println(s);
    }

    void println(int s) {
        if (err == null)return;
        err.println(s);
    }

    void println(int[] a) {
        if (err == null)return;
        println(Arrays.toString(a));
    }

    int[] array(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.nextInt();
        }
        return a;
    }

    int[] array1(int n) {
        int[] a = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            a[i] = in.nextInt();
        }
        return a;
    }

    int max(int[] a) {
        int max = a[0];
        for (int i = 0; i < a.length; i++)max = Math.max(max, a[i]);
        return max;
    }

    int min(int[] a) {
        int min = a[0];
        for (int i = 0; i < a.length; i++)min = Math.min(min, a[i]);
        return min;
    }

    int count(int[] a, int x) {
        int count = 0;
        for (int i = 0; i < a.length; i++)if (x == a[i])count++;
        return count;
    }

    void printArray(int[] a) {
        for (int ele : a)out.append(ele + " ");
        out.append("\n");
    }

    static {
        try {
            System.setIn(new FileInputStream("input.txt"));
            System.setOut(new PrintStream(new FileOutputStream("output.txt")));
            err = new PrintStream(new FileOutputStream("error.txt"));
        } catch (Exception e) {}
    }

    static FastReader in;
    static StringBuilder out;
    static PrintStream err;
    final int MAX;
    final int MIN;
    int mod ;
    Main() {
        in = new FastReader();
        out = new StringBuilder();
        MAX = Integer.MAX_VALUE;
        MIN = Integer.MIN_VALUE;
        mod = (int)1e9 + 7;
    }
    class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(
                new InputStreamReader(System.in));
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

        int nextInt() { return Integer.parseInt(next()); }

        long nextLong() { return Long.parseLong(next()); }

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

    class Pair implements Comparable<Pair> {
        int first , second;
        Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int compareTo(Pair b) {
            return this.first - b.first;
        }

        public String toString() {
            String s = "{ " + Integer.toString(first) + " , " + Integer.toString(second) + " }";
            return s;
        }
    }
}
