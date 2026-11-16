import java.io.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.util.*;
public class Main{
	static Scanner scanner=new Scanner(System.in);
	public static void main(String[] args) {
		int q=scanner.nextInt();
		while(q-->0) {
			int n=scanner.nextInt();
			long a[]=new long [n];
			for(int i=0;i<n;i++)a[i]=scanner.nextLong();
			long minr=Integer.MAX_VALUE;
			long mind=Integer.MAX_VALUE;
			long x=a[0],s=a[0]*n;
			long y=a[1],ss=a[1]*n;
			long ans=s+ss;
			for(int i=2;i<n;i++) {
				if(i%2==0) {
					if(x<=a[i]) {
						s+=(a[i]-x);
					}else {
						s-=(x-a[i])*(n-(i)/2);
						x=a[i];
						//System.out.println("oooo");
					}
				}else {
					if(y<=a[i]) {
						ss+=(a[i]-y);
					}else {
						//System.out.println("ppp");
						ss-=(y-a[i])*(n-(i)/2);
						y=a[i];
					}
				}
				ans=Math.min(ans, s+ss);
			}
			System.out.println(ans);
		}
	}
}













