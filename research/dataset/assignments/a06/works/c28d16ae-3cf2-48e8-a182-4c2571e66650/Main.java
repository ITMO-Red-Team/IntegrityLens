import java.util.*;
import java.io.*;

public class C_Minimum_Grid_Path{
    public static void main(String[] args) {
        FastScanner s= new FastScanner();
        StringBuilder res = new StringBuilder();
        int t=s.nextInt();
        int p=0;
        while(p<t){
       int n=s.nextInt();
       int array[]= new int[n];
       long sumodd[]= new long[n];
       long sumeven []= new long[n];
       long minodd[]= new long[n];
       long mineven[]= new long[n];
       long sume=0;
       long sumo=0;
       for(int i=0;i<n;i++){
           array[i]=s.nextInt();
           if(i%2==0){
               sume+=array[i];
               sumeven[i]=sume;
               if(i==0){
                   mineven[i]=array[i];
               }
               else{
                   mineven[i]=Math.min(array[i],mineven[i-2]);
               }
           }
           else{
               sumo+=array[i];
               sumodd[i]=sumo;
               if(i==1){
                minodd[i]=array[i];
            }
            else{
                minodd[i]=Math.min(array[i],minodd[i-2]);
            }
           }
       }
       long ans=Long.MAX_VALUE;
       for(int i=1;i<n;i++){
           long temp=0;
           if(i%2==0){
               int num=i/2;
              temp=(mineven[i]*(n-num))+(sumeven[i]-mineven[i])+(minodd[i-1]*(n-(num-1)))+(sumodd[i-1]-minodd[i-1]);
              ans=Math.min(ans,temp);
           }
           else{
               int num=i/2;
               temp=(minodd[i]*(n-num))+(sumodd[i]-minodd[i])+(mineven[i-1]*(n-num))+(sumeven[i-1]-mineven[i-1]);
               ans=Math.min(ans,temp);
           }
       }
       res.append(ans+"\n");
       p++;
    }
    System.out.println(res);



    }
    static class FastScanner {
		BufferedReader br;
		StringTokenizer st;
 
		public FastScanner(String s) {
			try {
				br = new BufferedReader(new FileReader(s));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 
		public FastScanner() {
			br = new BufferedReader(new InputStreamReader(System.in));
		}
 
		String nextToken() {
			while (st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}
 
		int nextInt() {
			return Integer.parseInt(nextToken());
		}
 
		long nextLong() {
			return Long.parseLong(nextToken());
		}
 
		double nextDouble() {
			return Double.parseDouble(nextToken());
		}
	}




}