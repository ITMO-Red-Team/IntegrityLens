import java.io.*;
import java.util.*;

public class C {
	
	public static void main(String[] args)throws IOException {
		
		FastScanner scan = new FastScanner();
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(System.out));
		int t = scan.nextInt();
		for(int tt = 0;tt<t;tt++) {
			
			int n = scan.nextInt();
			ArrayList<String> arr = new ArrayList<>();
			for(int i = 0;i<n;i++) arr.add(scan.next());
			int max = -1;
			for(int cases = 0;cases<5;cases++) {
				ArrayList<Integer> list = new ArrayList<>();
				char ch = (char)('a'+cases);
				for(int i = 0;i<n;i++) {
					String s = arr.get(i);
					int countch = 0, countTotal = 0;
					for(int j = 0;j<s.length();j++) {
						if(s.charAt(j)==ch) countch++;
						else countTotal++;
					}
					list.add(countch-countTotal);
				}
				Collections.sort(list);
				int sum = 0, count = 0;
				for(int i = n-1;i>=0;i--) {
					sum+=list.get(i);
					if(sum>0) count++;
					else break;
				}
				max = Math.max(max, count);
			}
			output.write(max+"\n");
		}
		output.flush();

	}

	public static int[] sort(int arr[]) {

		List<Integer> list = new ArrayList<>();
		for(int i:arr)
			list.add(i);
		Collections.sort(list);
		for(int i = 0;i<list.size();i++) {
			arr[i] = list.get(i);
		}
		return arr;

	}
	
	public static int gcd(int a, int b) {
		if(a == 0) return b;
		return gcd(b%a, a);
	}
	
	static class FastScanner {
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st=new StringTokenizer("");
		String next() {
			while (!st.hasMoreTokens())
				try {
					st=new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			return st.nextToken();
		}

		int nextInt() {
			return Integer.parseInt(next());
		}
		int[] readArray(int n) {
			int[] a=new int[n];
			for (int i=0; i<n; i++) a[i]=nextInt();
			return a;
		}
		long nextLong() {
			return Long.parseLong(next());
		}
		
		double nextDouble() {
			return Double.parseDouble(next());
		}
	}

}
