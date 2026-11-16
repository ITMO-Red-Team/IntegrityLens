
import java.util.*;
import java.lang.*;
import java.io.*;

public class E {

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int t = Integer.parseInt(br.readLine());
		br.readLine();
		StringBuilder sb = new StringBuilder();
		while (t-- > 0) {
			String[] scn = (br.readLine()).trim().split(" ");
			int n = Integer.parseInt(scn[0]);
			int m = Integer.parseInt(scn[1]);
			long[] land = new long[n + 1];
			scn = (br.readLine()).trim().split(" ");
			String[] scn1 = (br.readLine()).trim().split(" ");
			for (int i = 0; i < m; i++) {
				int idx = (int) Long.parseLong(scn[i]);
				long temp = Long.parseLong(scn1[i]);
				land[idx] = temp;
			}
			long[] ans = new long[n + 1];
			long min = (long) (1e15);
			for (int i = 1; i <= n; i++) {
				if (land[i] != 0) {
					min = Math.min(min, land[i]);
				}
				ans[i] = min;
				min += 1;
			}
			min = (long) (1e15);
			for (int i = n; i >= 1; i--) {
				if (land[i] != 0) {
					min = Math.min(min, land[i]);
				}
				ans[i] = Math.min(min, ans[i]);
				min += 1;
			}
			for (int i = 1; i <= n; i++) {
				sb.append(ans[i] + " ");
			}
			sb.append("\n");
			if (t != 0) {
				br.readLine();
			}
		}
		System.out.println(sb);
		return;

	}

	public static void sort(long[] arr) {
		int n = arr.length;
		for (int i = 0; i < n; i++) {
			int idx = (int) (Math.random() * n);
			long temp = arr[i];
			arr[i] = arr[idx];
			arr[idx] = temp;
		}
		Arrays.sort(arr);
	}

	public static void sort(int[] arr) {
		int n = arr.length;
		for (int i = 0; i < n; i++) {
			int idx = (int) (Math.random() * n);
			int temp = arr[i];
			arr[i] = arr[idx];
			arr[idx] = temp;
		}
		Arrays.sort(arr);
	}

	public static void print(long[][] dp) {
		for (long[] a : dp) {
			for (long ele : a) {
				System.out.print(ele + " ");
			}
			System.out.println();
		}
	}

	public static void print(int[][] dp) {
		for (int[] a : dp) {
			for (int ele : a) {
				System.out.print(ele + " ");
			}
			System.out.println();
		}
	}

	public static void print(int[] dp) {
		for (int ele : dp) {
			System.out.print(ele + " ");
		}
		System.out.println();
	}

	public static void print(long[] dp) {
		for (long ele : dp) {
			System.out.print(ele + " ");
		}
		System.out.println();
	}

}

