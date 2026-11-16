import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class MinimumGridPath {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = br.readLine();
		int cases = Integer.parseInt(line);
		for(int i = 0; i<cases; i++) {
			int n = Integer.parseInt(br.readLine());
			
			long[] segments = new long[n];
			Scanner sc = new Scanner(br.readLine());
			for(int k = 0; k<n; k++) {
				segments[k] = sc.nextLong();
			}
			
			long minHor = segments[0], minVer = Long.MAX_VALUE;
			long sumHor = segments[0], sumVer = 0;
			long best = Long.MAX_VALUE;
			for(int k = 1; k<n; k++) {
				if(k % 2 == 1) {
					minVer = Long.min(minVer, segments[k]);
					sumVer += segments[k];
				}else {
					minHor = Long.min(minHor, segments[k]);
					sumHor += segments[k];
				}
				long cost = sumVer + sumHor + minVer * (n - (k+1)/2) + minHor * (n- (1+k/2));
				best = Long.min(best, cost);
			}
			
			System.out.println(best);
		}
	
	}
}