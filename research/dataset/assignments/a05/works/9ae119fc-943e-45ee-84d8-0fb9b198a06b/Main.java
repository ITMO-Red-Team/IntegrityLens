import java.util.*;
import java.io.*;


public class MyClass {
    public static void main(String args[]) {
     Scanner sc = new Scanner(System.in);
     int t = sc.nextInt();
     while(t>0)
     {
         int n = sc.nextInt();
         sc.nextLine();
         String [] str = new String[n];
         int res = 0;
         for(int i=0;i<n;i++)
         {
             str[i]=sc.nextLine();
         }
         int [][] freq = new int [n][5];
         for(int i=0;i<n;i++)
         {
             for(int j=0;j<str[i].length();j++)
             {
                 int k = str[i].charAt(j)-'a';
                 freq[i][k]++;
             }
         }
         for(int i=0;i<5;i++)
         {
             int [] arr = new int[n];
             for(int j=0;j<n;j++)
             {
                 int pos = freq[j][i];
                 int sum=0;
                 for(int k = 0;k<5;k++)
                 {
                     sum+=freq[j][k];
                 }
                 sum-=pos;
                 arr[j]=(pos-sum);
             }
             Arrays.sort(arr);
            //  int p = n-1;
             int count=0;
             int sum=0;
             for(int p=n-1;p>=0;p--)
             {
                 sum+=arr[p];
                 if(sum>0)
                 {
                     count++;
                 }
                 else
                 {
                     break;
                 }
             }
             res=Math.max(count , res);
         }
         
         System.out.println(res);
         t--;
         
     }
    }
}