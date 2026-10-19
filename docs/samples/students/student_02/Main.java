import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> graph = readGraph(in, n, m);
        System.out.println(obhod(graph, 0));
    }

    static List<Integer> obhod(List<List<Integer>> g, int s) {
        boolean[] seen = new boolean[g.size()];
        Deque<Integer> q = new ArrayDeque<>();
        List<Integer> res = new ArrayList<>();
        seen[s] = true;
        q.add(s);
        while (!q.isEmpty()) {
            int cur = q.poll();
            res.add(cur);
            for (int x : g.get(cur)) {
                if (!seen[x]) {
                    seen[x] = true;
                    q.add(x);
                }
            }
        }
        return res;
    }

    static List<List<Integer>> load(Scanner sc, int n, int m) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int k = 0; k < n; k++) adj.add(new ArrayList<>());
        for (int k = 0; k < m; k++) {
            int a = sc.nextInt(), b = sc.nextInt();
            adj.get(a).add(b);
            adj.get(b).add(a);
        }
        for (List<Integer> r : adj) Collections.sort(r);
        return adj;
    }
}