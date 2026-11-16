import java.io.*;
import java.util.*;

public class C {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        PrintWriter pw = new PrintWriter(System.out);
        int t = in.nextInt();
        for (int t1 = 0; t1 < t; t1++) {
            int n = in.nextInt(), m = in.nextInt(), x = in.nextInt();
            ArrayList<Pair> list = new ArrayList<>();
            for (int i = 1; i <= n; i++) list.add(new Pair(in.nextInt(), i));
            pw.println(solve(list, n, m, x));
        }

        pw.close();
    }

    static StringBuilder solve(ArrayList<Pair> arr, int n, int m, int x) {
        Stack<Pair> stack = new Stack<>();
        HashMap<Integer, Integer> map = new HashMap<>();
        PriorityQueue<Pair> pq = new PriorityQueue<>();
        Collections.sort(arr);
        for (int i = m; i < n; i++) stack.push(arr.get(i));
        for (int i = 0, j = 1; i < m; i++, j++) {
            Pair p = arr.get(i);
            pq.add(new Pair(p.f, j));
            map.put(p.s, j);
        }
        while (!stack.isEmpty()) {
            Pair val = stack.pop();
            Pair p = pq.remove();
            map.put(val.s, p.s);
            pq.add(new Pair(p.f + val.f, p.s));
        }
      //  debug(pq);
        int min = pq.remove().f;
        while (!pq.isEmpty()) {
            int val = pq.remove().f;
            if ((val - min) > x) return new StringBuilder().append("NO");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("YES\n");
        for (int i = 1; i <= n; i++) {
            sb.append(map.get(i) + " ");
        }
        return sb;
    }

    static class Pair implements Comparable<Pair> {
        int f, s;

        Pair(int f, int s) {
            this.f = f;
            this.s = s;
        }

        @Override
        public int compareTo(Pair p) {
            return Integer.compare(this.f, p.f);
        }

        @Override
        public String toString() {
            return f + " " + s;
        }
    }

    static void debug(Object... obj) {
        System.err.println(Arrays.deepToString(obj));
    }
}