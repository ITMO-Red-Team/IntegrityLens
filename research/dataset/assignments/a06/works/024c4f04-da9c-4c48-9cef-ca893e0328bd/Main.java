import java.util.*;
import java.lang.*;
import java.io.*;

public class Main
{
	PrintWriter out = new PrintWriter(System.out);
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    StringTokenizer tok = new StringTokenizer("");
    String next() throws IOException {
        if (!tok.hasMoreTokens()) { tok = new StringTokenizer(in.readLine()); }
        return tok.nextToken();
    }
    int ni() throws IOException { return Integer.parseInt(next()); }
    long nl() throws IOException { return Long.parseLong(next()); }
    
    long mod=1000000007;
    
    void solve() throws IOException {
        for (int tc=ni();tc>0;tc--) {
            int n=ni();
            int[]A=new int[n];
            long[]T=new long[n];
            A[0]=ni();
            T[0]=A[0];
            long total=0;
            for (int i=1;i<n;i++) { A[i]=ni(); T[i]=T[i-1]+A[i]; } 
            
            long[]B=new long[n];
            long lefteven=n-1;
            long leftodd=n;
            int mineven=A[0];
            int minodd=A[1];
            long ans=Long.MAX_VALUE;
            
            for (int i=1;i<n;i++) {
                if (i%2==1) {
                    leftodd--;
                    minodd=Math.min(minodd,A[i]);
                    B[i]=T[i]+lefteven*mineven+leftodd*minodd;
                }
                else {
                    lefteven--;
                    mineven=Math.min(mineven,A[i]);
                    B[i]=T[i]+lefteven*mineven+leftodd*minodd;
                }
                ans=Math.min(ans,B[i]);
            }
            
            out.println(ans);
        }
        out.flush();
    }
    
    int gcd(int a,int b) { return(b==0?a:gcd(b,a%b)); }
    long gcd(long a,long b) { return(b==0?a:gcd(b,a%b)); }
    long mp(long a,long p) { long r=1; while(p>0) { if ((p&1)==1) r=(r*a)%mod; p>>=1; a=(a*a)%mod; } return r; }
    
    public static void main(String[] args) throws IOException {
        new Main().solve();
    }
}