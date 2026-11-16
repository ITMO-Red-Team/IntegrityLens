import java.util.*;
import java.io.*;

public class _1547_E {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        int t = Integer.parseInt(in.readLine());
        while(t-- > 0) {
            in.readLine();
            StringTokenizer line1 = new StringTokenizer(in.readLine());
            int n = Integer.parseInt(line1.nextToken());
            int k = Integer.parseInt(line1.nextToken());
            int[] x = new int[n];
            line1 = new StringTokenizer(in.readLine());
            StringTokenizer line2 = new StringTokenizer(in.readLine());
            TreeSet<AC> after = new TreeSet<AC>();
            for(int i = 0; i < k; i++) {
                int ai = Integer.parseInt(line1.nextToken()) - 1;
                int ti = Integer.parseInt(line2.nextToken());
                x[ai] = ti;
                after.add(new AC(ti, ai));
            }
            TreeSet<AC> before = new TreeSet<AC>();
            int[] res = new int[n];
            for(int i = 0; i < n; i++) {
                if(x[i] > 0) {
                    after.remove(new AC(x[i], i));
                    before.add(new AC(x[i], n - i));
                }
                AC be = before.size() == 0 ? null : before.first();
                AC af = after.size() == 0 ? null : after.first();
                if(be == null) {
                    res[i] = af.d + af.t - i;
                }else if(af == null) {
                    res[i] = be.d + be.t - (n - i);
                }else {
                    res[i] = Math.min(af.d + af.t - i, be.d + be.t - (n - i));
                }
            }
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < n; i++) {
                sb.append(res[i]);
                sb.append(' ');
            }
            out.println(sb.toString());
        }
        in.close();
        out.close();
    }
    static class AC implements Comparable<AC> {
        int t, d;
        AC(int tt, int dd) {
            t = tt;
            d = dd;
        }
        @Override
        public int compareTo(AC o) {
            if(t + d == o.t + o.d) {
                return d - o.d;
            }
            return t + d - (o.t + o.d);
        }
    }
}
