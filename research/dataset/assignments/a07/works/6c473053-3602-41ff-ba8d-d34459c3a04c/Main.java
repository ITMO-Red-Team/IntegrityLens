import java.util.*;
import java.io.*;
public class Armchairs{
    public static void main(String[] args) {
        FastReader fr = new FastReader();
        PrintWriter out = new PrintWriter(System.out);
        Scanner sc= new Scanner (System.in);
        
        //Code From Here----
        int t =fr.nextInt();
        ArrayList <Integer> chairs= new ArrayList<>();
        ArrayList <Integer> free= new ArrayList<>();

        for (int i = 0; i < t; i++) {
            int state =fr.nextInt();
            if(state==1)
            {
                chairs.add(i);
            }
            else{
                free.add(i);
            }
        }
        int [][] dp=new int [t][t];
        for (int[] is : dp) {
            Arrays.fill(is, -1);
        }
        int ans=solve(chairs,free,0,0,chairs.size(),dp);
        out.println(ans);
        
    
        out.flush();
        sc.close();
    }

    //This RadixSort() is for long method

   

    private static int solve(ArrayList<Integer> chairs, ArrayList<Integer> free, int i, int j, int size,int [][] dp) {
        if (dp[i][j]!=-1) {
            return dp[i][j];
        }
        if (size==0) {
            return 0;
        }
        if (j==free.size()) {
            return 10000000;
        }
        int a=solve(chairs, free, i, j+1, size,dp);
        int b=Math.abs(chairs.get(i)-free.get(j))+solve(chairs, free, i+1, j+1, size-1,dp);
        dp[i][j]=Math.min(a, b);
        return dp[i][j];
    }

    public static long[] radixSort(long[] f){ return radixSort(f, f.length); }
	public static long[] radixSort(long[] f, int n)
	{
		long[] to = new long[n];
		{
			int[] b = new int[65537];
			for(int i = 0;i < n;i++)b[1+(int)(f[i]&0xffff)]++;
			for(int i = 1;i <= 65536;i++)b[i]+=b[i-1];
			for(int i = 0;i < n;i++)to[b[(int)(f[i]&0xffff)]++] = f[i];
			long[] d = f; f = to;to = d;
		}
		{
			int[] b = new int[65537];
			for(int i = 0;i < n;i++)b[1+(int)(f[i]>>>16&0xffff)]++;
			for(int i = 1;i <= 65536;i++)b[i]+=b[i-1];
			for(int i = 0;i < n;i++)to[b[(int)(f[i]>>>16&0xffff)]++] = f[i];
			long[] d = f; f = to;to = d;
		}
		{
			int[] b = new int[65537];
			for(int i = 0;i < n;i++)b[1+(int)(f[i]>>>32&0xffff)]++;
			for(int i = 1;i <= 65536;i++)b[i]+=b[i-1];
			for(int i = 0;i < n;i++)to[b[(int)(f[i]>>>32&0xffff)]++] = f[i];
			long[] d = f; f = to;to = d;
		}
		{
			int[] b = new int[65537];
			for(int i = 0;i < n;i++)b[1+(int)(f[i]>>>48&0xffff)]++;
			for(int i = 1;i <= 65536;i++)b[i]+=b[i-1];
			for(int i = 0;i < n;i++)to[b[(int)(f[i]>>>48&0xffff)]++] = f[i];
			long[] d = f; f = to; to = d;
		}
		return f;
	}

	

	public static int[] radixSort2(int[] a)
	{
		int n = a.length;
		int[] c0 = new int[0x101];
		int[] c1 = new int[0x101];
		int[] c2 = new int[0x101];
		int[] c3 = new int[0x101];
		for(int v : a) {
			c0[(v&0xff)+1]++;
			c1[(v>>>8&0xff)+1]++;
			c2[(v>>>16&0xff)+1]++;
			c3[(v>>>24^0x80)+1]++;
		}
		for(int i = 0;i < 0xff;i++) {
			c0[i+1] += c0[i];
			c1[i+1] += c1[i];
			c2[i+1] += c2[i];
			c3[i+1] += c3[i];
		}
		int[] t = new int[n];
		for(int v : a)t[c0[v&0xff]++] = v;
		for(int v : t)a[c1[v>>>8&0xff]++] = v;
		for(int v : a)t[c2[v>>>16&0xff]++] = v;
		for(int v : t)a[c3[v>>>24^0x80]++] = v;
		return a;
	}


	
    static int lowerBound(long a[], long x) { // x is the target value or key
        int l = -1, r = a.length;
        while (l + 1 < r) {
            int m = (l + r) >>> 1;
            if (a[m] >= x) r = m;
            else l = m;
        }
        return r;
    }

    static int upperBound(long a[], long x) {// x is the key or target value
        int l = -1, r = a.length;
        while (l + 1 < r) {
            int m = (l + r) >>> 1L;
            if (a[m] <= x) l = m;
            else r = m;
        }
        return l + 1;
    }

    static int upperBound(long[] arr, int start, int end, long k) {
        int N = end;
        while (start < end) {
            int mid = start + (end - start) / 2;
            if (k >= arr[mid]) {
                start = mid + 1;
            } else {
                end = mid;
            }
        }
        if (start < N && arr[start] <= k) {
            start++;
        }
        return start;
    }

    static long lowerBound(long[] arr, int start, int end, long k) {
        int N = end;
        while (start < end) {
            int mid = start + (end - start) / 2;
            if (k <= arr[mid]) {
                end = mid;
            } else {
                start = mid + 1;
            }
        }
        if (start < N && arr[start] < k) {
            start++;
        }
        return start;
    }


	// For Fast Input  ----
    
    
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

		double nextDouble()
		{
			return Double.parseDouble(next());
		}

		String nextLine()
		{
			String str = "";
			try {
				str = br.readLine();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return str;
		}
	}



    
}
