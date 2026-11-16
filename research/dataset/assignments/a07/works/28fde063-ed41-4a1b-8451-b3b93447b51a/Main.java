import java.io.*;
import java.util.*;
 
public class A{
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        Task solver = new Task();
        solver.solve(in, out);
        out.close();
    }
    // main solver
    static class Task{

        double eps= 0.00000001;
        static final int MAXN = 10000001; 
       
        // stores smallest prime factor for every number 
        static int spf[] = new int[MAXN]; 

        Map<Integer,Set<Integer>> dp= new HashMap<>();
        
        // Calculating SPF (Smallest Prime Factor) for every 
        // number till MAXN. 
        // Time Complexity : O(nloglogn) 
        public void sieve() 
        { 
            spf[1] = 1; 
            for (int i=2; i<MAXN; i++) 
        
                // marking smallest prime factor for every 
                // number to be itself. 
                spf[i] = i; 
        
            // separately marking spf for every even 
            // number as 2 
            for (int i=4; i<MAXN; i+=2) 
                spf[i] = 2; 
        
            for (int i=3; i*i<MAXN; i++) 
            { 
                // checking if i is prime 
                if (spf[i] == i) 
                { 
                    // marking SPF for all numbers divisible by i 
                    for (int j=i*i; j<MAXN; j+=i) 
        
                        // marking spf[j] if it is not  
                        // previously marked 
                        if (spf[j]==j) 
                            spf[j] = i; 
                } 
            } 
        } 
        
        // A O(log n) function returning primefactorization 
        // by dividing by smallest prime factor at every step 
        public Set<Integer> getFactorization(int x) 
        { 
            if(dp.containsKey(x)) return dp.get(x);
            Set<Integer> ret = new HashSet<>(); 
            while (x != 1) 
            { 
                if(spf[x]!=2) ret.add(spf[x]); 
                x = x / spf[x]; 
            } 
            dp.put(x,ret);
            return ret; 
        } 
        // function to find first index >= x 
        public int lowerIndex(List<Integer> arr, int n, int x) 
        { 
            int l = 0, h = n - 1; 
            while (l <= h)  
            { 
                int mid = (l + h) / 2; 
                if (arr.get(mid) >= x) 
                    h = mid - 1; 
                else
                    l = mid + 1; 
            } 
            return l; 
        } 
        
        // function to find last index <= y 
        public int upperIndex(List<Integer> arr, int n, int y) 
        { 
            int l = 0, h = n - 1; 
            while (l <= h)  
            { 
                int mid = (l + h) / 2; 
                if (arr.get(mid) <= y) 
                    l = mid + 1; 
                else
                    h = mid - 1; 
            } 
            return h; 
        } 
        
        // function to count elements within given range 
        public int countInRange(List<Integer> arr, int n, int x, int y) 
        { 
            // initialize result 
            int count = 0; 
            count = upperIndex(arr, n, y) -  
                    lowerIndex(arr, n, x) + 1; 
            return count; 
        }
        ArrayList<Integer>A = new ArrayList<>();
        ArrayList<Integer>B = new ArrayList<>();
        ArrayList<List<Integer>> L = new ArrayList<List<Integer>>();
        public void solve(InputReader in, PrintWriter out) {           
            int n= in.nextInt();
            int[] A= new int[n];
            final int INF= 1000000000;
            List<Integer> pos= new ArrayList<>();
            for (int i = 0; i < n; i++){
                A[i]= in.nextInt();
                if (A[i] == 1) pos.add(i);
            }
            int cnt = pos.size();
            int[][] dp= new int[cnt][n+1];
            for(int[] temp: dp) Arrays.fill(temp,INF);
            if (cnt == 0){
                out.println(0);
                return;
            }
            for (int i = n - 1; i >= 0; i--){
                dp[cnt - 1][i] = dp[cnt - 1][i + 1];
                if (A[i] != 1){
                    dp[cnt - 1][i] = Math.min(dp[cnt - 1][i], Math.abs(i - pos.get(pos.size()-1)));
                }
            }
            for (int i = cnt - 2; i >= 0; i--){
                for (int j = n - 1; j >= 0; j--){
                    dp[i][j] = dp[i][j + 1];
                    if (A[j] != 1){
                        dp[i][j] = Math.min(dp[i][j], Math.abs(j - pos.get(i)) + dp[i + 1][j + 1]);
                    }
                }
            }
            out.println(dp[0][0]);
        }
        void backtrack(PrintWriter out, int i, List<Integer> considered, int used, int sum, int cur, int g){
            if (used == considered.size()){
                int percentage = (cur * 100) / sum;
                if (percentage >= g){
                    considered.add(percentage);
                    L.add(considered);
                }
                return;
            }
            if (i == B.size()){
                return;
            }
            for (int j = i; j < B.size(); j++){
                List<Integer> au = new ArrayList<>(considered);
                au.add(j + 1);
                backtrack(out, j + 1, au, used, sum, cur + B.get(j), g);
            }
        }
        public int _gcd(int a,  int  b)
        {
        
            if(b == 0) {
                    return a;
            }
            else {
                return _gcd(b, a % b);
            }
        }

    }

    static class Tuple implements Comparable<Tuple>{
        int x, y, z;
        public Tuple(int x, int y, int z){
            this.x= x;
            this.y= y;
            this.z=z;
        }
        @Override
        public int compareTo(Tuple o){
            return this.x-o.x;
        }
    }

    static class Pair implements Comparable<Pair>{
        public int x;
        public int y;
        public Pair(int x, int y){
            this.x= x;
            this.y= y;
        }
 
        @Override
        public int compareTo(Pair o) {
            return this.x-o.x;
        }
    }
    // fast input reader class;
    static class InputReader {
        BufferedReader br;
        StringTokenizer st;
 
        public InputReader(InputStream stream) {
            br = new BufferedReader(new InputStreamReader(stream));
        }
 
        public String nextToken() {
            while (st == null || !st.hasMoreTokens()) {
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (line == null) {
                    return null;
                }
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }
 
        public int nextInt() {
            return Integer.parseInt(nextToken());
        }
        public double nextDouble(){
            return Double.parseDouble(nextToken());
        }
        public long nextLong(){
            return Long.parseLong(nextToken());
        }
    }
}
 