import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

public class java1 {
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        TaskB solver = new TaskB();
        solver.solve(1, in, out);
        out.close();
    }
    static class TaskB {

        public void solve(int testNumber, InputReader in, PrintWriter out) {
            int t=in.nextInt();
          tt:  while(t-- >0)
            {
            	int n=in.nextInt();
            	String ar[]=new String[n];
            	for(int x=0;x<n;x++)
            	{
            		ar[x]=in.next();
            	}
            	char ch[]= {'a', 'b', 'c', 'd', 'e'};
            	int max=-1;
            	for(char c:ch)
            	{
            		int lmax=0;
            		ArrayList<Integer> al=new ArrayList<>();
            		int c1=0;
            		int c2=0;
            		for(int x=0;x<n;x++)
            		{
            			int a1=0;
            			int a2=0;
            			for(int y=0;y<ar[x].length();y++)
            			{
            				if(ar[x].charAt(y)==c)
            				{
            					a1++;
            				}
            				else
            				{
            					a2++;
            				}
            			}
            			if(a1>a2)
            			{
            				c1+=a1; c2+=a2; lmax++;
            			}
            			else
            			{
            				al.add(a2-a1);
            			}
            		}
            		Collections.sort(al);
            		for(int x:al)
            		{
            			if(c1>c2+x )
            			{
            				lmax++; c2+=x;
            			}
            		}
            		max=Math.max(lmax, max);
            	}
            	out.println(max);
            	
           
            	
            			
            }
        }
        static void sort(int[] a) {
    		ArrayList<Integer> l=new ArrayList<>();
    		for (int i:a) l.add(i);
    		Collections.sort(l);
    		for (int i=0; i<a.length; i++) a[i]=l.get(i);
    	}
        
    }
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }
        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }
        public int nextInt() {
            return Integer.parseInt(next());
        }
        public long nextLong() {
            return Long.parseLong(next());
        }
        public int[] inputarInt(int n)
        {
        	int ar[]=new int[n];
        	for(int x=0;x<n;x++)
        	{
        		ar[x]=nextInt();
        	}
        	return ar;
        }
        public long[] inputarLong(int n)
        {
        	long ar[]=new long[n];
        	for(int x=0;x<n;x++)
        	{
        		ar[x]=nextLong();
        	}
        	return ar;
        }
    }
}