
import java.util.Scanner;

public class Menorah {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        while (t-- > 0) {
            int n = sc.nextInt();
            char initial[] = sc.next().toCharArray();
            char desired[] = sc.next().toCharArray();
            int lit1 = 0, lit2 = 0;
            int ans = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (initial[i] == '1') {
                    ++lit1;
                }
                if (desired[i] == '1') {
                    ++lit2;
                }
            }
            if (lit1 == lit2) {
                int count = 0;
                for (int i = 0; i < n; i++) {
                    if (initial[i] != desired[i]) {
                        ++count;
                    }
                }
                ans = Math.min(count, ans);
            }
            if (lit2 == (n - lit1 + 1)) {
                int count = 0;
                for (int i = 0; i < n; i++) {
                    if (initial[i] == desired[i]) {
                        ++count;
                    }
                }
                ans = Math.min(ans, count);
            }
            if (ans == Integer.MAX_VALUE) {
                System.out.println(-1);
            } else {
                System.out.println(ans);
            }
        }
    }
}