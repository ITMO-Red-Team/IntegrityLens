import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;


public class Solution {

	public static int INF= Integer.MAX_VALUE;
	public static long MOD= 1000000000+7L;

	public static int WHITE= 0;
	public static int GRAY= 1;
	public static int BLACK= 2;


	class Pair{
		public int day;
		int count;

		Pair(int d, int c){
			day=d; count=c;
		}
	}

	static class Solver {

		public void solve(InputReader in, PrintWriter out) {
			int tt=in.nextInt();
			//int tt=1;
			for(int cases=1;cases<=tt;cases++){
				int n= in.nextInt();
				long odd_min= Long.MAX_VALUE;
				long even_min= Long.MAX_VALUE;
				long ans= Long.MAX_VALUE;
				int odd_length=0;
				int even_length=0;

				long total=0;
				for(int segment=1;segment<=n;segment++){
					int cost= in.nextInt();
					total+=cost;
					if(segment%2==1){
						odd_min = Math.min(odd_min, cost);
						odd_length++;
					}
					else{
						even_min = Math.min(even_min, cost);
						even_length++;
					}

					if(segment>1){

						{

							long value=total-(odd_min+even_min);
							value+= odd_min * (n-odd_length+1);
							value+= even_min * (n-even_length+1);
							ans= Math.min(value, ans);
						}
					}
				}

				System.out.println(ans);
			}
		}


	} 

	public static void main(String[] args) {
		InputStream inputStream = System.in;
		OutputStream outputStream = System.out;
		InputReader in = new InputReader(inputStream);
		PrintWriter out = new PrintWriter(outputStream);
		Solver solver = new Solver();
		solver.solve(in, out);
		out.close();
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

	}
}