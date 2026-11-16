import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Div2 {
  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String[] s = reader.readLine().split(" ");
    StringBuilder sb = new StringBuilder();
    Div2 div2 = new Div2();
    int t = Integer.parseInt(s[0]);
    while (t-- > 0) {
      s = reader.readLine().split(" ");
      int n = Integer.parseInt(s[0]);
      s = reader.readLine().split(" ");
      long[] cs = new long[n];
      for (int i = 0; i < n; i++) {
        cs[i] = Long.parseLong(s[i]);
      }
      long min1 = cs[0];
      long min2 = cs[1];
      long cost = n * min1 + n * min2;
      long sum = min1 + min2;
      for (int i = 2; i < n; i++) {
        sum += cs[i];
        int count = i / 2;
        if (i % 2 == 0) {
          min1 = Math.min(min1, cs[i]);
          cost = Math.min(cost, sum + (n - count - 1) * min1 + (n - count) * min2);
        } else {
          count++;
          min2 = Math.min(min2, cs[i]);
          cost = Math.min(cost, sum + (n - count) * min1 + (n - count) * min2);
        }
      }
      sb.append(cost).append("\n");
    }
    System.out.println(sb.toString());
  }
}