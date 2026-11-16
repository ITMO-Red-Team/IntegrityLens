import java.util.*;
import java.io.*;
public class Fixed_Points 
{
	
	public static void process()throws IOException
	{
		int n=I();
	    String s[]=new String[n];
	    for(int i=0;i<n;i++)
	    {
	    	s[i]=S();
	    }
	    int max=0;
		for(char ch='a';ch<='e';ch++)
		{
			int t=0;
			ArrayList<Integer> po=new ArrayList<Integer>();
			ArrayList<Integer> ne=new ArrayList<Integer>();
			for(int j=0;j<n;j++)
			{
				int f1=0;int f2=0;
				
			    for(int k=0;k<s[j].length();k++)
			    {
			    	if(s[j].charAt(k)==ch)
			    	{
			    		f1++;
			    	}
			    	else
			    	{
			    		f2++;
			    	}
			    }
			    if(f1>f2)
			   po.add(f1-f2);
			    else
			    ne.add(f1-f2);
			}
			Collections.sort(po);
			Collections.reverse(po);
			Collections.sort(ne);
			Collections.reverse(ne);
			int sum=0;
			for(int i=0;i<po.size();i++)
			{
				sum=sum+po.get(i);
			}
			int tp=0;
			for(int i=0;i<ne.size();i++)
			{
				if(ne.get(i)+sum>0)
				{
				sum=sum+ne.get(i);
				tp++;
				}
			}
			t=tp+po.size();
			max=Math.max(max, t);
		}
		pn(max);
		
    }
	static Scanner sc = new Scanner(System.in);
	static PrintWriter out = new PrintWriter(System.out);
	static void pn(Object o){out.println(o);out.flush();}
	static void p(Object o){out.print(o);out.flush();}
	static void pni(Object o){out.println(o);System.out.flush();}
	static int I() throws IOException{return sc.nextInt();}
	static long L() throws IOException{return sc.nextLong();}
	static double D() throws IOException{return sc.nextDouble();}
	static String S() throws IOException{return sc.next();}
	static char C() throws IOException{return sc.next().charAt(0);}
	static int[] Ai(int n) throws IOException{int[] arr = new int[n];for (int i = 0; i < n; i++)arr[i] = I();return arr;}
	static String[] As(int n) throws IOException{String s[] = new String[n];for (int i = 0; i < n; i++)s[i] = S();return s;}
	static long[] Al(int n) throws IOException {long[] arr = new long[n];for (int i = 0; i < n; i++)arr[i] = L();return arr;}
	static void dyn(int dp[][],int n,int m,int z)throws IOException {for(int i=0;i<n;i++){ for(int j=0;j<m;j++){ dp[i][j]=z;}} }
	
//	*--------------------------------------------------------------------------------------------------------------------------------*//
	static class AnotherReader {BufferedReader br;StringTokenizer st;AnotherReader() throws FileNotFoundException {br = new BufferedReader(new InputStreamReader(System.in));}
	AnotherReader(int a) throws FileNotFoundException {br = new BufferedReader(new FileReader("input.txt"));}
	String next() throws IOException{while (st == null || !st.hasMoreElements()) {try {st = new StringTokenizer(br.readLine());} catch (IOException e) {e.printStackTrace();}}return st.nextToken();}
	int nextInt() throws IOException {return Integer.parseInt(next());}
	long nextLong() throws IOException {return Long.parseLong(next());}
	double nextDouble() throws IOException {return Double.parseDouble(next());}
	String nextLine() throws IOException {String str = "";try {str = br.readLine();} catch (IOException e) {e.printStackTrace();}return str;}}
	public static void main(String[] args)throws IOException{try{boolean oj=true;if(oj==true)
	{AnotherReader sk=new AnotherReader();PrintWriter out=new PrintWriter(System.out);}
	else
	{AnotherReader sk=new AnotherReader(100);out=new PrintWriter("output.txt");}
	long T=L();while(T-->0)
	{process();}out.flush();out.close();}catch(Exception e){return;}}}
//*-----------------------------------------------------------------------------------------------------------------------------------*//
	

