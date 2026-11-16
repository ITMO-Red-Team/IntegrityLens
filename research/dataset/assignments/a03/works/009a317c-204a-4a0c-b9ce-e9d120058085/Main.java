import java.util.*;
public class E1547 {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        int q = sc.nextInt();
        for(int i = 0; i < q; i++){
            int n = sc.nextInt();
            int k = sc.nextInt();
            int[][] t = new int[k][2];
            for(int j = 0; j < k; j++){
                t[j][0] = sc.nextInt();//room
            }
            for(int j = 0; j < k; j++){
                t[j][1] = sc.nextInt();//air
            }
            long[] left = new long[n];
            long[] right = new long[n];
            long tmp = Integer.MAX_VALUE;
            long[] max =new long[n];
            for(int j = 0; j < n; j++){
                max[j] = Integer.MAX_VALUE;
            }
            for (int j = 0; j < k; j++) {
                max[t[j][0]-1] = t[j][1];
            }
            for (int j = 1; j <= n; j++) {
                tmp = Math.min(tmp+1, max[j-1]);
                left[j-1] = tmp;
            }
            for(int j = n; j >= 1; j--){
                tmp = Math.min(tmp+1, max[j-1]);
                right[j-1] = tmp;
            }
            for(int j = 0; j < n; j++){
                System.out.print(Math.min(left[j], right[j]) + " ");
            }
            System.out.println();
        }
    }
}
