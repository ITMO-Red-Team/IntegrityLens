import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class C {
    static long mod=(long)1e9+7;
    static long mod1=998244353;
    static InputStream inputStream = System.in;
    static OutputStream outputStream = System.out;
    static InputReader in = new InputReader(inputStream);
    static PrintWriter out = new PrintWriter(outputStream);

    public static void main(String[] args) throws IOException {

        int t= in.nextInt();
        while(t-->0) {

            int n = in.nextInt();
            ArrayList<Pair>[] adj = new ArrayList[5];
            for(int i = 0;i<5;i++) adj[i] = new ArrayList<>();
            for(int i = 0;i<n;i++){
                char[] arr = in.next().toCharArray();
                int[] count = new int[5];
                for(char c:arr)
                    count[c-'a']++;
                for(int j = 0;j<5;j++){
                    adj[j].add(new Pair(count[j],arr.length-count[j]));
                }
            }
            int max = 0;
            for(int i = 0;i<5;i++){
                Collections.sort(adj[i]);
                int count = 0;
                int sum1 = 0;
                int sum2 = 0;
                for(Pair p:adj[i]){
                    if(sum1+p.a>sum2+p.b){
                        count++;
                        sum1+=p.a;
                        sum2+=p.b;
                    }
                }
                max = Math.max(max,count);
            }

            out.println(max);

        }
        out.close();
    }

    static class Pair implements Comparable<Pair>{
        int a,b;
        Pair(int a, int b){
            this.a = a;
            this.b = b;
        }

        public int compareTo(Pair x){
            return Integer.compare(x.a-x.b,this.a-this.b);
        }
    }


    static final Random random=new Random();

    static void ruffleSort(char[] a) {
        int n=a.length;//shuffle, then sort
        for (int i=0; i<n; i++) {
            int oi=random.nextInt(n); char temp=a[oi];
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

        public int[] readArray(int n) {
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) arr[i] = nextInt();
            return arr;
        }
    }
}