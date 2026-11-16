//package Task1;

import java.util.Scanner;

public class Menorah {
    static int MOD9= 1000000000;

    public static void main(String[] args){

        Scanner sc= new Scanner(System.in);
        int numberTest=sc.nextInt();
        while(numberTest-->0){
            int n=sc.nextInt();
            char[] s=new char[n+5];
            char[] t=new char[n+5];
            String ss=sc.next();
            String tt=sc.next();
            s=ss.toCharArray();
            t=tt.toCharArray();
            int cntax = 0, cntbx = 0, same = 0;
            int ans=MOD9;
            for(int i=0; i<n; i++){
                if(s[i]=='1')cntax++;
                if(t[i]=='1')cntbx++;
                if(s[i]==t[i])same++;
            }
            if(same==n){
                System.out.println(0);
                continue;
            }
            else if (cntax==0){
                System.out.println(-1);
                continue;
            }
            if(cntax==cntbx){
                ans=Math.min(ans,n-same);
            }
            if(n-cntax+1==cntbx)ans=Math.min(ans,same);
            if(ans<MOD9) System.out.println(ans);
            else System.out.println(-1);



        }

    }
}
