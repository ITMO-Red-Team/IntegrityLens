import java.io.*;
import java.util.*;
public class qC {

	public static void main(String[] args)throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int T = Integer.parseInt(br.readLine());
		while(T-- > 0) {
			int N = Integer.parseInt(br.readLine());
			char[] curr = br.readLine().toCharArray();
			char[] sol = br.readLine().toCharArray();
//			int oddP = 0;
//			int evenP = 0;
//			int even1 = 0;
//			int odd1 = 0;
//			int even0 = 0;
//			int odd0 = 0;
//			boolean alreadySolved = true;
//			for(int i = 0;i < N;i++) {
//				if(curr[i] == sol[i]) {
//					evenP++;
//					if(curr[i] == '1') even1++;
//					else even0++;
//				}
//				else {
//					oddP++;
//					if(curr[i] == '1') odd1++;
//					else odd0++;
//					alreadySolved = false;
//				}
//			}
//			
//			if(alreadySolved) {
//				System.out.println(0);
//				continue;
//			}
//			int minSwaps = Integer.MAX_VALUE;
//			if(N % 2 == 1) {
//				if(evenP % 2 == 1 && Math.abs(odd1 - odd0) <= 1 && odd1 > 0 && odd0 > 0) {
//					minSwaps = Math.min(minSwaps, oddP);
//				}
//				if(oddP % 2 == 0 && Math.abs(even1 - even0) <= 1 && even1 > 0 && even1 > 0) {
//					minSwaps = Math.min(minSwaps, evenP);
//				}
//			}
//			else {
//				if(evenP % 2 == 0 && Math.abs(odd1 - odd0) <= 1 && odd1 > 0 && odd0 > 0) {
//					minSwaps = Math.min(minSwaps, oddP);
//				}
//				if(oddP % 2 == 1 && Math.abs(even1 - even0) <= 1 && even1 > 0 && even1 > 0) {
//					minSwaps = Math.min(minSwaps, evenP);
//				}
//			}
//			System.out.println((minSwaps == Integer.MAX_VALUE) ? -1: minSwaps);
			
			int curr1 = 0;
			int sol1 = 0;
			int mismatch = 0;
			for(int i = 0;i < N;i++) {
				if(curr[i] == '1') curr1++;
				if(sol[i] == '1') sol1++;
				if(curr[i] != sol[i]) mismatch++;
			}
			
			int minAns = Integer.MAX_VALUE;
			//even operations
			if(curr1 == sol1 && mismatch % 2 == 0) {
				minAns = Math.min(mismatch, minAns);
			}
			//odd operations
			for(int i = 0;i < N;i++) {
				if(curr[i] == '1') {
					int tempcurr1 = N - curr1 + 1;
					int tempmismatch;
					if(sol[i] == '0') {
						tempmismatch = N - mismatch;
					}
					else {
						tempmismatch = N - mismatch - 1;
					}
					if(tempcurr1 == sol1 && tempmismatch % 2 == 0) {
						minAns = Math.min(minAns, tempmismatch + 1);
					}
				}
			}
			
			System.out.println((minAns == Integer.MAX_VALUE) ? -1 : minAns);
		}

	}

}
