import java.util.*;

public class Solution {

    public static void main(String args[])

    {   Scanner s = new Scanner(System.in);

        int t = s.nextInt();

        while(t-- > 0){
        	
        	int n= s.nextInt();
        	
        	long [] time= new long [n];
        	
        	long [] health= new long [n];
        	
        	for(int i=0; i<n; i++)
        		
        		time[i]= s.nextLong();
        	
        	for(int i=0; i<n; i++)
        		
        		health[i]= s.nextLong();
        	
        	long currhealth= health[n-1];
        	
        	long currtime= time[n-1];
        	
        	long ans=0;
        	
        	for(int i= n-2; i>=0; i--)
        	
        	{   if(currtime-time[i]>= currhealth)
        		
        	    {  ans+= (currhealth*(currhealth+1))/2;
        	    
        	       currhealth= health[i];
        	       
        	       currtime= time[i];
        		
        	    }
        	
        	    else if(currtime-time[i]>currhealth-health[i])
        	    	
        	    	currhealth+= (currtime-time[i]-currhealth+health[i]);
        		
        	}
        	
        	ans+= (currhealth*(currhealth+1))/2;
        	
        	System.out.println(ans);

        }
        
        s.close();

    }
    
}