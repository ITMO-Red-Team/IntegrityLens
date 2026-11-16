import java.util.*;

public class Solution {
	
	private static Scanner in = new Scanner(System.in);
	
	public static void main(String args[]) {
		
		int t = in.nextInt();
		
		while(t-->0) {
			solution();
		}
	}
	
	private static void solution() {
		
		int ans=0;
		int n = in.nextInt();
		String s[] = new String[n];
		int occurance[][] = new int[n][5];
		for(int i=0;i<n;i++) {
			s[i] = in.next();
			for(int j=0;j<s[i].length();j++) {
				occurance[i][s[i].charAt(j)-'a']++;
			}
		}
		
//		for(int i=0;i<n;i++)
//		{
//			for(int j=0;j<5;j++)
//				System.out.println(occurance[i][j]);
//			System.out.println();
//		}
		
		for(int i=0;i<5;i++) {
			int arr[] = new int[n];
			for(int j=0;j<n;j++) {
				arr[j] = s[j].length() - (2 * occurance[j][i]);
			}
			Arrays.sort(arr);
			
//			for(int j=0;j<n;j++)
//				System.out.println(arr[j]);
			
			int temp=0;
			int count=0;
			for(int j=0;j<n;j++) {
				if(temp+arr[j] < 0)
				{
					count++;
					temp += arr[j];
				}
				else 
					break;
			}
			ans = Math.max(ans, count);
		}
		System.out.println(ans);
	}
}