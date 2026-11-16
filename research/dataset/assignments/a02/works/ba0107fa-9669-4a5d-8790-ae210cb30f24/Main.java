/* package whatever; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
public class Ideone
{
	public static void main (String[] args) throws java.lang.Exception
	{
		Scanner scn = new Scanner(System.in);
		int t = scn.nextInt();
		while(t>0){
			int n = scn.nextInt();
			String a = scn.next();
			String b = scn.next();
		
	//	System.out.println(a);
			int ob=0,oa=0,za=0;
			for(int i=0;i<a.length();i++){
				if(a.charAt(i)=='1'){
					
					oa++;
				}else{
					za++;
				}
			}
			for(int i=0;i<b.length();i++){
				if(b.charAt(i)=='1'){
					ob++;	
				}
			}
		//	System.out.println(ob+" "+za);
			int min = Integer.MAX_VALUE;
			if(ob==oa){
				int op=0;
				for(int i=0;i<a.length();i++){
					if(a.charAt(i)!=b.charAt(i)){
						op++;
					}
				}
			
				min = Math.min(min,op);
			}
			if(ob==za+1){
				int op=0;
				for(int i=0;i<n;i++){
					if(a.charAt(i)==b.charAt(i)){
						op++;
					}
				}
				min = Math.min(min,op);
			}
			
			if(min!=Integer.MAX_VALUE){
				System.out.println(min);
				}else{
				System.out.println(-1);
			}
			t--;
		}
	}
}