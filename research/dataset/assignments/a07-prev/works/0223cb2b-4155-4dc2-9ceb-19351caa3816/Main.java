import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Armchair {
    public static void main(String args[]){
        Scanner in=new Scanner(System.in);
        int n=in.nextInt();
        int arr[]=new int[n];
        ArrayList<Integer> list1=new ArrayList<Integer>();
         ArrayList<Integer> list2=new ArrayList<Integer>();
        for(int i=0;i<n;i++)
        {
            int a=in.nextInt();
            if(a==0)
                list2.add(i);
            else
                list1.add(i);
        }
        long dp[][]=new long[list1.size()+1][list2.size()+1];
        
        solve(list1,list2,dp);
        System.out.println(dp[list1.size()][list2.size()]);
    }

public static void solve( ArrayList<Integer> list1,ArrayList<Integer> list2,long dp[][]){
       for(int i=1;i<=list1.size();i++)
		dp[i][0]=Integer.MAX_VALUE;

	for(int i=1;i<=list1.size();i++){
		for(int j=1;j<=list2.size();j++){
			dp[i][j]=Math.min(Math.abs(list1.get(i-1)-list2.get(j-1))+dp[i-1][j-1],dp[i][j-1]);
		}
	}
}
}