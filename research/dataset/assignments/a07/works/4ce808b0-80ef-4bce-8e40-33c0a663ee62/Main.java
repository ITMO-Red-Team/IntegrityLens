import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class _109D {
    static BufferedReader br;

    public static void main(String[] args) throws IOException {
        br = new BufferedReader(new InputStreamReader(System.in));
        int n = readInt();
        int arr[] = readIntarray();
        ArrayList<Integer> a = new ArrayList<>();
        ArrayList<Integer> b = new ArrayList<>();
        for(int i = 0; i < n; i++){
            if(arr[i] == 1){
                a.add(i);
            }else{
                b.add(i);
            }
        }
        if(a.size() == 0){
            System.out.println("0");
            return;
        }
        int [][] dp = new int[a.size()][b.size()];
        for(int i = 0; i < a.size(); i++){
            for(int j = i; j < b.size(); j++) {
                if (j == 0) {
                    dp[i][j] = Math.abs(a.get(i) - b.get(j));
                } else if (i == 0) {
                    dp[i][j] = Math.min(dp[i][j - 1], Math.abs(a.get(i) - b.get(j)));
                } else if (i == j) {
                    dp[i][j] = dp[i - 1][j - 1] + Math.abs(a.get(i) - b.get(j));
                } else {
                    dp[i][j] = Math.min(dp[i][j - 1], dp[i - 1][j - 1] + Math.abs(a.get(i) - b.get(j)));
                }
            }
        }
        System.out.println(dp[a.size() - 1][b.size() - 1]);
    }


    static int readInt() throws IOException {
        return Integer.parseInt(br.readLine());
    }

    static long readLong() throws IOException {
        return Long.parseLong(br.readLine());
    }

    static int[] readIntarray() throws IOException {
        String[] _a = br.readLine().split(" ");
        int[] _res = new int[_a.length];
        for (int i = 0; i < _a.length; i++) {
            _res[i] = Integer.parseInt(_a[i]);
        }
        return _res;
    }

    static long[] readLongarray() throws IOException {
        String[] _a = br.readLine().split(" ");
        long[] _res = new long[_a.length];
        for (int i = 0; i < _a.length; i++) {
            _res[i] = Long.parseLong(_a[i]);
        }
        return _res;
    }
}
