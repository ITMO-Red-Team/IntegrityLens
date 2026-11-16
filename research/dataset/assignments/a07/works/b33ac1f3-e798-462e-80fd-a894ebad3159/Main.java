import java.io.BufferedReader; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.util.Scanner; 
import java.util.StringTokenizer;
import java.util.*;
public class codeforcesD{
   static class FastReader 
    { 
        BufferedReader br; 
        StringTokenizer st; 
 
        public FastReader() 
        { 
            br = new BufferedReader(new
                     InputStreamReader(System.in)); 
        } 
 
        String next() 
        { 
            while (st == null || !st.hasMoreElements()) 
            { 
                try
                { 
                    st = new StringTokenizer(br.readLine()); 
                } 
                catch (IOException  e) 
                { 
                    e.printStackTrace(); 
                } 
            } 
            return st.nextToken(); 
        } 
 
        int nextInt() 
        { 
            return Integer.parseInt(next()); 
        } 
 
        long nextLong() 
        { 
            return Long.parseLong(next()); 
        } 
 
        double nextDouble() 
        { 
            return Double.parseDouble(next()); 
        } 
 
        String nextLine() 
        { 
            String str = ""; 
            try
            { 
                str = br.readLine(); 
            } 
            catch (IOException e) 
            { 
                e.printStackTrace(); 
            } 
            return str; 
        } 
    } 
    public static long dp[][];
    public static List<Integer> list1,list0;
   public static void main(String args[]){
        FastReader sc=new FastReader();
        int n=sc.nextInt();
        list1=new ArrayList<>();
        list0=new ArrayList<>();
        for(int i=0;i<n;i++){
           int a=sc.nextInt();
           if(a==0){list0.add(i);}
           if(a==1){list1.add(i);}
        }
        dp=new long[list1.size()][list0.size()];
        for(int i=0;i<list1.size();i++){
          for(int j=0;j<list0.size();j++){
              dp[i][j]=-1;
            }
        }
        System.out.println(check(0,0));
       
   } 
   public static long check(int i,int j){
          if(i>=list1.size()){return 0;}
          if(j>=list0.size()){return Integer.MAX_VALUE;}
          if(dp[i][j]!=-1){return dp[i][j];}
          dp[i][j]=Math.min(check(i+1,j+1)+(long)Math.abs(list1.get(i)-list0.get(j)),check(i,j+1));
         return dp[i][j];
     }
}
