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
        List<Integer> result = new ArrayList<>();
        List<Integer> level = new ArrayList<>(List.of(start));
        boolean[] seen = new boolean[graph.size()];
        seen[start] = true;
        while (!level.isEmpty()) {
            result.addAll(level);
            List<Integer> next = new ArrayList<>();
            for (int v : level) {
                for (int u : graph.get(v)) {
                    if (!seen[u]) {
                        seen[u] = true;
                        next.add(u);
                    }
                }
            }
            level = next;
        }
        return result;
    }

    static List<List<Integer>> readGraph(Scanner in, int n, int m) {
        int[] from = new int[m], to = new int[m];
        for (int k = 0; k < m; k++) {
            from[k] = in.nextInt();
            to[k] = in.nextInt();
        }
        List<List<Integer>> graph = new ArrayList<>();
        for (int v = 0; v < n; v++) graph.add(new ArrayList<>());
        for (int k = 0; k < m; k++) {
            graph.get(from[k]).add(to[k]);
            graph.get(to[k]).add(from[k]);
        }
        graph.forEach(Collections::sort);
        return graph;
    }
}