import java.util.*;
import java.io.*;
public class AirConditioner{
    public static void main(String[] args) throws Exception{
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        int t=Integer.parseInt(br.readLine());
        while(t-->0){
            br.readLine();
            StringTokenizer st=null;
            st=new StringTokenizer(br.readLine());
            int n=Integer.parseInt(st.nextToken());
            int k=Integer.parseInt(st.nextToken());
            int[] idx=new int[k];
            int[] temp=new int[k];
            st=new StringTokenizer(br.readLine());
            for(int i=0;i<k;i++){
                idx[i]=Integer.parseInt(st.nextToken());
            }
            st=new StringTokenizer(br.readLine());
            for(int i=0;i<k;i++){
                temp[i]=Integer.parseInt(st.nextToken());
            }
            int[] arr=new int[n];
            Arrays.fill(arr,Integer.MAX_VALUE-1);
            for(int i=0;i<k;i++){
                arr[idx[i]-1]=temp[i];
            }
            int[] left=new int[n];
            int[] right=new int[n];
            left[0]=Math.min(Integer.MAX_VALUE,arr[0]);
            for(int i=1;i<n;i++){
                left[i]=Math.min(left[i-1]+1,arr[i]);
            }
            right[n-1]=Math.min(Integer.MAX_VALUE,arr[n-1]);
            for(int i=n-2;i>=0;i--){
                right[i]=Math.min(right[i+1]+1,arr[i]);
            }
            for(int i=0;i<n;i++){
                arr[i]=Math.min(left[i],right[i]);
            }
            for(int tp:arr)
                System.out.print(tp+" ");
            System.out.println();

        }
    }
}
