import java.util.*;
import java.io.*;
        
@SuppressWarnings("unchecked")
public class Main {
    public static void main(String args[]) {    
        FastReader input = new FastReader();
        PrintWriter out = new PrintWriter(System.out);
        int t = input.nextInt();
        while(t-- > 0) {
        int n = input.nextInt();
        int m = input.nextInt();
        int x = input.nextInt();
        int arr[] = new int[n];
        for(int i = 0; i < n; i++) arr[i] = input.nextInt(); 
        PriorityQueue<Pair> queue = new PriorityQueue();
        out.println("YES");
        int res[] = new int[n];
        for(int i = 1; i <= m; i++) {
            queue.add(new Pair(0, i));
        }
        for(int i = 0; i < n; i++) {
            Pair p = queue.remove();
            out.print(p.second()+" ");
            queue.add(new Pair(p.first()+arr[i], p.second()));    
        }
        out.println();
        out.flush();
        }
    }   
}
 
class Pair implements Comparable<Pair> {    
    public int x;
    public int y;
 
    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }
 
    public int first() {
        return x;
    }
    public int second() {
        return y;
    }
 
    public int compareTo(Pair p) {
        if(this.first() == p.first()) {
            return 0;
        }else if(this.first() > p.first()) {
            return 1;
        }else {
            return -1;
        }
    } 
}
 
class FastReader {
    BufferedReader br;
    StringTokenizer st;
    
    public FastReader() {
        br = new BufferedReader(new InputStreamReader(System.in));
    }
    public String next() {
        while(st == null || !st.hasMoreElements()) {
            try {
                st = new StringTokenizer(br.readLine());
            }catch(Exception e) {
                System.out.println(e);
            }
        }     
        return st.nextToken();
    }
    public int nextInt() {
        return Integer.parseInt(next());
    }
 
    public long nextLong() {
        return Long.parseLong(next());
    }
 
    public String getString() {
        return next();
    }
}