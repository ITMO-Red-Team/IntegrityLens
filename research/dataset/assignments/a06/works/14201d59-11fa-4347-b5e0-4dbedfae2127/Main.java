import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
public class Main {

    public static void main(String[] args) {
        FastScanner in=new FastScanner();
        PrintWriter out=new PrintWriter(System.out);
        int t=in.nextInt();
        while(t-->0)
        solve(in,out);
        out.close();
    }
    static void solve(FastScanner in,PrintWriter out){
        int n=in.nextInt();
        long a[]=new long[n];
        for (int i = 0; i < n; i++) {
            a[i]=in.nextLong();
        }
        long odd=Integer.MAX_VALUE,even=Integer.MAX_VALUE;
        even=a[0];
        long sum=a[0];
        long ans=Long.MAX_VALUE;
        for (int i = 1; i < n; i++) {
            if(i%2==0) {
                ans=Math.min(ans,(n-i/2)*a[i] + odd*(n-i/2) +sum);
                even=Math.min(even,a[i]);
            } else {
                ans=Math.min(ans,(n-i/2)*a[i] + even*(n-i/2-1) +sum);
                odd=Math.min(odd,a[i]);
            }
            sum+=a[i];
        }
        out.println(ans);
    }

    static class pair<U extends Comparable<U>, V extends Comparable<V>> implements Comparable<pair<U, V>> {
        public U x;
        public V y;

        public pair(U x, V y) {
            this.x = x;
            this.y = y;
        }

        public int compareTo(pair<U, V> other) {
            int i = x.compareTo(other.x);
            if (i != 0) return i;
            return y.compareTo(other.y);
        }

        public String toString() {
            return x.toString() + " " + y.toString();
        }

        public boolean equals(Object obj) {
            if (this.getClass() != obj.getClass()) return false;
            pair<U, V> other = (pair<U, V>) obj;
            return x.equals(other.x) && y.equals(other.y);
        }

        public int hashCode() {
            return 31 * x.hashCode() + y.hashCode();
        }

    }

    static void sort(int[] a) {
        ArrayList<Integer> l=new ArrayList<>();
        for (int i:a) l.add(i);
        Collections.sort(l);
        for (int i=0; i<a.length; i++) a[i]=l.get(i);
    }

    static class FastScanner {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st=new StringTokenizer("");
        String next() {
            while (!st.hasMoreTokens())
                try {
                    st=new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }
        int[] readArray(int n) {
            int[] a=new int[n];
            for (int i=0; i<n; i++) a[i]=nextInt();
            return a;
        }
        long nextLong() {
            return Long.parseLong(next());
        }
    }
}
