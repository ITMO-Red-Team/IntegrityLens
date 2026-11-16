import java.io.*;
import java.util.*;
 
public class Main {
 
  static class Task {
 
    int NN = 200005;
    int MOD = 1000000007;
    int INF = 2000000000;
    long INFINITY = 2000000000000000000L;

	int [] a;
	int [] b;
	List<Integer> [] g;
	long [][] dp;

	long rec(int node, int prev, int index) {
		if(dp[node][index] != -1){
			return dp[node][index];
		}
		long ret = 0;
		int val = index==0?a[node]:b[node];
		for(int adj: g[node]) {
			if(adj == prev) {
				continue;
			}
			ret += Math.max((long)(rec(adj, node, 0) + (long)(Math.abs(val - a[adj]))), (long)(rec(adj, node, 1) + (long)(Math.abs(val - b[adj]))));
		}
		return dp[node][index] = ret;
	}
    
    public void solve(InputReader in, PrintWriter out) {
		int t = in.nextInt();
		while(t-->0) {
			int n = in.nextInt();
			a = new int[n + 1];
			b = new int[n + 1];
			g = new ArrayList[n + 1];
			dp = new long[n + 1][2];
			for(int i=1;i<=n;++i) {
				a[i] = in.nextInt();
				b[i] = in.nextInt();
				g[i] = new ArrayList<>();
				dp[i][0] = dp[i][1] = -1;
			}
			for(int i=1;i<n;++i) {
				int u = in.nextInt();
				int v = in.nextInt();
				g[u].add(v);
				g[v].add(u);
			}
			long ans = Math.max(rec(1, -1, 0), rec(1, -1, 1));
			out.println(ans);
		}
    }
  }
 
  static void prepareIO(boolean isFileIO) {
    // long t1 = System.currentTimeMillis();
    Task solver = new Task();
    // Standard IO
    if (!isFileIO) {
      InputStream inputStream = System.in;
      OutputStream outputStream = System.out;
      InputReader in = new InputReader(inputStream);
      PrintWriter out = new PrintWriter(outputStream);
      solver.solve(in, out);
      // out.println("time(s): " + (1.0*(System.currentTimeMillis()-t1))/1000.0);
      out.close();
    }
    // File IO
    else {
      String IPfilePath = System.getProperty("user.home") + "/Downloads/ip.in";
      String OPfilePath = System.getProperty("user.home") + "/Downloads/op.out";
      InputReader fin = new InputReader(IPfilePath);
      PrintWriter fout = null;
      try {
        fout = new PrintWriter(new File(OPfilePath));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      solver.solve(fin, fout);
      // fout.println("time(s): " + (1.0*(System.currentTimeMillis()-t1))/1000.0);
      fout.close();
    }
  }
 
  public static void main(String[] args) {
    prepareIO(false);
  }
 
  static class InputReader {
    public BufferedReader reader;
    public StringTokenizer tokenizer;
 
    public InputReader(InputStream stream) {
      reader = new BufferedReader(new InputStreamReader(stream), 32768);
      tokenizer = null;
    }
 
    public InputReader(String filePath) {
      File file = new File(filePath);
      try {
        reader = new BufferedReader(new FileReader(file));
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      tokenizer = null;
    }
 
    public String nextLine() {
      String str = "";
      try {
        str = reader.readLine();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return str;
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
  }
}