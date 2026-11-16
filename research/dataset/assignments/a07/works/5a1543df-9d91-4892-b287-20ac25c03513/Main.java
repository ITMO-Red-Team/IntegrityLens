import java.io.*;
import java.util.*;

public class E {

	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		int n = Integer.parseInt(br.readLine());
		int cnt = n;
		boolean[] non = new boolean[n];
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		for(int i = 0; i < n; i++) {
			
			if(Integer.parseInt(st.nextToken()) == 0) {
				non[i] = true;
				cnt--;
			}
			
		}
		
		int x = 0;
		int y = 0;
		int[] location = new int[cnt];
		int[] rlocation = new int[n-cnt];
		
		for(int i = 0; i < n; i++) {
			
			if(!non[i]) {
				
				location[x] = i;
				x++;
				
			}else{
				
				rlocation[y] = i;
				y++;
				
			}
			
		}
		
		int[][] dp = new int[(n-cnt)+1][cnt+1];
		Arrays.fill(dp[0], 100000000);
		dp[0][0] = 0;
		
		for(int i = 0; i < n-cnt; i++) {
			
			//System.out.println("HIT");
			
			if(i < (n-cnt))
				Arrays.fill(dp[i+1], 100000000);
						
			for(int j = 0; j < cnt; j++) {
				
				if(i < (n-cnt)) {
					dp[i+1][j] = Math.min(dp[i+1][j], dp[i][j]);
					dp[i+1][j+1] = Math.min(dp[i+1][j+1], dp[i][j] + Math.abs(rlocation[i] - location[j]));			
					//System.out.println(dp[i+1][j+1] + " " + dp[i][j] + " " + j + " " + rlocation[i] + " " + location[j]);
				}
				
			}
			
		}
		
		int min = Integer.MAX_VALUE;
		
		for(int i = 0; i < (n-cnt)+1; i++) {
			
			min = Math.min(dp[i][cnt], min);
			
		}
				
		System.out.println(min);
		
	}

}