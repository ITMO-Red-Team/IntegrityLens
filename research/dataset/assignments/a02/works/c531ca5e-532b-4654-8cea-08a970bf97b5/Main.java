import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Menorah {
	
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
        
        double nextFloat() { return Float.parseFloat(next()); }
 
        double nextDouble() { return Double.parseDouble(next()); }
 
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
        
        int[] nextArray(int n)
        {
        	int[] a = new int[n];
        	for (int i=0; i<n; i++) {
        		a[i] = this.nextInt();
        	}
        	return a;
        }
        
        long[] nextArrayLong(int n)
        {
        	long[] a = new long[n];
        	for (int i=0; i<n; i++) {
        		a[i] = this.nextLong();
        	}
        	return a;
        }
        
    }
	
	public static void solve(int n, String a, String b) {
		
		int bothOne = 0;
		int bothZero = 0;
		int oneZero = 0;
		int zeroOne = 0;
		for (int i=0; i<n; i++) {
			if (a.charAt(i) == '0') {
				if (b.charAt(i) == '0') {
					bothZero++;
				}
				else {
					zeroOne++;
				}
			}
			else {
				if (b.charAt(i) == '0') {
					oneZero++;
				}
				else {
					bothOne++;
				}
			}
		}
		int ans = Integer.MAX_VALUE;
		if (bothOne - bothZero == 1) {
			ans = (bothZero*2) + 1;
		}
		if (oneZero == zeroOne) {
			ans = Math.min(ans, oneZero*2);
		}
		if (ans == Integer.MAX_VALUE) {
			ans = -1;
		}
		System.out.println(ans);
		
	}

	public static void main(String[] args) {
		
		FastReader in = new FastReader();
		int t = in.nextInt();
		while (t-- > 0) {
			solve(in.nextInt(), in.next(), in.next());
		}

	}

}
