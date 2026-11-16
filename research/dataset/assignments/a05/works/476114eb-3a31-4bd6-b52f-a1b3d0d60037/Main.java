
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class Interesting_Story {
    static class RealScanner {
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
            for (int i = 0; i < n; i++) a[i] = nextInt();
            return a;
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }

    public static void main(String[] args) {
        RealScanner sc = new RealScanner();
        int t = sc.nextInt();
        while (t-- > 0) {
            int n = sc.nextInt();
            List<String> l = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                l.add(sc.next());
            }
            int res = 0;
            for (int i = 0; i <= 5; i++) {
                List<Integer> myL = new ArrayList<>();
                for (String s : l) {
                    int count1 = 0, count2 = 0;
                    for (char ch : s.toCharArray()) {
                        if (ch == (char) (i + 97)) {
                            count1++;
                        } else {
                            count2++;
                        }
                    }
                    myL.add(count2 - count1);
                }
                //System.out.println(myL);
                Collections.sort(myL);
                int sum = 0;
                for (int m = 0; m < myL.size(); m++) {
                    sum += myL.get(m);
                    if (sum < 0) {
                        res = Math.max(res, m + 1);
                    }
                }
            }
            System.out.println(res);
        }
    }
}
