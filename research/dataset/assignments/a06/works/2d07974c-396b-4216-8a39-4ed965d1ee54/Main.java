/*input
3
2
13 88
3
2 3 1
5
4 3 2 1 4

*/
import java.util.*;
import java.lang.*;
import java.io.*;

public class Main
{
	static PrintWriter out;
	static int MOD = 1000000007;
	static FastReader scan;

    /*-------- I/O using short named function ---------*/
	public static String ns(){return scan.next();}
	public static int ni(){return scan.nextInt();}
	public static long nl(){return scan.nextLong();}
	public static double nd(){return scan.nextDouble();}
	public static String nln(){return scan.nextLine();}
	public static void p(Object o){out.print(o);}
	public static void ps(Object o){out.print(o + " ");}
	public static void pn(Object o){out.println(o);}
    /*-------- for output of an array ---------------------*/
     static void iPA(int arr []){
     	 StringBuilder output = new StringBuilder();
	     for(int i=0; i<arr.length; i++)output.append(arr[i] + " ");out.println(output);
	}
	static void lPA(long arr []){
	     StringBuilder output = new StringBuilder();
	     for(int i=0; i<arr.length; i++)output.append(arr[i] + " ");out.println(output);
	}
	static void sPA(String arr []){
	     StringBuilder output = new StringBuilder();
	     for(int i=0; i<arr.length; i++)output.append(arr[i] + " ");out.println(output);
	}
	static void dPA(double arr []){
	     StringBuilder output = new StringBuilder();
	     for(int i=0; i<arr.length; i++)output.append(arr[i] + " ");out.println(output);
	}
    /*-------------- for input in an array ---------------------*/
	static void iIA(int arr[]){
	     for(int i=0; i<arr.length; i++)arr[i] = ni();
	}
	static void lIA(long arr[]){
	     for(int i=0; i<arr.length; i++)arr[i] = nl(); 
	}
	static void sIA(String arr[]){
	     for(int i=0; i<arr.length; i++)arr[i] = ns(); 
	}
	static void dIA(double arr[]){
	     for(int i=0; i<arr.length; i++)arr[i] = nd();
	}
	/*------------ for taking input faster ----------------*/
     static class FastReader
	{
		BufferedReader br;
		StringTokenizer st;

		public FastReader()
		{
			br = new BufferedReader(new InputStreamReader(System.in));
		}

		String next()
		{
			while (st == null || !st.hasMoreElements())
			{
				try
				{
					st = new StringTokenizer(br.readLine());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}

		int nextInt()
		{
			return Integer.parseInt(next());
		}

		long nextLong()
		{
			return Long.parseLong(next());
		}

		double nextDouble()
		{
			return Double.parseDouble(next());
		}

		String nextLine()
		{
			String str = "";
			try
			{
				str = br.readLine();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return str;
		}
	}

	// Method to check if x is power of 2
    static boolean isPowerOfTwo (int x) { return x!=0 && ((x&(x-1)) == 0);}
    //Method to return lcm of two numbers
    static int gcd(int a, int b){return a==0?b:gcd(b % a, a); }
    //Method to count digit of a number
    static int countDigit(long n){return (int)Math.floor(Math.log10(n) + 1);}

    //Method for sorting
	static void ruffle_sort(int[] a) {
		//shandom_ruffle
		Random r=new Random();
		int n=a.length;
		for (int i=0; i<n; i++) {
			int oi=r.nextInt(n);
			int temp=a[i];
			a[i]=a[oi];
			a[oi]=temp;
		}
		//sort
		Arrays.sort(a);
	}
	//Method for checking if a number is prime or not
	static boolean isPrime(int n) { 
        if (n <= 1) return false; 
        if (n <= 3) return true; 
        if (n % 2 == 0 || n % 3 == 0) return false; 
        for (int i = 5; i * i <= n; i = i + 6) 
            if (n % i == 0 || n % (i + 2) == 0) 
            return false; 
        return true; 
    } 

	public static void main (String[] args) throws java.lang.Exception
	{
		OutputStream outputStream =System.out;
        out =new PrintWriter(outputStream);
        scan =new FastReader();
		//for fast output sometimes
		StringBuilder sb = new StringBuilder();
		int t = ni();
		while(t-->0){
			int n = ni();
			long arr[] = new long[n];
			lIA(arr);
			long ans = (long)(n*(arr[0] + arr[1]));
			long sum = arr[0] + arr[1];
			long emin = arr[0], omin = arr[1];
			
			for(int i=2; i<n; i++){
				sum += arr[i];
				if(i%2==0){
					emin = Math.min(arr[i], emin);
				}
				else{
					omin = Math.min(arr[i], omin);
				}
				long temp = sum - emin - omin;
				if(i%2==0)
					temp += (n-i/2)*emin + (n-i/2+1)*omin;
				else
					temp += (n-(i-1)/2)*(emin + omin);
				ans = Math.min(ans, temp);

			}
			pn(ans);
        }
        out.flush();
		out.close();
	}
	
}

