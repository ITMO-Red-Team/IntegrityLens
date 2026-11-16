import java.util.Scanner;

public class MinimumGridPath {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int test = scanner.nextInt();

        StringBuilder sb = new StringBuilder();
        for (int t = 0; t < test; t++) {
            int n = scanner.nextInt();
            long[] arr = new long[n];
            for (int i = 0; i < n; i++) {
                arr[i] = scanner.nextLong();
            }

            long minx = arr[0];
            long miny = arr[1];

            long min = minx * n + miny * n;
            long sumx = arr[0];
            long sumy = arr[1];

            for(int i=2; i<n; i++) {
                int xc;
                int yc;
                if(i%2 == 0) {
                    xc = i/2 + 1;
                    yc = i/2;
                    sumx += arr[i];
                    minx = Math.min(minx, arr[i]);
                } else {
                    xc = i/2 + 1;
                    yc = i/2 + 1;
                    sumy += arr[i];
                    miny = Math.min(miny, arr[i]);
                }
                min = Math.min(min, sumx + (n-xc) * minx + sumy + (n-yc) * miny);
            }


            sb.append(min).append(System.lineSeparator());
        }
        System.out.println(sb);

    }
}

/*
// yehara
import java.io.PrintWriter;
import java.util.*;

public class C {

    static Scanner sc;
    static PrintWriter out;

    public static void main(String[] args) throws Exception {
        sc = new Scanner(System.in);
        out = new PrintWriter(System.out);
        int t = sc.nextInt();
        for(int i=0; i<t; i++) {
            solve();
        }
        out.flush();
    }

    static void solve() {
        int n = sc.nextInt();
        int[] c = new int[n];
        for(int i=0; i<n; i++) {
            c[i] = Integer.parseInt(sc.next());
        }
        long minx = c[0];
        long miny = c[1];

        long min = minx * n + miny * n;
        long sumx = c[0];
        long sumy = c[1];

        for(int i=2; i<n; i++) {
            int xc;
            int yc;
            if(i%2 == 0) {
                xc = i/2 + 1;
                yc = i/2;
                sumx += c[i];
                minx = Math.min(minx, c[i]);
            } else {
                xc = i/2 + 1;
                yc = i/2 + 1;
                sumy += c[i];
                miny = Math.min(miny, c[i]);
            }
            min = Math.min(min, sumx + (n-xc) * minx + sumy + (n-yc) * miny);
        }

        out.println(min);

    }

}

 */
