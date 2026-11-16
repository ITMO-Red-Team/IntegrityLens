import java.util.*;
import java.lang.*;
import java.io.*;

public class Main {
    static int func(List<Integer> ones, List<Integer> zeroes, int idO, int idZ, int o, int z, Integer[][] memo) {
        if(idZ == zeroes.size() || idO == ones.size()) {
            return 0;
        }
        
        if(memo[idO][idZ] != null) {
            return memo[idO][idZ];
        }
        
        if(o == z) {
            return memo[idO][idZ] = Math.abs(ones.get(idO) - zeroes.get(idZ))
                + func(ones, zeroes, idO + 1, idZ + 1, o - 1, z - 1, memo);
        } else {
            return memo[idO][idZ] = Math.min(
                Math.abs(ones.get(idO) - zeroes.get(idZ))
                + func(ones, zeroes, idO + 1, idZ + 1, o - 1, z - 1, memo),
                func(ones, zeroes, idO, idZ + 1, o, z - 1, memo)
            );
        }
    }
    
	public static void main (String[] args) {
	   // Scanner scan = new Scanner(System.in);
    	FastScanner scan = new FastScanner();
        int n =  scan.nextInt();
        int[] arr = scan.readArray(n);
        
        List<Integer> ones = new ArrayList<>();
        List<Integer> zeroes = new ArrayList<>();
        
        for(int i = 0; i < n; i++) {
            if(arr[i] == 0) {
                zeroes.add(i);
            } else {
                ones.add(i);
            }
        }
        
        int o = ones.size();
        int z = zeroes.size();
        
        Integer[][] memo = new Integer[o + 1][z + 1];
        
        int ans = func(ones, zeroes, 0, 0, o, z, memo);
        
        System.out.println(ans);
    }
/*
    int n = scan.nextInt();
            int[] arr = scan.readArray(n);
    
    int n = scan.nextInt();
            int m = scan.nextInt();
            int[][] arr = new int[n][m];
            for(int i = 0; i < n; i++) {
                for(int j = 0; j < m; j++) {
                    arr[i][j] = scan.nextInt();
                }
            }
    
*/
    
    
    static class FastScanner {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer("");
		String next() {
			while (!st.hasMoreTokens())
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			return st.nextToken();
		}
		
		int nextInt() {
			return Integer.parseInt(next());
		}
		int[] readArray(int n) {
			int[] a = new int[n];
			for (int i = 0; i < n; i++) a[i] = nextInt();
			return a;
		}
		long nextLong() {
			return Long.parseLong(next());
		}
	}
    
}