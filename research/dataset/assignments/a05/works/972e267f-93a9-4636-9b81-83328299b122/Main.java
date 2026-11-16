
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class InterestingStory {

    public static void main(String args[]) {
        Scanner scan = new Scanner(System.in);
        int t = scan.nextInt();
        while (t-- > 0) {
            int n = scan.nextInt();
            String[] a = new String[n];
            for (int i = 0; i < n; i++) {
                a[i] = scan.next();
            }
            int res = -1;
            int[] st = new int[n];
            for (int i = 0; i < n; i++) {
                String s = a[i];
                int c = 0;
                for (int j = 0; j < s.length(); j++) {
                    if (s.charAt(j) == 'a') {
                        ++c;
                    }
                }
                st[i] = ((s.length() - c) - c);
            }
            sort(st);
            long sum = 0;
            int count = 0;
            for (int i = 0; i < n; i++) {
                if (st[i] + sum >= 0) {
                    break;
                }
                sum += st[i];
                ++count;
            }
            res = Math.max(res, count);

            for (int i = 0; i < n; i++) {
                String s = a[i];
                int c = 0;
                for (int j = 0; j < s.length(); j++) {
                    if (s.charAt(j) == 'b') {
                        ++c;
                    }
                }
                st[i] = ((s.length() - c) - c);
            }
            sort(st);
            sum = 0;
            count = 0;
            for (int i = 0; i < n; i++) {
                if (st[i] + sum >= 0) {
                    break;
                }
                sum += st[i];
                ++count;
            }
            res = Math.max(res, count);

            for (int i = 0; i < n; i++) {
                String s = a[i];
                int c = 0;
                for (int j = 0; j < s.length(); j++) {
                    if (s.charAt(j) == 'c') {
                        ++c;
                    }
                }
                st[i] = ((s.length() - c) - c);
            }
            sort(st);
            sum = 0;
            count = 0;
            for (int i = 0; i < n; i++) {
                if (st[i] + sum >= 0) {
                    break;
                }
                sum += st[i];
                ++count;
            }
            res = Math.max(res, count);

            for (int i = 0; i < n; i++) {
                String s = a[i];
                int c = 0;
                for (int j = 0; j < s.length(); j++) {
                    if (s.charAt(j) == 'd') {
                        ++c;
                    }
                }
                st[i] = ((s.length() - c) - c);
            }
            sort(st);
            sum = 0;
            count = 0;
            for (int i = 0; i < n; i++) {
                if (st[i] + sum >= 0) {
                    break;
                }
                sum += st[i];
                ++count;
            }
            res = Math.max(res, count);

            for (int i = 0; i < n; i++) {
                String s = a[i];
                int c = 0;
                for (int j = 0; j < s.length(); j++) {
                    if (s.charAt(j) == 'e') {
                        ++c;
                    }
                }
                st[i] = ((s.length() - c) - c);
            }
            sort(st);
            sum = 0;
            count = 0;
            for (int i = 0; i < n; i++) {
                if (st[i] + sum >= 0) {
                    break;
                }
                sum += st[i];
                ++count;
            }
            res = Math.max(res, count);
            System.out.println(res);

        }
    }

    static final Random random = new Random();

    static void sort(int[] a) {
        int n = a.length;//shuffle, then sort 
        for (int i = 0; i < n; i++) {
            int oi = random.nextInt(n), temp = a[oi];
            a[oi] = a[i];
            a[i] = temp;
        }
        Arrays.sort(a);
    }
}
