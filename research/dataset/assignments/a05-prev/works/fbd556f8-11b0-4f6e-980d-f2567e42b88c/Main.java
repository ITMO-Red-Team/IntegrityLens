import java.util.*;
import java.io.*;
public class codeforces {

    public static void main(String []args){
        FastScanner sc = new FastScanner();
        int t = sc.nextInt();
        while(t-->0)   {
            int n = sc.nextInt();
            String s[] = new String[n];
            for(int i=0;i<n;i++)
                s[i] = sc.next();
            int a[][] = new int[n][6];
            for(int i=0;i<n;i++)
                for(int j=0;j<s[i].length();j++){
                    a[i][s[i].charAt(j)-'a']++;
                }
            for(int i=0;i<n;i++)
                a[i][5] = s[i].length();
            int ans=0;
           for(int i=0;i<5;i++)
           {
             ans = Math.max(ans,help(a,i,n));
           }
           System.out.println(ans);
        }
    }
    public static int help(int a[][],int i,int n){
        int total=0,count=0,count1=0;
        PriorityQueue<Integer> pq = new PriorityQueue<>();

        for(int j=0;j<n;j++)
        {
            if(2*a[j][i]-a[j][5]>0)
            {
                total+=(2*a[j][i]-a[j][5]);
                count++;
            }
            else if(2*a[j][i]-a[j][5]==0)
                count1++;
            else
                pq.add(a[j][5]-2*a[j][i]);
        }
        if(count>0)
        {
            count+=count1;
            int value=0;
            while(!pq.isEmpty()&&value<total){
                value+=pq.peek();
                pq.remove();
                count++;
            }
            if(value>=total)
                count--;
        }
        return count;
    }
    public static class Pair{
        public  int x;
        public  int y;
        public Pair(int x,int y){
            this.x=x;
            this.y=y;
        }
    }
    public static class FastScanner {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st=new StringTokenizer("");
        String next() {
            while (!st.hasMoreTokens())
                try {
                    st=new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }
        int[] readArray(int n) {
            int[] a=new int[n];
            for (int i=0; i<n; i++) a[i]=nextInt();
            return a;
        }
        long nextLong() {
            return Long.parseLong(next());
        }
    }
}
