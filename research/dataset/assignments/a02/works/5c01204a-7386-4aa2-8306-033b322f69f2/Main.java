import java.util.*;
public class Main {
    public static void main(String args[]) {
        Scanner s = new Scanner(System.in);
        int t = s.nextInt();
        while (t-- > 0){
            int n = s.nextInt();
            String a = s.next();
            String b = s.next();
            int a1 = 0, b1 = 0;
            for (char c: a.toCharArray()){
                if (c == '1') a1++;
            }
            for (char c: b.toCharArray()){
                if (c == '1') b1++;
            }
            int ans = Integer.MAX_VALUE;
            int res = 0;
            for (int i = 0; i < n; i++) {
                if (a.charAt(i) != b.charAt(i)) res++;
            }
            if (a1 == b1) ans = Math.min(ans, res);
            if (b1 == n-a1+1) ans = Math.min(ans, n-res);
            if (ans == Integer.MAX_VALUE){
                System.out.println("-1");
            } else {
                System.out.println(ans);
            }
        }
    }
}