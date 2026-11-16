import java.util.*;
import java.io.*;

public class C_Interesting_Story{
    public static void main(String[] args) {
        FastScanner s= new FastScanner();
        StringBuilder res = new StringBuilder();
        int t=s.nextInt();
        int p=0;
        while(p<t){
        int n=s.nextInt();
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0;i<n;i++){
            String str=s.nextToken();
            list.add(str);
        }
        long max=Integer.MIN_VALUE;
        for(int i=97;i<102;i++){
            char ch=(char)i;
            ArrayList<Integer> nice = new ArrayList<Integer>();
            for(int j=0;j<n;j++){
                String obj=list.get(j);
                int count=0;
                for(int k=0;k<obj.length();k++){
                     char ch2=obj.charAt(k);
                     if(ch2==ch){
                         count++;
                     }
                }
                int num=obj.length()-count;
                nice.add(count-num);

            }
            Collections.sort(nice,Collections.reverseOrder());
            long sum=0;
            long counting=0;
            for(int j=0;j<nice.size();j++){
                sum+=nice.get(j);
                if(sum>0){
                    counting++;
                }
                else{
                    break;
                }
            }
            max=Math.max(max,counting);

        }
        res.append(max+"\n");
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