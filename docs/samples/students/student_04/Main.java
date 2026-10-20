import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> adj = build(in, n, m);
        System.out.println(traverse(adj, 0));
    }

    // список смежности
    static List<List<Integer>> build(Scanner in, int n, int m) {
        List<List<Integer>> adj = new ArrayList<>();
        while (adj.size() < n) adj.add(new ArrayList<>());
        for (int k = 0; k < m; k++) {
            int p = in.nextInt();
            int q = in.nextInt();
            adj.get(p).add(q); // ребро в обе стороны
            adj.get(q).add(p);
        }
        return adj;
    }

    /**
     * Обход в ширину.
     * @param adj список смежности
     * @param src стартовая вершина
     */
    static List<Integer> traverse(List<List<Integer>> adj, int src) {
        List<Integer> out = new ArrayList<>();
        Set<Integer> vis = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        vis.add(src);
        queue.addLast(src);
        while (!queue.isEmpty()) {
            int cur = queue.pollFirst();
            out.add(cur);
            List<Integer> nbrs = new ArrayList<>(adj.get(cur));
            Collections.sort(nbrs);
            for (int t : nbrs) {
                if (vis.contains(t)) {
                    continue;
                }
                vis.add(t);
                queue.addLast(t);
            }
        }
        return out;
    }
}