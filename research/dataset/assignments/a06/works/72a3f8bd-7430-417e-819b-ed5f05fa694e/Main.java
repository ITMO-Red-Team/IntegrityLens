//Praise our lord and saviour qlf9
//DecimalFormat f = new DecimalFormat("##.00");

import java.util.*;
import java.io.*;
import java.math.*;
import java.text.*;
public class C{
public static void main(String[] omkar) throws Exception
{
   BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
   StringTokenizer st = new StringTokenizer(in.readLine());
   StringBuilder sb = new StringBuilder();
   int cases = Integer.parseInt(st.nextToken());
   for(int i = 0; i < cases; i++)
   {
      solve(in, st, sb);
   }  
   System.out.println(sb);
        }
 public static void solve(BufferedReader in, StringTokenizer st, StringBuilder sb) throws Exception
 {
      st = new StringTokenizer(in.readLine());
      int n = Integer.parseInt(st.nextToken());
      int[] arr = readArr(n, in, st);
      int[] mins = new int[n];
      mins[0] = arr[0];
      mins[1] = arr[1]; 
      for(int i = 2; i < n; i++)
      {
         mins[i] = Math.min(arr[i], mins[i-2]);
      }
      long[] sums = new long[n];
      sums[0] = (long)(arr[0]);
      for(int i = 1; i < n; i++)
      {
         sums[i] = sums[i-1]+(long)(arr[i]);
      }
      long minc = Long.MAX_VALUE;
      long temp;
      for(int i = 1; i < n; i++)
      {
         temp = sums[i];
         temp += (long)(mins[i])*(long)(n-(i+2)/2);
         temp += (long)(mins[i-1])*(long)(n-(i+1)/2);
         minc = Math.min(minc, temp);
      }
      sb.append(minc+"\n");
 }
 public static int[] readArr(int N, BufferedReader in, StringTokenizer st) throws Exception
      {
         int[] arr = new int[N];
         st = new StringTokenizer(in.readLine());
         for(int i=0; i < N; i++)
            arr[i] = Integer.parseInt(st.nextToken());
         return arr;
      }


}