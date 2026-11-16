import java.util.*;
import java.util.Scanner;


public class Solution {
	static int mod=1000000007;;
	
//	 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		System.out.println();

		Scanner sc=new Scanner(System.in);
		int tt=sc.nextInt();
//		
//		
		while(tt-->0){
				int n=sc.nextInt();
			
				int k[]=new int[n];
				int h[]=new int[n];
				
				for(int i=0;i<n;i++) {
					k[i]=sc.nextInt();
				}
				for(int i=0;i<n;i++) {
					h[i]=sc.nextInt();
				}
				long ans=0;
				int start=k[0]-h[0]-1;
				int end=k[0];
				int last=0;
				for(int j=0;j<n;j++) {
					 start=k[j]-h[j]+1;
					 end=k[j];
					 last=j;
				for(int i=j+1;i<n;i++) {
					int temp=k[i]-h[i]+1;
					
					if(temp<=end) {
						start=Math.min(start, temp);
						end=Math.max(end, k[i]);
						last=i;
					}
					
					
					
				}
				j=last;
				
				long va=end-start+1;
				
				ans+=(va*(va+1))/2;
				
				}
				System.out.println(ans);
				
			
		}
	}
		
		
	
		
		
	
	
	


}
