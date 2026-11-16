import java.util.*;
import java.io.*;

public class _1615_C {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(in.readLine());
        while(t-- > 0) {
            int n = Integer.parseInt(in.readLine());
            String a = in.readLine();
            String b = in.readLine();
            int x = 0;
            int y = 0;
            int match = 0;
            for(int i = 0; i < n; i++) {
                if(a.charAt(i) == '1') {
                    x++;
                }
                if(b.charAt(i) == '1') {
                    y++;
                }
                if(a.charAt(i) == b.charAt(i)) {
                    match++;
                }
            }
            if(x != y && n - x + 1 != y) {
                out.println(-1);
            }else {
                int moves = Integer.MAX_VALUE;
                if(x == y) {
                    int mismatch = n - match;
                    moves = Math.min(moves, mismatch);
                }
                if(n - x + 1 == y) {
                    moves = Math.min(moves, match);
                }
                out.println(moves);
            }
        }
        in.close();
        out.close();
    }
}
