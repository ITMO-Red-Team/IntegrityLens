import java.io.*;
import java.util.*;
public class Ishu
{
    static Scanner scan = new Scanner(System.in);
    static BufferedWriter output = new BufferedWriter(new OutputStreamWriter(System.out)); 
    static void tc() throws Exception
    {
    int n = scan.nextInt();
    int[] a = new int[n];
    int one = 0;
    int i,j;
    
    for(i=0;i<n;++i)
        {
        a[i] = scan.nextInt();
        one += a[i];
        }
        
    int[] pos = new int[one];
    one = 0;
    for(i=0;i<n;++i)  
        if(a[i] == 1)
            pos[one++] = i;
          
    long[][] dp = new long[one][n];
    
    if(one == 0)
        {
        output.write("0\n");
        output.flush();
        return;
        }
        
    if(a[0] == 0)
        dp[0][0] = pos[0];
    else 
        dp[0][0] = Integer.MAX_VALUE;
    for(j=1;j<n;++j)    
        {
        if(a[j] == 1)
            {
            dp[0][j] = dp[0][j-1];
            continue;
            }
            
        dp[0][j] = Math.abs(pos[0] - j);
        dp[0][j] = Math.min(dp[0][j], dp[0][j-1]);    
        }
     
     
    for(i=1;i<one;++i)  
        {
        int cnt = 0;
        for(j=0;j<n;++j)
            {
            if(a[j] == 0)
                ++cnt;
            if(cnt == i + 1)
                break;
             
            dp[i][j] = Integer.MAX_VALUE;
            }
             
        for(;j<n;++j)
            {
            if(a[j] == 1)
                {
                dp[i][j] = dp[i][j-1];
                continue; 
                }    
                
            dp[i][j] = Math.abs(pos[i] - j) + dp[i-1][j-1];
            dp[i][j] = Math.min(dp[i][j], dp[i][j-1]); 
            }
        }  
    
//    for(i=0;i<one;++i)
//        {
//        for(j=0;j<n;++j)
//            output.write(dp[i][j] + " ");
//        output.write("\n");
//        }
    long ans = dp[one-1][n-1];
    output.write(ans + "\n");    
    output.flush();
    }
    public static void main(String[] args) throws Exception
    {
    int t = 1;
    //t = scan.nextInt();
    while(t-- > 0)
        tc();
    }
}
