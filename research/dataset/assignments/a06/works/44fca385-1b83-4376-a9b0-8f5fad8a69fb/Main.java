import java.util.*;
import java.io.*;
public class Main  {
    public static void main(String args[]) {
      FastScanner fs=new FastScanner();    
      PrintWriter out=new PrintWriter(System.out);
      
      int t=fs.nextInt();
      while(t-->0)
      {
          int n=fs.nextInt();
          int c[]=fs.readArray(n);
          int i;
          
          
          long mn0=c[0];
          long mn1=c[1];
          long s0=c[0];
          long s1=c[1];
          
          
          long ans;
          ans=(mn0+mn1)*n*1L;
          
          for(i=2;i<n;i++)
          {
              if(i%2==0)
              {
                  s0+=c[i];
                  mn0=Math.min(mn0,c[i]);
              }
              else
              {
                  s1+=c[i];
                  mn1=Math.min(mn1,c[i]);
              }
              ans=Math.min(ans,s0+mn0*(n-i/2-1)+s1+mn1*(n-(i+1)/2));
          }
          
          out.println(ans);
          
          
          
          
      }
        
        out.flush();
        out.close();
    }
	static void sort(int a[]){
	    ArrayList<Integer> al=new ArrayList<>();
	    for(int x:a)al.add(x);
	    Collections.sort(al);
	    for(int i=0;i<a.length;i++) a[i]=al.get(i);
	}
}
        class FastScanner {
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st=new StringTokenizer("");
		
		String next() {
			while (!st.hasMoreTokens())
				try {
				st=new StringTokenizer(br.readLine());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			return st.nextToken();
		}
		String nextLine() {
		    String str="";
		    try {
		        str=br.readLine();
		    }
		    catch (IOException e) {
		        e.printStackTrace(); 
		    }
		    return str;
		}
		int nextInt() {
			return Integer.parseInt(next());
		}
		long nextLong() {
			return Long.parseLong(next());
		}
		int[] readArray(int n) {
			int[] a=new int[n];
			for (int i=0; i<n; i++) a[i]=nextInt();
			return a;
		}
	}