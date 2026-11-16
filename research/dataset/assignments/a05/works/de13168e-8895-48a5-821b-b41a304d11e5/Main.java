// Piyush Nagpal
import java.util.*;
import java.io.*;
 public class C{
     
    static int MOD=1000000007;
    static PrintWriter pw;
    static FastReader sc;
    
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
 
        public FastReader()
        {
            br = new BufferedReader(
                new InputStreamReader(System.in));
        }
 
        String next()
        {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }
 
        int nextInt() { return Integer.parseInt(next()); }
 
        long nextLong() { return Long.parseLong(next()); }
 
        double nextDouble(){return Double.parseDouble(next());}
        
        public char nextChar() throws IOException {return next().charAt(0);}
 
        String nextLine(){
            String str = "";
            try {
                str = br.readLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
        public int[] intArr(int n) throws IOException {int[]in=new int[n];for(int i=0;i<n;i++)in[i]=nextInt();return in;}
        public long[] longArr(int n) throws IOException {long[]in=new long[n];for(int i=0;i<n;i++)in[i]=nextLong();return in;}
        
    }   


     public static long gcd(long a,long b){if( b>a ){return gcd(b,a);}if( b==0 ){return a;} return gcd(b,a%b);}
     public static long expo( long a,long b,long M ){long result=1;while(b>0){if( b%2==1 ){result=(result*a)%M;}a=(a*a)%M;b=b/2;}return result%M;}
    public static ArrayList<Long> Sieve(int n){boolean [] arr= new boolean[n+1];Arrays.fill(arr, true);ArrayList<Long> list= new ArrayList<>();for(int i=2;i<=n;i++){if( arr[i]==true ){list.add((long)i);}for(int j=2*i;j<=n;j+=i){arr[j]=false;}}return list;}

    public static void printarr(int [] arr){for(int i=0;i<arr.length;i++){pw.print(arr[i]+" ");}}
    public static void printarr(long [] arr){for(int i=0;i<arr.length;i++){pw.print(arr[i]+" ");}}
    
    // int [] arr=sc.intArr(n);
    static int Max=2*100005;

    // static int [][] arr= new int [Max][5];
    // static int [] tot= new int [Max];
    static ArrayList<ArrayList<Integer>> arr= new ArrayList<>();
    static ArrayList<Integer> tot= new ArrayList<>();
    static int fun(int ch , int n){
    	ArrayList<Integer> list= new ArrayList<>();
    	for(int i=0;i<n;i++){
    		list.add( (arr.get(i).get(ch))-(tot.get(i)-(arr.get(i).get(ch))) );
    	}
    	Collections.sort(list,Collections.reverseOrder());
    	int count=0,sum=0;
    	for(Integer x: list){
    		sum+=x;
    		if(sum>0){
    			count++;
    		}
    	}
    	return count;
    }

    static void solve() throws Exception{
    	int n=sc.nextInt();
    	arr= new ArrayList<>();
    	tot= new ArrayList<>();
    	for(int i=0;i<n;i++){
            arr.add( new ArrayList<>());
    		tot.add(0);
    		for(int j=0;j<5;j++){
    			ArrayList<Integer> list=arr.get(i);
    			list.add(0);
    		}
    	}
    	// pw.println(tot);
    	// pw.println(arr);
    	// return;
    	for(int i=0;i<n;i++){
    		String s=sc.next();
    		tot.set(i,s.length());
    		for(int j=0;j<s.length();j++){
    			ArrayList<Integer> list=arr.get(i);
    			list.set((s.charAt(j)-'a'),list.get((s.charAt(j)-'a'))+1);
    			// list.set((s.charAt(j)-'a'),1);
    		}
    	}
    	int ans =0;
    	for(int i=0;i<5;i++){
    		ans = Math.max(ans,fun(i,n));
    	}
    	pw.println(ans);
 }
    
   public static void main(String[] args) throws Exception{
        try {
            System.setIn(new FileInputStream("input.txt"));
            System.setOut(new PrintStream(new FileOutputStream("output.txt")));
        } catch (Exception e) {
            System.err.println("Error");
        }
        sc= new FastReader();
        pw = new PrintWriter(System.out);

   
        int tc=1;
        tc=sc.nextInt();
        for(int i=1;i<=tc;i++) {
//            pw.printf("Case #%d: "b, i);
            solve();
        }
       
        pw.flush();

    

    }

}