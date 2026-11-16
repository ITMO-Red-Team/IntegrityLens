import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class E {
    static long mod=(long)1e9+7;
    static long mod1=998244353;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);


        int t= in.nextInt();
        while(t-->0) {

            int n = in.nextInt();
            int k = in.nextInt();
            int[] a= in.readArray(k);
            int[] temp = in.readArray(k);
            int[] pre = new int[n];
            Arrays.fill(pre,Integer.MAX_VALUE);
            int[] suf = new int[n];
            Arrays.fill(suf,Integer.MAX_VALUE);
            for(int i = 0;i<k;i++){
                pre[a[i]-1]=temp[i];
                suf[a[i]-1]=temp[i];
            }
            int min = Integer.MAX_VALUE;
            E.ruffleSort(a);
            for(int i=a[0]-1;i<n;i++){
                min = Math.min(min,pre[i]);
                pre[i] = min;
                min++;

            }
            min = Integer.MAX_VALUE;
            for(int i = a[k-1]-1;i>=0;i--){
                min = Math.min(min,suf[i]);
                suf[i] = min;
                min++;
            }

            for(int i=0;i<n;i++)
                out.print(Math.min(pre[i],suf[i])+" ");
            out.println();
        }
        out.close();
    }




    static final Random random=new Random();

    static void ruffleSort(int[] a) {
        int n=a.length;//shuffle, then sort
        for (int i=0; i<n; i++) {
            int oi=random.nextInt(n), temp=a[oi];
            a[oi]=a[i]; a[i]=temp;
        }
        Arrays.sort(a);
    }
    static long gcd(long x, long y){
        if(x==0)
            return y;
        if(y==0)
            return x;
        long r=0, a, b;
        a = Math.max(x, y);
        b = Math.min(x, y);
        r = b;
        while(a % b != 0){
            r = a % b;
            a = b;
            b = r;
        }
        return r;
    }
    static long modulo(long a,long b,long c){
        long x=1,y=a%c;
        while(b > 0){
            if(b%2 == 1)
                x=(x*y)%c;
            y = (y*y)%c;
            b = b>>1;
        }
        return  x%c;
    }
    public static void debug(Object... o){
        System.err.println(Arrays.deepToString(o));
    }

    static int upper_bound(int[] arr,int n,int x){
        int mid;
        int low=0;
        int high=n;
        while(low<high){
            mid=low+(high-low)/2;
            if(x>=arr[mid])
                low=mid+1;
            else
                high=mid;
        }
        return low;
    }

    static int lower_bound(int[] arr,int n,int x){
        int mid;
        int low=0;
        int high=n;
        while(low<high){
            mid=low+(high-low)/2;
            if(x<=arr[mid])
                high=mid;
            else
                low=mid+1;
        }
        return low;
    }
    static String printPrecision(double d){
        DecimalFormat ft = new DecimalFormat("0.00000000000");
        return String.valueOf(ft.format(d));
    }
    static int countBit(long mask){
        int ans=0;
        while(mask!=0){
            mask&=(mask-1);
            ans++;
        }
        return ans;
    }

    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }
        public long nextLong() {
            return Long.parseLong(next());
        }
        public double nextDouble() {
            return Double.parseDouble(next());
        }
        public int[] readArray(int n)
        {
            int[] arr=new int[n];
            for(int i=0;i<n;i++) arr[i]=nextInt();
            return arr;
        }
    }
}