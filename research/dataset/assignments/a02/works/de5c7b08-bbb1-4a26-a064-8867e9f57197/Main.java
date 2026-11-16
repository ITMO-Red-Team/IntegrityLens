import java.io.*;
import java.util.*;

public class C {

	public static void main(String[] args) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
		
		int t = Integer.parseInt(br.readLine());
		
		while(t --> 0) {
			
			int n = Integer.parseInt(br.readLine());
			
			char[] lineA = br.readLine().toCharArray();
			char[] lineB = br.readLine().toCharArray();
			
			boolean[] a = new boolean[n];
			boolean[] b = new boolean[n];
			
			int ac = 0;
			int aic = 0;
			int bc = 0;
			int stay = 0;
			int flip = 0;
			
			for(int i = 0; i < n; i++) {
				
				if(lineA[i] == '1') {
					
					ac++;
					a[i] = true;
					
				}else
					aic++;
				
				if(lineB[i] == '1') {
					
					bc++;
					b[i] = true;
					
				}
				
				if(a[i] == b[i])
					stay++;
				else
					flip++;
				
			}
			
			if(ac != bc && aic + 1 != bc) {
				
				pw.println(-1);
				
			}else {
				
				if(ac == aic+1)
					pw.println(Math.min(stay, flip));
				else if(ac == bc)
					pw.println(flip);
				else
					pw.println(stay);
				
			}
			
		}
		
		pw.close();

	}

}
