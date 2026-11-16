
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main
{
 
    public static void main(String[] args)
    {
      FastScanner input = new FastScanner();
        int tc = input.nextInt();
        while (tc-- > 0) {
         int n = input.nextInt();
      int k = input.nextInt();
      int a[] = new int[n+1];
      int b[] = new int[k+1];
        for (int i = 1; i <=k; i++) {
            b[i] = input.nextInt();
            
        }
        Arrays.fill(a, Integer.MAX_VALUE);
        for (int i = 1; i <=k; i++) {
           int value = input.nextInt();
           a[b[i]] = value;
            
        }
//        System.out.println(Arrays.toString(a));
        long L[] = new long[n+1];
        long R[] = new long[n+1];
        Arrays.fill(L, Integer.MAX_VALUE);
        Arrays.fill(R, Integer.MAX_VALUE);
        long p = Integer.MAX_VALUE;
        for (int i = 1; i <=n; i++) {
             p = Math.min(p+1, a[i]);
             L[i] = p;
        }
        p = Integer.MAX_VALUE;
        for (int i = n; i >=1; i--) {
            p = Math.min(p+1,a[i] );
            R[i] = p;
        }
//        System.out.println(Arrays.toString(L));
//        System.out.println(Arrays.toString(R));
        long ans[] = new long[n+1];
        for (int i = 1; i <=n; i++) {
            ans[i] = Math.min(L[i], R[i]);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 1; i <=n; i++) {
         result.append(ans[i]+" ");
        }
        System.out.println(result);   
        }
      
    }

    

    static class FastScanner
    {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");

        String next()
        {
            while (!st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                }
            }
            return st.nextToken();
        }

        int nextInt()
        {
            return Integer.parseInt(next());
        }

        long nextLong()
        {

            return Long.parseLong(next());
        }

        double nextDouble()
        {
            return Double.parseDouble(next());
        }

        String nextLine() throws IOException
        {
            return br.readLine();
        }
    }

}
