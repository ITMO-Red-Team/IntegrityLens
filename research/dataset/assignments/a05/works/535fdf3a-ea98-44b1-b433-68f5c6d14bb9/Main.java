import java.util.*;
public class code
{
    int hell(String arr[],int a,int n)
    {
        int sum=0;
        int s=0;
        int arr1[]=new int[n];
        int x=0;
        int y=0;
        for(int i=0;i<n;i++)
        {
            int l=arr[i].length();
            for(int j=0;j<l;j++)
            {
                if(arr[i].charAt(j)==a)
                    x++;
                else
                    y++;
            }
            arr1[i]=y-x;
            y=0;
            x=0;
        }
        Arrays.sort(arr1);
        for(int i=0;i<n;i++)
        {
            s+=arr1[i];
            if(s<0)
                sum++;
            else    
                break;
        }
        return sum;
    }
    public static void main(String args[])
    {
        code ob=new code();
        Scanner sc=new Scanner(System.in);
        int t=sc.nextInt();
        while(t-->0)
        {
            int sum1=0;
            int sum2=0;
            int n=sc.nextInt();
            String arr[]=new String[n];
            for(int i=0;i<n;i++)
            {
                arr[i]=sc.next();
            }
            for(int i=97;i<=102;i++)
            {
                sum2=ob.hell(arr,i,n);
                sum1=Math.max(sum2,sum1);
            }
            System.out.println(sum1);
        }
    }
}