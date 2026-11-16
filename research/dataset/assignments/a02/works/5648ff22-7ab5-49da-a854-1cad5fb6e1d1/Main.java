import java.util.*;
import java.io.*;

public class Menorah {

	public static void main(String[] args) throws IOException {
		BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
		int t=Integer.parseInt(f.readLine());
		while(t-->0) {
			int n=Integer.parseInt(f.readLine());
			String s=f.readLine();
			String s2=f.readLine();
			int[] arr=new int[4];
			//0 00 1 01 2 10 3 11
			for(int i=0;i<n;i++) {
				if(s.charAt(i)=='0') {
					if(s2.charAt(i)=='0') {
						arr[0]++;
					}
					else {
						arr[1]++;
					}
				}
				else {
					if(s2.charAt(i)=='0') {
						arr[2]++;
					}
					else {
						arr[3]++;
					}
				}
			}
			int min=Integer.MAX_VALUE;
			if(arr[1]==arr[2]) {
				min=arr[1]*2;
			}
			int temp=arr[1];
			arr[1]=arr[3];
			arr[3]=temp;
			temp=arr[0];
			arr[0]=arr[2];
			arr[2]=temp;
			arr[3]++;
			arr[1]--;
			if(arr[1]==arr[2]) {
				min=Math.min(min, arr[1]*2+1);
			}
			System.out.println(min!=Integer.MAX_VALUE?min:-1);
		}
	}

}
