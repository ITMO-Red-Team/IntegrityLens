
import java.util.Scanner;

public class Menorah {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int t = scan.nextInt();
        k:
        while (t-- > 0) {
            int n = scan.nextInt();
            String a = scan.next();
            String b = scan.next();
            int zo = 0;
            int oz = 0;
            int ind = -1;
            for (int i = 0; i < n; i++) {
                if (a.charAt(i) == '0' && b.charAt(i) == '1') {
                    ++zo;
                } else if (a.charAt(i) == '1' && b.charAt(i) == '0') {
                    ++oz;
                } else if (a.charAt(i) == '1' && b.charAt(i) == '1') {
                    ind = i;
                }
            }
            int res = 1000000000;
            int res2 = 1000000000;
            if (zo == oz) {
                res = zo + oz;
            }
            if (ind != -1) {
                char[] s = a.toCharArray();
                for (int i = 0; i < n; i++) {
                    if (i == ind) {
                        continue;
                    }
                    if (s[i] == '0') {
                        s[i] = '1';
                    } else {
                        s[i] = '0';
                    }
                }

                zo = 0;
                oz = 0;

                for (int i = 0; i < n; i++) {

                    if (s[i] == '0' && b.charAt(i) == '1') {
                        ++zo;
                    } else if (s[i] == '1' && b.charAt(i) == '0') {
                        ++oz;
                    }
                }

                if (zo == oz) {
                    res2 = zo + oz + 1;
                }

            }
            if (res == 1000000000 && res2 == 1000000000) {
                System.out.println("-1");
            } else {
                System.out.println(Math.min(res, res2));
            }

        }
    }
}
