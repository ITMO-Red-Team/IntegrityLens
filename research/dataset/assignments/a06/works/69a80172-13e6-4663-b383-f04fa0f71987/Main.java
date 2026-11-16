import java.util.*;
//import java.util.Stack;


public class Main {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        int t=s.nextInt();

        while(t-->0) {
            int n=s.nextInt();
            long[] a=new long[n];
            long[] odd=new long[n];
            long[] even=new long[n];
            long[] sum=new long[n];
            long m1=Long.MAX_VALUE;
            long m2=Long.MAX_VALUE;
            long st=(long)0;
            for(int i=0;i<n;i++){
              a[i]=s.nextLong();
              if(i%2==0&&m1>a[i])
                  m1=a[i];
              if(i%2==1&&m2>a[i])
                  m2=a[i];
              st+=a[i];
              odd[i]=m1;
              even[i]=m2;
              sum[i]=st;
            }
            long minc=Long.MAX_VALUE;
            for(int i=1;i<n;i++){

                long c=sum[i]+odd[i]*(n-i-1+(i+1)/2)+even[i]*(n-(i+1)/2);
                if(c<minc&&c>0)
                    minc=c;
            }
            System.out.println(minc);
        }
    }
}
