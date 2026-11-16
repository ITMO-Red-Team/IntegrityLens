import java.util.*;
import java.io.*;

public class monstersandspells {
	public static void main(String args[]) throws IOException {
		FastScanner in = new FastScanner(System.in);
		PrintWriter out = new PrintWriter(System.out);
		/*
1
5
1 5 8 9 10
1 2 6 1 2
		 */
		int t = in.nextInt();
		
		for ( ; t > 0; t--) {
			
			int n = in.nextInt();
			
			long[] times = new long[n];
			long[] health = new long[n];
			for (int i = 0; i < n; i++)
				times[i] = in.nextLong();
			for (int i = 0; i < n; i++)
				health[i] = in.nextLong();
			
			long small = 0;
			long prev = 0;
			long lastDamage = 0;
			
			for (int i = 0; i < n; i++) {
				long diff = times[i] - prev;
				boolean use = true;
				long delta = 0;
				//System.out.println(i + " " + times[i] + " " + diff);
				if (diff >= health[i]) {
					delta += health[i];
					long curr = health[i];
					long lastTime = times[i];
					for (int j = i + 1; j < n; j++) {
						long increase = times[j] - lastTime;
						//long power = damageOverTime(increase + curr, curr + 1);
						if (curr + increase >= health[j]) {
							curr += increase;
							lastTime = times[j];
						}
						else {
							delta += (health[j] - (curr + increase));
							curr = health[j];
							lastTime = times[j];
						}
					}
					if (delta <= diff) {
						use = true;
					}
					else
						use = false;
				}
				else
					use = false;
				
				if (use) {
					//System.out.println(damageOverTime(health[i], 1));
					small += damageOverTime(delta, 1);
					lastDamage = delta;
				}
				else {
					small += damageOverTime((times[i] - prev) + lastDamage, lastDamage + 1);
					lastDamage+=(times[i] - prev);
				}
				// System.out.println(use + " " + delta + " " + lastDamage + " " + small);
				prev = times[i];
			}
			
			out.println(small);
		}
		
		out.close();
	}
	
	public static long damageOverTime(long endPower, long startPower) {
		return triangleSum(endPower) - triangleSum(startPower - 1);
	}
	
	public static long triangleSum(long a) {
		return a * (a + 1) / 2;
	}
	
	static class FastScanner {
		BufferedReader br;
		StringTokenizer st;

		public FastScanner(InputStream i) {
			br = new BufferedReader(new InputStreamReader(i));
			st = new StringTokenizer("");
		}

		public String next() throws IOException {
			if (st.hasMoreTokens())
				return st.nextToken();
			else
				st = new StringTokenizer(br.readLine());
			return next();
		}

		public int nextInt() throws IOException {
			return Integer.parseInt(next());
		}

		public long nextLong() throws IOException {
			return Long.parseLong(next());
		}

		public double nextDouble() throws IOException {
			return Double.parseDouble(next());
		}
	}
}
