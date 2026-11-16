import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException{
        //BufferedReader f = new BufferedReader(new FileReader("uva.in"));
        BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        int t = Integer.parseInt(f.readLine());
        while(t-- > 0) {
            int n = Integer.parseInt(f.readLine());
            int[][] occ = new int[n][6];
            for(int i = 0; i < n; i++) {
                char[] temp = f.readLine().toCharArray();
                for(char j: temp) {
                    occ[i][j-'a']++;
                }
                occ[i][5] = occ[i][0]+occ[i][1]+occ[i][2]+occ[i][3]+occ[i][4];
            }
            int max = 0;
            for(int i = 0; i < 5; i++) {
                int[] temp = new int[n];
                for(int j = 0; j < n; j++) {
                    temp[j] = occ[j][i]-(occ[j][5]-occ[j][i]);
                }
                Arrays.sort(temp);
                int j;
                int cur = 0;
                for(j = n-1; j >= 0; j--) {
                    if(cur+temp[j] <= 0) {
                        break;
                    }
                    cur += temp[j];
                }
                max = Math.max(max, n-j-1);
            }
            out.println(max);
        }
        f.close();
        out.close();
    }
}