import java.util.Arrays;
import java.util.Scanner;

public class First {
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        int t = sc.nextInt();
        while (t-- > 0) {
            int n = sc.nextInt();
            String a = sc.next();
            String b = sc.next();
            char ch1[] = a.toCharArray();
            char ch2[] = b.toCharArray();
            int zz = 0;
            int oz = 0;
            int zo = 0;
            int oo = 0;
            for (int i = 0; i < n; i++) {
                if (ch1[i] == '0') {
                    if (ch2[i] == '0') {
                        zz += 1;
                    } else {
                        oz += 1;
                    }
                } else {
                    if (ch2[i] == '0') {
                        zo += 1;
                    } else {
                        oo += 1;
                    }
                }
            }
            int ans = -1;
            if ((oo - zz) == 1 || zo == oz) {
                int s1 = (int) 1e7;
                int s2 = (int) 1e7;
                if ((oo - zz) == 1) {
                    s1 = oo + zz;
                }
                if (zo == oz)
                    s2 = zo + oz;
                ans = Math.min(s1, s2);
            }
            System.out.println(ans);
        }
    }
}

//9
//001011011
//011010101    3 2 2 2
//9
//100010111
//101101100    2 1 3 3