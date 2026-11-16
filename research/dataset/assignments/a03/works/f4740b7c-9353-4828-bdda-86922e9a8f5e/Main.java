import java.util.*;

public class Solution {
	
	private static Scanner in = new Scanner(System.in);
	
	public static void main(String[] args) {
		
		int t = in.nextInt();
		while(t-- > 0)
			solve();
    }
	
	private static void solve() {
		int n = in.nextInt();
		int k = in.nextInt();
		int a[] = new int[k+1];
		for(int i=1; i<=k; i++)
			a[i] = in.nextInt();
		long l[] = new long[n+1];
		long r[] = new long[n+1];
		for(int i=1; i<=n; i++) {
			l[i] = Integer.MAX_VALUE;
			r[i] = Integer.MAX_VALUE;
		}
		for(int i=1; i<=k; i++) {
			l[a[i]] = in.nextInt();
			r[a[i]] = l[a[i]];
		}
		for(int i=2;i<=n;i++) {
			l[i] = Math.min(l[i-1]+1, l[i]);
		}
		for(int i=n-1;i>=0;i--) {
			r[i] = Math.min(r[i+1]+1, r[i]);
		}
		
		for(int i=1;i<=n;i++) {
				System.out.print(Math.min(l[i], r[i])+" ");
		}
		System.out.println();
	}
}
