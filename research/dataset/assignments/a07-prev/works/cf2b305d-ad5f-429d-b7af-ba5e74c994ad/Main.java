    import java.io.*;
    import java.util.*;
    import java.lang.Math;


    public class Main {

          static PrintWriter pw;
          static Scanner sc;
          static StringBuilder ans;
          static long mod = 1000000000+7;

          static void pn(final Object arg) {
               pw.print(arg);          
               pw.flush();      
          }     

          /*-------------- for input in an value ---------------------*/
          static int ni()     { return sc.nextInt();       }
          static long nl()    { return sc.nextLong();      }
          static double nd()  { return sc.nextDouble();    }
          static String ns()  { return sc.next();          }


          static void ap(int arg)           { ans.append(arg); }
          static void ap(long arg)          { ans.append(arg); }
          static void ap(String arg)        { ans.append(arg); }
          static void ap(StringBuilder arg) { ans.append(arg); }        


          /*-------------- for input in an array ---------------------*/
          static void inputIntegerArray(int arr[]){
               for(int i=0; i<arr.length; i++)arr[i] = ni();
          }
          static void inputLongArray(long arr[]){
               for(int i=0; i<arr.length; i++)arr[i] = nl(); 
          }
          static void inputStringArray(String arr[]){
               for(int i=0; i<arr.length; i++)arr[i] = ns(); 
          }    
          static void inputDoubleArray(double arr[]){
               for(int i=0; i<arr.length; i++)arr[i] = nd();
          }


          /*-------------- File vs Input ---------------------*/
          static void runFile() throws Exception {
               sc = new Scanner(new FileReader("input.txt"));
               pw = new PrintWriter(new BufferedWriter(new FileWriter("output.txt")));
          }
          static void runIo() throws Exception  {
               pw =new PrintWriter(System.out);
               sc = new Scanner(System.in);
          }    

          static void swap(int a, int b) {
               int  t = a;
               a = b;
               b = t;
          }

          static boolean isPowerOfTwo (long x) { return x!=0 && ((x&(x-1)) == 0);}

          static int gcd(int a, int b) { if (b == 0) return a;     return gcd(b, a % b); }
          
          static int countDigit(long n){return (int)Math.floor(Math.log10(n) + 1);} 
          
          static boolean isPrime(int n) { 
               if (n <= 1) return false; 
               if (n <= 3) return true; 
               if (n % 2 == 0 || n % 3 == 0) return false; 

               for (int i = 5; i * i <= n; i = i + 6) 
               if (n % i == 0 || n % (i + 2) == 0) 
                    return false; 

               return true; 
          }         

          static boolean sv[] = new boolean[1000002];
          static void seive() {
                 //true -> not prime
               // false->prime

               sv[0] = sv[1] = true; 
               sv[2] = false;
               for(int i = 0; i< sv.length; i++) {
                    if( !sv[i] && (long)i*(long)i < sv.length ) {
                         for ( int j = i*i; j<sv.length ; j += i ) {                
                          sv[j] = true;
                         }
                    }
               }

          }

          static long  binpow(  long a,   long b) {
               long res = 1;
               while (b > 0) {
                    if ( (b & 1) > 0){
                    res = (res * a)%mod;
               }
               a = (a * a)%mod;
               b >>= 1;
               }

               return res;
          }

          static long factorial(long n)  {
               long res = 1, i;
               for (i = 2; i <= n; i++){
                    res  =  ((res%mod) * (i%mod))%mod;
               }          
               return res;
          }
  

          static class Pair {
               int idx;
               int v;
               Pair(int idx, int v){
                    this.idx = idx;
                    this.v = v;
               }
          }
     
          public static void main(String[] args)  throws Exception {

               // runFile();

               runIo();              

               int t;
               t = 1;     
            
               // t =  sc.nextInt();
          
               ans = new StringBuilder();
          
               while( t-- > 0 ) {   
                    solve();
               }                
          
               pn(ans+""); 
 
          }
    
          static int N ; 
          static int M ; 
          static ArrayList<Integer> f;
          static ArrayList<Integer> e;
          static long dp[][];

          static long find(int i, int j ) {
               
               if( i == N )   return 0;
               if( j == M )   return Integer.MAX_VALUE;


               if (dp[i][j] != -1 )
                    return dp[i][j];

               return dp[i][j] = Math.min( find(i, j+1), Math.abs(f.get(i)-e.get(j)) +  find(i+1, j+1) );
          }       


          public static void solve() {          
          
               int n = ni();
               f = new ArrayList();
               e = new ArrayList();               

               for(int i = 0; i<n; i++) {
                    int v = ni();
                    if( v == 0 ) {
                         e.add(i);
                    }
                    else
                         f.add(i);
               }

               N = f.size();
               M = e.size();               

               dp = new long[N][M];

               for(int i = 0; i<N; i++)
                    Arrays.fill(dp[i], -1);

               ap(find(0, 0)+"\n");
          }
 
     }
  

   // 0 1 2 3 4 5 6 7
   // 1 1 0 0 0 1 0 0

   // 1s -> { 0, 1, 5 }
   // 0s -> { 2, 3, 6, 7 }


