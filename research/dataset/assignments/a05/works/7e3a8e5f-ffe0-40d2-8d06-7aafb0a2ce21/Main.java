import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
public class Main
{
    public static void main(String args[])
    {
     FastReader s=new FastReader();
     int t=s.nextInt();
     while(t>0)
     {
      Solve solve=new Solve();
      t--;
      int n=s.nextInt();
      String str[]=new String[n];
      for(int i=0;i<n;i++)
      str[i]=s.nextLine();
      char array[]=new char[]{'a','b','c','d','e'};
      int arr[]=new int[n];
      int ans=0;
      for(int i=0;i<5;i++)
      {
        Arrays.fill(arr,0);
          for(int j=0;j<n;j++)
          {
                 for(int k=0;k<str[j].length();k++)
                 {
                     if(str[j].charAt(k)==array[i])
                     arr[j]++;
                     else
                     arr[j]--;
                 }
          }
          ans=(ans>solve.solve(arr,n))?ans:solve.solve(arr,n);
      }
      System.out.println(ans);
     }
    }
}
class Solve{
    public int solve(int arr[],int n)
    {
        int ans=0;
        int sum=0;
        Arrays.sort(arr);
        for(int i=n-1;i>=0;i--)
        {
            if(sum+arr[i]>0)
            {
                sum+=arr[i];
                ans++;
            }
            else
            break;
        }
        return ans;
    }
}
class FastReader {
    BufferedReader br;
    StringTokenizer st;
    public FastReader()
    {
        br = new BufferedReader(
                new InputStreamReader(System.in));
    }
    String next()
    {
        while (st == null || !st.hasMoreElements()) {
            try {
                st = new StringTokenizer(br.readLine());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return st.nextToken();
    }

    int nextInt() { return Integer.parseInt(next()); }

    long nextLong() { return Long.parseLong(next()); }

    double nextDouble()
    {
        return Double.parseDouble(next());
    }

    String nextLine()
    {
        String str = "";
        try {
            str = br.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
