import java.util.*;
public class solution{
	

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int t = sc.nextInt();
		while(t-- > 0){
			int n = sc.nextInt();
			long[] k = new long[n];
			long[] h = new long[n];
			for(int i = 0;i < n;i++){
				k[i] = sc.nextLong();
			}
			for(int i = 0;i < n;i++){
				h[i] = sc.nextLong(); 
			}
			long length = h[n-1];
			long curr = 0;
			long ans = 0;
			for(int i = n-1;i >= 1;i--){
				if(k[i] - k[i-1] >= length){
					curr += length;
					ans = ans += (curr * (curr + 1))/2;
					length = h[i-1];
					curr = 0;
				}else{
					length = Math.max(length - (k[i] - k[i-1]), h[i-1]);
					curr += k[i] - k[i-1];
				}
			}
			curr += length;
			ans += (curr * (curr + 1))/2;
			System.out.println(ans);
		}
	}
}