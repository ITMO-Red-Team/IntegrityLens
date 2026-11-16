import java.util.*;
import java.io.*;
public class Solution {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        public FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }
        int nextInt() {
            return Integer.parseInt(next());
        }
        long nextLong() {
            return Long.parseLong(next());
        }
        double nextDouble() {
            return Double.parseDouble(next());
        }
        String nextLine() {
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
    static void sort(int a[]){ // int -> long
        ArrayList<Integer> arr=new ArrayList<>(); // Integer -> Long
        for(int i=0;i<a.length;i++)
        arr.add(a[i]);
        Collections.sort(arr);
        for(int i=0;i<a.length;i++)
        a[i]=arr.get(i);
        
    }
    private static long gcd(long a, long b){
        if(b==0)return a;
        return gcd(b,a%b);
    }
    private static long pow(long x,long y){
        if(y==0)return 1;
        long temp = pow(x, y/2);
        if(y%2==1){
            return x*temp*temp;
        }
        else{
            return temp*temp;
        }
    }
    static int log(long n){
        int res = 0;
        while(n>0){
            res++;
            n/=2;
        }
        return res;
    }

    static int mod = (int)1e9+7;
    static int INF = Integer.MAX_VALUE;
    static PrintWriter out;
    static FastReader sc ;
    public static void main(String[] args) throws IOException {
        sc = new FastReader();
        out = new PrintWriter(System.out);
        // primes();
        // ================================ //
        int test = sc.nextInt();
        while (test-- > 0) {
            int n = sc.nextInt();
            String s = sc.nextLine();
            String t = sc.nextLine();
            solver(s,t, n);
        }
        // ================================ //
        // int n = sc.nextInt();
        // solver();
        // ================================ //
        out.flush();
    }

    public static void solver(String s, String t, int n) {
        int diff = 0;
        int one = 0;
        int zero = 0;
        for(int i=0;i<n;i++){
            if(s.charAt(i)!=t.charAt(i)){
                diff++;
                if(s.charAt(i)=='1')one++;
            }
            else{
                if(s.charAt(i)=='1')zero++;
            }
        }
        if(diff==0){
            out.println(0);
            return;
        }
        int res = INF;
        if(diff%2==0 && one==(diff-one)){
            res = diff;
        }
        if(n-diff-1>=0 && (n-diff-1)%2==0 && zero>0 && (n-diff-zero)==zero-1){
            res = Math.min(res, (n-diff-1)+1);
        }
        out.println(res==INF?-1:res);
    }
}
