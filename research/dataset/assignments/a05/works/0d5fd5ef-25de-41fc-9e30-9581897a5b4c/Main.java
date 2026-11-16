import java.util.*;
import java.io.*;
public class Transform_the_experssion  {
	static Scanner sc=new Scanner(System.in);
	static PrintWriter pw=new PrintWriter(System.out);
	static ArrayList<Integer> []arr;
	
	public static void main(String[] args) throws IOException, InterruptedException {
       int t=sc.nextInt();
       while(t-->0) {
           int n=sc.nextInt();
           String [] strings=new String [n];
           int [][]arr=new int [5][n];
           int [] total=new int [5];
           for(int i=0;i<n;i++)
        	   strings[i]=sc.next();
           for(int i=0;i<5;i++) {
        	   char x=(char)('a'+i);
        	   for(int j=0;j<n;j++) {
        		   int sum=0;
        		   int end=strings[j].length();
        		   for(int k=0;k<end;k++) {
        			   if(strings[j].charAt(k)==x) {
        				   sum++;
        			   }
        		   }
        		   arr[i][j]=sum-(end-sum);
        		   total[i]+=sum-(end-sum);
        	   }
           }
           boolean f=false;
           for(int i=0;i<5 && !f;i++) {
        	   if(total[i]>0) {
        		   pw.println(n);
        		   f=true;
        	   }
           }
           //pw.println(Arrays.toString(total));
           if(!f) {
        	   int [] MaxS=new int [5];
        	   for(int i=0;i<5;i++) {
        			Arrays.sort(arr[i]);
        			if(arr[i][n-1]<=0)   {
        				 MaxS[i]=0; 
        			}
        			else {
						int sum=arr[i][n-1],countWords=1;
						for(int j=n-2;j>-1 && sum+arr[i][j]>0;j--) {
							sum+=arr[i][j];
							countWords++;
						}
						 MaxS[i]=countWords;
					}
        	   }
        	   int ans=0;
        	   //pw.println(Arrays.toString(MaxS));
               for(int i=0;i<5;i++)
            	   ans=Math.max(ans, MaxS[i]);
               pw.println(ans);
           }
           
    }
    
    
    pw.flush();
	}
	/*public class UnionFind {                                              
		int[] p, rank, setSize;
		int numSets;

		public UnionFind(int N) 
		{
			p = new int[numSets = N];
			rank = new int[N];
			setSize = new int[N];
			for (int i = 0; i < N; i++) {  p[i] = i; setSize[i] = 1; }
		}

		public int findSet(int i) { return p[i] == i ? i : (p[i] = findSet(p[i])); }

		public boolean isSameSet(int i, int j) { return findSet(i) == findSet(j); }

		public void unionSet(int i, int j) 
		{ 
			if (isSameSet(i, j)) 
				return;
			numSets--; 
			int x = findSet(i), y = findSet(j);
			if(rank[x] > rank[y]) { p[y] = x; setSize[x] += setSize[y]; }
			else
			{	p[x] = y; setSize[y] += setSize[x];
				if(rank[x] == rank[y]) rank[y]++; 
			} 
		}

		public int numDisjointSets() { return numSets; }

		public int sizeOfSet(int i) { return setSize[findSet(i)]; }
	}
	static class pair implements Comparai)ble<pair> {
		long x;
		long y;
 
		public pair(long x, long y) {
			this.x = x;
			this.y = y;
		}
 
		public String toString() {
			return x + " " + y;
		}
 
		public boolean equals(Object o) {
			if (o instanceof pair) {
				pair p = (pair) o;
				return p.x == x && p.y == y;
			}
			return false;
		}
 
		public int hashCode() {
			return new Double(x).hashCode() * 31 + new Double(y).hashCode();
		}
 
		public int compareTo(pair other) {
			if (this.x == other.x) {
				return Long.compare(this.y, other.y);
			}
			return Long.compare(this.x, other.x);
		}
	}*/
	static class Scanner {
		StringTokenizer st;
		BufferedReader br;
 
		public Scanner(InputStream s) {
			br = new BufferedReader(new InputStreamReader(s));
		}
 
		public Scanner(FileReader r) {
			br = new BufferedReader(r);
		}
		public String readAllLines(BufferedReader reader) throws IOException {
		    StringBuilder content = new StringBuilder();
		    String line;
		    
		    while ((line = reader.readLine()) != null) {
		        content.append(line);
		        content.append(System.lineSeparator());
		    }

		    return content.toString();
		}
		public String next() throws IOException {
			while (st == null || !st.hasMoreTokens())
				st = new StringTokenizer(br.readLine());
			return st.nextToken();
		}
 
		public int nextInt() throws IOException {
			return Integer.parseInt(next());
		}
 
		public long nextLong() throws IOException {
			return Long.parseLong(next());
		}
 
		public String nextLine() throws IOException {
			return br.readLine();
		}
 
		public double nextDouble() throws IOException {
			String x = next();
			StringBuilder sb = new StringBuilder("0");
			double res = 0, f = 1;
			boolean dec = false, neg = false;
			int start = 0;
			if (x.charAt(0) == '-') {
				neg = true;
				start++;
			}
			for (int i = start; i < x.length(); i++)
				if (x.charAt(i) == '.') {
					res = Long.parseLong(sb.toString());
					sb = new StringBuilder("0");
					dec = true;
				} else {
					sb.append(x.charAt(i));
					if (dec)
						f *= 10;
				}
			res += Long.parseLong(sb.toString()) / f;
			return res * (neg ? -1 : 1);
		}
 
		public long[] nextlongArray(int n) throws IOException {
			long[] a = new long[n];
			for (int i = 0; i < n; i++)
				a[i] = nextLong();
			return a;
		}
 
		public Long[] nextLongArray(int n) throws IOException {
			Long[] a = new Long[n];
			for (int i = 0; i < n; i++)
				a[i] = nextLong();
			return a;
		}
 
		public int[] nextIntArray(int n) throws IOException {
			int[] a = new int[n];
			for (int i = 0; i < n; i++)
				a[i] = nextInt();
			return a;
		}
 
		public Integer[] nextIntegerArray(int n) throws IOException {
			Integer[] a = new Integer[n];
			for (int i = 0; i < n; i++)
				a[i] = nextInt();
			return a;
		}
 
		public boolean ready() throws IOException {
			return br.ready();
		}
 
	}
 
}