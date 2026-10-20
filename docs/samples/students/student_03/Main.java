import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> graph = readGraph(in, n, m);
        System.out.println(bfs(graph, 0));
    }

    static List<List<Integer>> readGraph(Scanner in, int n, int m) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int i = 0; i < m; i++) {
            int u = in.nextInt(), v = in.nextInt();
            g.get(u).add(v);
            g.get(v).add(u);
        }
        for (List<Integer> row : g) Collections.sort(row);
        return g;
    }

    static List<Integer> bfs(List<List<Integer>> graph, int start) {
        int[] dist = new int[graph.size()];
        Arrays.fill(dist, -1);
        dist[start] = 0;
        Queue<Integer> q = new LinkedList<>();
        q.offer(start);
        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int v = q.poll();
            order.add(v);
            for (int to : graph.get(v)) {
                if (dist[to] == -1) {
                    dist[to] = dist[v] + 1;
                    q.offer(to);
                }
            }
        }
        return order;
    }
}