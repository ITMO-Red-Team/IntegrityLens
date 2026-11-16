import java.util.*;
import java.io.*;


public class Main{

    public static void main(String[] args) throws java.io.IOException {
        Scanner sc = new Scanner(System.in);
        int n=sc.nextInt();
        int[] arr=new int[n];
        int[][] dp=new int[n][n];
        int[][] min=new int[n][n];
        ArrayList<Integer> ones=new ArrayList<>();
        ArrayList<Integer> zero=new ArrayList<>();
        for(int i=0;i<n;++i) {
            arr[i] = sc.nextInt();
            if(arr[i]==1)
                ones.add(i);
            else
                zero.add(i);
        }
        for(int i=0;i<n;++i)
            for(int j=0;j<n;++j) {
                min[i][j] = Integer.MAX_VALUE;
                dp[i][j] = Integer.MAX_VALUE;
            }
        int len=ones.size();
        int zlen=zero.size();
        int minn=0;
        for(int i=0;i<len;++i)
        {
            int cur = ones.get(i);
            for(int j=i;j<zlen;j++)
            {
                int curz = zero.get(j);
                int cost = Math.abs(cur-curz);
                if(i!=0 && curz-1>=0)
                {
                    cost+=min[i-1][curz-1];
                }
                dp[i][curz]=cost;
            }
            minn=Integer.MAX_VALUE;
            for(int j=0;j<n;++j)
            {
                if(dp[i][j]<minn)
                    minn=dp[i][j];
                min[i][j]=minn;
            }
        }
        System.out.println(minn);
    }
}
// 1 0 1 1 0 0 1