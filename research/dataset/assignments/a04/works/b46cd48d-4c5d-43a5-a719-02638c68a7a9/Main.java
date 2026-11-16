

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class test {

	public static void main(String[] args) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		int t = Integer.parseInt(br.readLine());
		StringBuilder sb = new StringBuilder();
		while (t-- > 0) {
			String[] st = br.readLine().split(" ");
			int n = Integer.parseInt(st[0]);
			int m = Integer.parseInt(st[1]);
			int x = Integer.parseInt(st[2]);

			int[] arr = new int[n];
			st = br.readLine().split(" ");
			Pair[] temp = new Pair[n];
			for (int i = 0; i < n; i++) {
				arr[i] = Integer.parseInt(st[i]);
				temp[i] = new Pair(arr[i], i);
			}

			int bcount = n / m;
			int extra = n % m;

			int[] ans = new int[n];
			sort(temp);
			int id = 0;

			int minmax = 0;
			boolean flag = false;
			for (int i = 0; i < bcount; i++) {

				minmax -= temp[id].val;
				for (int j = 0; j < m; j++) {
					ans[temp[id].idx] = j + 1;

					if (j == m) {
						minmax += temp[id].val;
					}
					id++;
				}

				if (minmax > x) {
					sb.append("NO\n");
					flag = true;
					break;
				}

			}

			for (int i = 0; i < extra; i++) {
				ans[temp[id].idx] = i + 1;
				id++;
			}

			if (flag == false) {
				sb.append("YES\n");
				for (int i = 0; i < n; i++) {
					sb.append(ans[i] + " ");
				}

				sb.append("\n");
			}

		}

		System.out.println(sb);
	}

	public static class Pair implements Comparable<Pair> {
		int val;
		int idx;

		public Pair(int val, int idx) {
			this.val = val;
			this.idx = idx;
		}

		@Override
		public int compareTo(Pair o) {
			return this.val - o.val;
		}
	}

	public static void sort(Pair[] arr) {
		ArrayList<Pair> list = new ArrayList<>();
		for (int i = 0; i < arr.length; i++) {
			list.add(new Pair(arr[i].val, arr[i].idx));
		}

		Collections.sort(list);

		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i);
		}

	}
}
