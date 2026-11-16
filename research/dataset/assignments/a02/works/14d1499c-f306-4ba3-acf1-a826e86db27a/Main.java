import java.io.*;
import java.util.*;
 
public class Contest1615C
{
    static class InputReader {
        BufferedReader reader;
        StringTokenizer tokenizer;
        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }
        String next() { // reads in the next string
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }
        public int nextInt() { // reads in the next int
            return Integer.parseInt(next());
        }
        public long nextLong() { // reads in the next long
            return Long.parseLong(next());
        }
        public double nextDouble() { // reads in the next double
            return Double.parseDouble(next());
            }
        }
    static InputReader r = new InputReader(System.in);
    static PrintWriter pw = new PrintWriter(System.out);
    static long mod = 1000000007;
    public static void main(String[] args)
    {
    	int t = r.nextInt();
    	
    	while (t > 0)
    	{
    		t--;
    		int n = r.nextInt();
    		String a = r.next();
    		String b = r.next();
    		int sum1 = 0; int sum2 = 0;
    		for (int i = 0; i < n; i ++)
    		{
    			sum1 += (a.charAt(i) == '1'?1:0);
    			sum2 += (b.charAt(i) == '1'?1:0);
    		}
    		if (sum1!=sum2 && sum1+sum2 != n+1)
    		{
    			pw.println(-1);
    			continue;
    		}
    		int[][] count = new int[2][2];
    		for (int i = 0; i < n; i ++)
    		{
    			count[(int)a.charAt(i)-(int)'0'][(int)b.charAt(i)-(int)'0']++;
    		}
    		int min = 10000000;
    		if (count[0][1] == count[1][0])
    		{
    			min = Math.min(min, count[0][1]*2);
    		}
    		if (count[1][1] == count[0][0] + 1)
    		{
    			min = Math.min(min, count[1][1] + count[0][0]);
    		}
    		pw.println(min == 10000000 ? -1:min);
    	}
        pw.close();
    }
}