import java.io.*;
import java.util.*;

public class E_Air_Conditioners{

    public static void main(String Args[]) throws Exception{
        BufferedReader br = new BufferedReader (new InputStreamReader(System.in));

        int t= Integer.parseInt(br.readLine());
        StringTokenizer st;

        while(t-->0){
            String gap=br.readLine();
            
            st=new StringTokenizer(br.readLine());
            int n= Integer.parseInt(st.nextToken());
            int k= Integer.parseInt(st.nextToken());

            int pos[]=new int[k];
            st=new StringTokenizer(br.readLine());

            for(int i=0;i<k;i++){
                pos[i]=Integer.parseInt(st.nextToken())-1;
            }

            int temp[]=new int[k];
            st=new StringTokenizer(br.readLine());

            int ans[]=new int[n];
            int l[]=new int[n];
            int r[]=new int[n];

            Arrays.fill(ans,Integer.MAX_VALUE);

            for(int i=0;i<k;i++){
                temp[i]=Integer.parseInt(st.nextToken());
                ans[pos[i]]=temp[i];
            }



            int min=Integer.MAX_VALUE;
            for(int i=0;i<n;i++){
                if(min==Integer.MAX_VALUE){
                    min=ans[i];
                }
                else{
                    min=Math.min(min+1,ans[i]);
                }
                l[i]=min;
            }

            min=Integer.MAX_VALUE;
            for(int i=n-1;i>=0;i--){
                if(min==Integer.MAX_VALUE){
                    min=ans[i];
                }
                else{
                    min=Math.min(min+1,ans[i]);
                }
                r[i]=min;
            }

            for(int i=0;i<n;i++){
                System.out.print(Math.min(l[i],r[i])+" ");
            }
            System.out.println();

        }

    }
}
