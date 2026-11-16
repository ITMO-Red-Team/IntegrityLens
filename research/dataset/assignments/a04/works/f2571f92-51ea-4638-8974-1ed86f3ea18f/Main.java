import java.util.*;
import java.io.*;
public class JavaTract
{
	static class Pair implements Comparable<Pair>{
    	int first;
    	int second;
    	Pair(int x,int y){
        	this.first=x;
        	this.second=y;
    	}
    	@Override
    	public int compareTo(Pair x){
        	return this.first-x.first;
   		}
   	}	
    public static void main (String[] args) 
    {
        Scanner scan=new Scanner(System.in);
        int t=scan.nextInt();
        while(t-->0){
            int n=scan.nextInt();
            int m=scan.nextInt();
            int x=scan.nextInt();
            int[]arr=new int[n];
            for(int i=0;i<n;i++){
                arr[i]=scan.nextInt();
            }
            // TreeSet<Pair> set = new TreeSet<>(); 
            Queue<Pair> set = new PriorityQueue<>();
            for(int i=1;i<=m;i++){
               
               set.add(new Pair(0,i));
            }
            
            System.out.println("YES");
            for(int i=0;i<n;i++){
                Pair temp=set.poll();
                
                int first = temp.first;
                int second = temp.second;

                System.out.print(second+" ");
                
                set.add(new Pair(first+arr[i],second));
                
            }
            System.out.println();


        }
    }

       
}
	
