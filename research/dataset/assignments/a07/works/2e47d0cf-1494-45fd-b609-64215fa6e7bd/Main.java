import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main
{
	public static void main(String[] args)
	{
		Scanner sc=new Scanner(System.in);
		int n=sc.nextInt();
		int[] a=new int[n];
		for(int i=0;i<n;i++)
			a[i]=sc.nextInt();
		ArrayList<Integer> pos=new ArrayList<>(n);
		for(int i=0;i<n;i++)
			if(a[i]==1)
				pos.add(i);
		int k=pos.size();
		int[][] dp=new int[n+1][k+1];
		for(int i=0;i<n+1;i++)
			Arrays.fill(dp[i], Integer.MAX_VALUE);
		dp[0][0]=0;
		for(int i=0;i<n;i++)
			for(int j=0;j<=k;j++)
			{
				if(dp[i][j]==Integer.MAX_VALUE)
					continue;
				dp[i+1][j]=Math.min(dp[i+1][j], dp[i][j]);
				if(j<k && a[i]==0)
					dp[i+1][j+1]=Math.min(dp[i+1][j+1], dp[i][j]+Math.abs(pos.get(j)-i));
			}
		System.out.println(dp[n][k]);
    }
}