import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> graph = readGraph(in, n, m);
        System.out.println(bfs(graph, 0));
    }

    static List<Integer> bfs(List<List<Integer>> graph, int start) {
        boolean[] visited = new boolean[graph.size()];
        Deque<Integer> queue = new ArrayDeque<>();
        List<Integer> order = new ArrayList<>();
        visited[start] = true;
        queue.add(start);
        while (!queue.isEmpty()) {
            int node = queue.poll();
            order.add(node);
            for (int nb : graph.get(node)) {
                if (!visited[nb]) {
                    visited[nb] = true;
                    queue.add(nb);
                }
            }
        }
        return order;
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
}