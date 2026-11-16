import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class _1615C16 {

  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    int t = Integer.parseInt(in.readLine());
    int n;
    int s1, e1;
    char[] start, end;
    for (int s = 0; s < t; s++) {
      n = Integer.parseInt(in.readLine());
      start = in.readLine().toCharArray();
      end = in.readLine().toCharArray();
      s1 = 0;
      e1 = 0;
      for (int i = 0; i < n; i++) if (start[i] == '1') s1++;
      for (int i = 0; i < n; i++) if (end[i] == '1') e1++;
      if (e1 == n - s1 + 1) {
        for (int i = 0; i < n; i++) {
          if (start[i] == '1') start[i] = '0';
          else start[i] = '1';
        }
        int ct = 0;
        for (int i = 0; i < n; i++) if (start[i] != end[i]) ct++;
        int ct2 = Integer.MAX_VALUE;
        if (s1 == e1) {
          for (int i = 0; i < n; i++) {
            if (start[i] == '1') start[i] = '0';
            else start[i] = '1';
          }
          ct2 = 0;
          for (int i = 0; i < n; i++) if (start[i] != end[i]) ct2++;
        }
        System.out.println(Math.min(ct, ct2));
      } else if (s1 == e1) {
        int ct = 0;
        for (int i = 0; i < n; i++) if (start[i] != end[i]) ct++;
        int ct2 = Integer.MAX_VALUE;
        if (e1 == n - s1 + 1) {
          ct2 = 0;
          for (int i = 0; i < n; i++) if (start[i] != end[i]) ct2++;
        }
        System.out.println(Math.min(ct, ct2));
      } else {
        System.out.println(-1);
      }
    }
  }
}
