import java.util.*;
import java.lang.*;
import java.io.*;
 
/* Name of the class has to be "Main" only if the class is public. */
public class Codeforces {
    public static void main(String[] args) throws java.lang.Exception {
        /* your code goes here */
        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
       int t = Integer.parseInt(buf.readLine());
        StringBuilder sb = new StringBuilder();
       for (int i = 0; i < t; i++) {
           String st=buf.readLine();
          String st1[]=(buf.readLine()).split(" ");
          int n=Integer.parseInt(st1[0]);
          int k=Integer.parseInt(st1[1]);
          int a[]=new int[k];
          int temp[]=new int[k];
          long arr[]=new long[n];
          String st2[]=(buf.readLine()).split(" ");
           String st3[]=(buf.readLine()).split(" ");
           for(int j=0;j<k;j++)
           {
               a[j]=Integer.parseInt(st2[j]);
               temp[j]=Integer.parseInt(st3[j]);
           }
          for(int j=0;j<k;j++)
          {
              arr[a[j]-1]=temp[j];
          }
          long min=Integer.MAX_VALUE;
          long left[]=new long[n];
          long right[]=new long[n];
          if(arr[0]==0)
              left[0]=min;
          else
              left[0]=arr[0];
          for(int j=1;j<n;j++)
          {
              if(arr[j]==0)
              {
                  left[j]=left[j-1]+1;
              }
              else
              {
                  left[j]=Math.min(left[j-1]+1,arr[j]);
              }
          }
          if(arr[n-1]==0)
              right[n-1]=min;
          else
              right[n-1]=arr[n-1];
          for(int j=n-2;j>=0;j--)
          {
              if(arr[j]==0)
              {
                  right[j]=right[j+1]+1;
              }
              else
              {
                  right[j]=Math.min(right[j+1]+1,arr[j]);
              }
          }
          for(int j=0;j<n;j++)
          {
              arr[j]=Math.min(left[j],right[j]);
              sb.append(arr[j]+" ");
          }
          sb.append("\n");
        }
        System.out.println(sb);
    }
}