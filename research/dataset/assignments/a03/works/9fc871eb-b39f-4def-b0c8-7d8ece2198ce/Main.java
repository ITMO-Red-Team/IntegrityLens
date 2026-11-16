import java.util.*;
public class Main
{
	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		int t=sc.nextInt();
		while(t-->0){
		    int n=sc.nextInt();
		    int k=sc.nextInt();
		    
		    int idx[]=new int[k];
		    
		    for(int i=0;i<k;i++){
		        idx[i]=sc.nextInt();
		    }
		    
		    long arr[]=new long[n];
		    Arrays.fill(arr,Integer.MAX_VALUE);
		    for(int i=0;i<k;i++){
		        long temp=sc.nextLong();
		        arr[idx[i]-1]=temp;
		    }
		    
		    long left[]=new long[n];
		    long right[]=new long[n];
		    
		    Arrays.fill(left,Integer.MAX_VALUE);
		    Arrays.fill(right,Integer.MAX_VALUE);
		    
		    
		    left[0]=arr[0];
		    
		    for(int i=1;i<n;i++){
		       left[i]=Math.min(left[i-1]+1,arr[i]);
		    }
		    
		    right[arr.length-1]=arr[arr.length-1];
		    
		    for(int i=n-2;i>=0;i--){
		        right[i]=Math.min(right[i+1]+1,arr[i]);
		    }
		    
		    for(int i=0;i<n;i++){
		      //  System.out.print(left[i]+"--"+right[i]+"\\");
		       
		       
		       System.out.print(Math.min(left[i],right[i])+" ");
		    }
		    System.out.println();
		  
		}
	}
}
