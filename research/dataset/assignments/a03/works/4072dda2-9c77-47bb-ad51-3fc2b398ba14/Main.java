import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AirConditioners {
	public static void main(String args[]) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			int t = Integer.parseInt(br.readLine());

			while (t-- > 0) {
				br.readLine();
				String[] str = br.readLine().split(" ");				
				int n = Integer.parseInt(str[0]);
				int k = Integer.parseInt(str[1]);
				
				String[] stra = br.readLine().split(" ");
				String[] strk = br.readLine().split(" ");
				
				int[] arr = new int[n];
				for (int i = 0; i < n; i++) {
					arr[i] = Integer.MAX_VALUE;
				}
				
				int[][] ac = new int[k][2];				
				for (int i = 0; i < k; i++) {
					ac[i][1] = Integer.parseInt(stra[i]) - 1;					
					ac[i][0] = Integer.parseInt(strk[i]);	
					arr[ac[i][1]] = ac[i][0];
				}				
				sort(ac, 0, k-1);
				
				for (int i = 0; i < k; i++) {
					int j = ac[i][1]-1;
					while (j >= 0 && ac[i][0] + Math.abs(ac[i][1] - j) < arr[j]) {
						arr[j] = ac[i][0] + Math.abs(ac[i][1] - j);
						j--;
					}
					j = ac[i][1]+1;
					while (j < n && ac[i][0] + Math.abs(ac[i][1] - j) < arr[j]) {
						arr[j] = ac[i][0] + Math.abs(ac[i][1] - j);
						j++;
					}
				}
				
				
//				for (int i = 0; i < k; i++) {
//					for (int j = 0; j < n; j++) {
//						arr[j] = Math.min(arr[j], ac[i][0] + Math.abs(ac[i][1] - j));
//					}
//				}
				
				for (int i = 0; i < n; i++) {
					System.out.print(arr[i] + " ");
				}
				System.out.println();

			}

		} catch (Exception e) {
			return;
		}
	}
	
	// Merges two subarrays of arr[].
	// First subarray is arr[l..m]
	// Second subarray is arr[m+1..r]
	static void merge(int arr[][], int l, int m, int r) {
		// Find sizes of two subarrays to be merged
		int n1 = m - l + 1;
		int n2 = r - m;
		
		/* Create temp arrays */
		int L[][] = new int[n1][2];
		int R[][] = new int[n2][2];
		
		/*Copy data to temp arrays*/
		for (int i = 0; i < n1; ++i) {
			L[i][0] = arr[l + i][0];
			L[i][1] = arr[l + i][1];
		}
		for (int j = 0; j < n2; ++j) {
			R[j][0] = arr[m + 1 + j][0];
			R[j][1] = arr[m + 1 + j][1];
		}
		
		/* Merge the temp arrays */
		
		// Initial indexes of first and second subarrays
		int i = 0, j = 0;
		
		// Initial index of merged subarry array
		int k = l;
		while (i < n1 && j < n2) {
			if (L[i][0] <= R[j][0]) {
				arr[k][0] = L[i][0];
				arr[k][1] = L[i][1];
				i++;
			} else {
				arr[k][0] = R[j][0];
				arr[k][1] = R[j][1];
				j++;
			}
			k++;
		}
		
		/* Copy remaining elements of L[] if any */
		while (i < n1) {
			arr[k][0] = L[i][0];
			arr[k][1] = L[i][1];
			i++;
			k++;
		}
		
		/* Copy remaining elements of R[] if any */
		while (j < n2) {
			arr[k][0] = R[j][0];
			arr[k][1] = R[j][1];
			j++;
			k++;
		}
	}
	
	// Main function that sorts arr[l..r] using
	// merge()
	// l = 0, r = n-1
	static void sort(int arr[][], int l, int r) {
		if (l < r) {
			// Find the middle point
			int m = l + (r - l) / 2;
			
			// Sort first and second halves
			sort(arr, l, m);
			sort(arr, m + 1, r);
			
			// Merge the sorted halves
			merge(arr, l, m, r);
		}
	}
}
