import java.util.*;
import java.io.*;

public class Solution {

    private static ArrayList<Integer> prime = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        Scanner in=new Scanner(System.in);
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        StringBuffer out = new StringBuffer();

        int T = in.nextInt();

        OUTER:
        while(T-->0) {

            int n=in.nextInt(), k=in.nextInt();

            int a[]=new int[k];
            for(int i=0; i<k; i++) {
                a[i]=in.nextInt()-1;
            }

            int t[]=new int[k];
            long ans[]=new long[n];
            for(int i=0; i<k; i++) {
                t[i]=in.nextInt();
                ans[a[i]]=t[i];
            }

            long temp=Integer.MAX_VALUE;

            long left[]=new long[n];
            for(int i=0; i<n; i++) {
                if(ans[i]!=0) {
                    temp=Math.min(temp, ans[i]);
                }
                left[i]=temp;
                temp+=1;
            }

            temp=Integer.MAX_VALUE;

            long right[]=new long[n];
            for(int i=n-1; i>=0; i--) {
                if(ans[i]!=0) {
                    temp=Math.min(temp, ans[i]);
                }
                right[i]=temp;
                temp+=1;
            }

            for(int i=0; i<n; i++) {
                ans[i]=Math.min(left[i], right[i]);
                out.append(ans[i]+" ");
            }
            out.append("\n");
        }

        System.out.print(out);
    }

    private static long lcm(long a, long b) {
        return (a*b)/gcd(a, b);
    }

    private static long gcd(long a, long b) {
        if (a==0)
            return b;
        return gcd(b%a, a);
    }

    private static int toInt(String s) {
        return Integer.parseInt(s);
    }

    private static long toLong(String s) {
        return Long.parseLong(s);
    }
}