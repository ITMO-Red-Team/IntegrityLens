import java.lang.*;
import java.util.*;
import java.io.*;

public class Main {    
    static void deal(int[] arr,int n) {
        int m1 = 0;
        int m2 = 0;
        for(int i=0;i<n;i++) {
            if(arr[i]==1) {
                m1 ++;
            } else {
                m2 ++;
            }
        }
        if(m1 ==0) {
            System.out.println(0);
            return;
        }
        int[] arr1 = new int[m1];
        int[] arr2 = new int[m2];
        int index1 = 0;
        int index2 = 0;
        for(int i=0;i<n;i++) {
            if(arr[i] == 1) {
                arr1[index1] = i;
                index1++;
            } else {
                arr2[index2] = i;
                index2++;
            }
        }
        int[][] dp = new int[m2][m1];
        dp[0][0] = Math.abs(arr1[0]-arr2[0]);
        for(int i=0;i<m1;i++) {
            if(i>0) dp[i][i] = dp[i-1][i-1] + Math.abs(arr1[i]-arr2[i]);
            for(int j=i+1;j<m2;j++) {
                if(i>0) {
                    dp[j][i] = Math.min(dp[j-1][i],dp[j-1][i-1]+Math.abs(arr2[j]-arr1[i]));
                } else {
                    dp[j][i] = Math.min(dp[j-1][i],Math.abs(arr2[j]-arr1[i]));
                }
            }
        }
        System.out.println(dp[m2-1][m1-1]);
    }
    
	public static void main(String[] args) {
      	MyScanner scanner = new MyScanner();
        int n = scanner.nextInt();
        int[] arr = new int[n];
        for(int i=0;i<n;i++) {
            arr[i] = scanner.nextInt();    
        }
        deal(arr,n);
    }
    public static class MyScanner {
        BufferedReader br;
        StringTokenizer st;
        
        public MyScanner() {
                br = new BufferedReader(new InputStreamReader(System.in));
        }
        
        String next() {
                while (st == null || !st.hasMoreElements()) {
                        try {
                                st = new StringTokenizer(br.readLine());
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                return st.nextToken();
        }
        
        int nextInt() {
                return Integer.parseInt(next());
        }
        
        long nextLong() {
                return Long.parseLong(next());
        }
        
        double nextDouble() {
                return Double.parseDouble(next());
        }
        
        String nextLine(){
                String str = "";
    try {
            str = br.readLine();
    } catch (IOException e) {
            e.printStackTrace();
    }
    return str;
        }
        
    }
}