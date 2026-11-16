
import java.util.*;
/**
 * Created by Brandon on 4/29/2021, 11:04 AM.
 */
public class Main {
    static long mod = 998244353;

    public static void solve() {
        // string:
        //char** vals = new char* [n];
        //for (int i = 0; i < n; i++) {
        //	string str = "";
        //	cin >> str;
        //	vals[i] = new char[n];
        //	for (int j = 0; j < n; j++) {
        //		vals[i][j] = str[j];
        //	}
        //}
        int n;
        Scanner sc = new Scanner(System.in);
        n = Integer.parseInt(sc.next());
	    int[] arr = new int[n];
        ArrayList<Integer> ones = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            arr[i] = Integer.parseInt(sc.next());
            if (arr[i] == 1) {
                ones.add(i);
            }
        }
        if (ones.size() == 0) {
            System.out.println("0\n");
            return;
        }

	    int[] zeroes = new int[n - ones.size()];
        int cur = 0;
        for (int i = 0; i < n; i++) {
            if (arr[i] == 0) {
                zeroes[cur] = i;
                cur++;
            }
        }
	    int[][] dp = new int[ones.size()][n-ones.size()];
        for (int i = 1; i < ones.size(); i++) {
            dp[i][0] = 100000000;
        }
        dp[0][0] = Math.abs(ones.get(0) - zeroes[0]);
        for (int i = 1; i < n - ones.size(); i++) {
            dp[0][i] = Math.min(dp[0][i - 1], Math.abs(ones.get(0) - zeroes[i]));
        }
        // dp[i][j] is number where you only have to move first i but max position you can put is j
        for (int i = 1; i < ones.size(); i++) {
            for (int j = 1; j < n - ones.size(); j++) {
                dp[i][j] = Math.min(dp[i][j - 1], dp[i-1][j-1] + Math.abs(zeroes[j] - ones.get(i)));
            }
        }
        System.out.println(dp[ones.size()-1][n-ones.size()-1] + "\n");
    }
    public static void main(String[] args) {
        solve();
    }
}
