import java.util.*;

public class Solution{
    public static void main(String[] args){
        Scanner sc=new Scanner(System.in);
        int t=sc.nextInt();
        long max=(long)1e18;
        StringBuilder sb=new StringBuilder();
        while(t-->0){
            int n=sc.nextInt();
            int k=sc.nextInt();
            long[] ans=new long[n+1];
            long[] tem=new long[n+1];
            int[] item=new int[k];
            
            Arrays.fill(ans,max);
            
            for(int i=0;i<k;i++){
                item[i]=sc.nextInt();
               
            }
            for(int i=0;i<k;i++){
                tem[item[i]]=sc.nextLong();
            }
            for(int i=n;i>=1;i--){
                 if(tem[i]!=0){
                     if(i==n)ans[i]=Math.min(ans[i],tem[i]);
                     else{ ans[i]=Math.min(ans[i],ans[i+1]+1);
                           ans[i]=Math.min(tem[i],ans[i]);
                           
                     }
                 }
                 else{
                     if(i<n)ans[i]=Math.min(ans[i],ans[i+1]+1);
                   
                 }
            }
               for(int i=1;i<=n;i++){
                 if(tem[i]!=0){
                     if(i==1)ans[i]=Math.min(ans[i],tem[i]);
                     else{ ans[i]=Math.min(ans[i],ans[i-1]+1);
                     ans[i]=Math.min(tem[i],ans[i]);
                     }
                 }
                 else{
                     if(i>1)ans[i]=Math.min(ans[i],ans[i-1]+1);
                   
                 }
            }
            for(int i=1;i<=n;i++){
              
                sb.append(ans[i]+" ");
            }
            sb.append("\n");
        }
        System.out.println(sb);
    }
}