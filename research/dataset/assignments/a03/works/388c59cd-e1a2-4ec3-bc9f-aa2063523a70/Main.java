import java.util.*;

public class file
{
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        while(t!=0)
        {
            sc.nextLine();
            int n = sc.nextInt();
            int k = sc.nextInt();
            int[] position = new int[k];
            for(int i = 0;i < k;i++)
            {
                position[i] = sc.nextInt();
            }
            int[] temprature = new int[k];
            for(int i = 0;i < k;i++)
            {
                temprature[i] = sc.nextInt();
            }
            int[] left = new int[n];
            int[] right = new int[n];
            int c[] = new int[n];
            Arrays.fill(c, Integer.MAX_VALUE);
            for(int i = 0;i < k;i++)
            {
                c[position[i]-1] = temprature[i];
            }
            long min = Integer.MAX_VALUE - 1;
            for(int i = 0;i < n;i++)
            {
                min = Math.min(min+1, c[i]);
                left[i] = (int)min;

            }
            min = Integer.MAX_VALUE - 1;
            for(int i = n-1;i >= 0;i--)
            {
                min = Math.min(min+1, c[i]);
                right[i] = (int)min;
            }
            for(int i = 0;i < n;i++)
            {
                System.out.print(Math.min(left[i], right[i])+" ");
            }
            System.out.println();
            t--;
        }
    }
}