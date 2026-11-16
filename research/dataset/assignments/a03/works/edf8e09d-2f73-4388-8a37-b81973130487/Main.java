import java.io.*;
import java.util.*;
 
public class Codeforces {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int cases = Integer.parseInt(br.readLine());
		while(cases-- > 0) {
			br.readLine();
			String[] str = br.readLine().split(" ");
			int n = Integer.parseInt(str[0]);
			int k = Integer.parseInt(str[1]);
			int[] a = new int[k];
			int[] t = new int[k];
			str = br.readLine().split(" ");
			for(int i=0; i<k; i++) {
				a[i] = Integer.parseInt(str[i]) - 1;
			}
			str = br.readLine().split(" ");
			for(int i=0; i<k; i++) {
				t[i] = Integer.parseInt(str[i]);
			}
			int[] temp = new int[n];
			Arrays.fill(temp, Integer.MAX_VALUE);
			int[] left = new int[n];
			int[] right = new int[n];
			Arrays.fill(left, Integer.MAX_VALUE);
			Arrays.fill(right, Integer.MAX_VALUE);
			int ind = 0;
			for(int i=0; i<k; i++) {
				left[a[i]] = t[i];
				right[a[i]] = t[i];
			}
			int minleft = Integer.MAX_VALUE;
			for(int i=0; i<n; i++) {
				left[i] = Math.min(left[i], minleft);
				minleft = left[i] == Integer.MAX_VALUE ? Integer.MAX_VALUE : left[i]+1;
			}
			int minright = Integer.MAX_VALUE;
			for(int i=n-1; i>=0; i--) {
				right[i] = Math.min(right[i], minright);
				minright = right[i] == Integer.MAX_VALUE ? Integer.MAX_VALUE : right[i]+1;
			}
			for(int i=0; i<n; i++) {
				temp[i] = Math.min(right[i], left[i]);
				System.out.print(temp[i]+" ");
			}
			System.out.println();
		}
	}
}