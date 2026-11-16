import java.io.*;
import java.lang.*;
import java.util.*;
public class c
{
static class FastScanner {
InputStreamReader is;
BufferedReader br;
StringTokenizer st;
public FastScanner() {
is = new InputStreamReader(System.in);
br = new BufferedReader(is);
}
String next() throws Exception {
while (st == null || !st.hasMoreElements())
st = new StringTokenizer(br.readLine());
return st.nextToken();
}
int ni() throws Exception {
return Integer.parseInt(next());
}
long nextLong() throws Exception {
return Long.parseLong(next());
}
int[] readArray(int num) throws Exception {
int arr[]=new int[num];
for(int i=0;i<num;i++)
arr[i]=ni();
return arr;
}
String nextLine() throws Exception {
return br.readLine();
}
}
 public static boolean power_of_two(int a)
{
  if((a&(a-1))==0)
{
 return true;
}
return false;
}
static boolean PS(double x)
{
if (x >= 0) {
double i= Math.sqrt(x);
if(i%1!=0)
{
return false;
}
return ((i * i) == x);
}
return false;
}
public static int[] ia(int n)
{
    int ar[]=new int[n];
    return ar;
}
public static long[] la(int n)
{
    long ar[]=new long[n];
    return ar;
}

static class pair implements Comparable<pair>{
    int ht;
    int id;
    pair(int ht, int id)
{
    this.ht=ht;
    this.id=id;
}
public int compareTo(pair p)
{
    return this.ht-p.ht;
}
}
public static void main(String args[]) throws java.lang.Exception
{
FastScanner sc=new FastScanner();
 int t=sc.ni();
while(t-->0)
{
    int n=sc.ni();
    int m=sc.ni();
    int x=sc.ni();
    int ar[]=ia(n);
for(int i=0;i<n;i++)
{
ar[i]=sc.ni();
}
System.out.println("YES");
PriorityQueue<pair> pq=new PriorityQueue<>();
 for(int i=0;i<m;i++)
 {
pq.add(new pair(0,i+1));
 }                     
 int i=0;
 while(i<n)
 {
     pair pp=pq.remove();
pp.ht+=ar[i];
System.out.print(pp.id+" ");
pq.add(pp);
i++;
 }   
System.out.println();
}
}
}