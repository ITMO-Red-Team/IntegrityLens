import java.util.*;
import java.io.*;
import java.math.BigInteger;
 
public class code2{
 
    public static int GCD(int a, int b)
    {
      if (b == 0)
        return a;
      return GCD(b, a % b);
    }
    
    
    //@SuppressWarnings("unchecked")
    public static void main(String[] arg) throws IOException{
        //Reader in=new Reader();
        
        PrintWriter out = new PrintWriter(System.out);
        Scanner in = new Scanner(System.in);
        
        int t=in.nextInt();
        
        while(t-- > 0){
           int n=in.nextInt();
           long[] k=new long[n];
           long[] h=new long[n];

           for(int i=0;i<n;i++) k[i]=in.nextLong();
           for(int i=0;i<n;i++) h[i]=in.nextLong();

           long[] res=new long[n];
           long[] power=new long[n];
           long l=k[n-1]+1,r=-1;

           for(int i=0;i<n;i++){
             long lc=k[i]-h[i]+1;
             //out.println("lc:"+lc);
             long m=h[i];
             if(lc<=l){
               res[i]=(m*(m+1))/2;
               power[i]=h[i];
             }
             else if(lc>r){
               //out.println(m);
               res[i]=res[i-1]+(m*(m+1))/2;
               power[i]=h[i];
             }
             else{
               res[i]=Long.MAX_VALUE;
               
               for(int j=i-1;j>=0;j--){
                 
                if(power[j]+k[i]-k[j] >= h[i]){
					
                   long x=power[j]+k[i]-k[j];
				   
                   if(x>=h[i]){
					  if(k[i]-h[i]+1>k[j]){
						power[i]=h[i];
						res[i]=res[j]+(m*(m+1))/2;
					  } 
					  else{
						power[i]=x;
						long c=(x*(x+1))/2;
						long d=(power[j]*(power[j]+1))/2;
						res[i]=res[j]+c-d;
					  }
					  break;
				   }

                 }

               }
             }
             l=Math.min(l,lc);
             r=k[i];
             
           }

           out.println(res[n-1]);
        }
 
        out.flush();
    }
 
}

