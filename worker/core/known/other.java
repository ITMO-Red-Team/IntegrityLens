import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static List<List<Integer>> graph = new ArrayList<>();

    static int[] distances(int source) {
        int[] dist = new int[graph.size()];
        java.util.Arrays.fill(dist, -1);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        dist[source] = 0;
        queue.add(source);
        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (int next : graph.get(node)) {
                if (dist[next] < 0) {
                    dist[next] = dist[node] + 1;
                    queue.add(next);
                }
            }
        }
        return dist;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String[] header = reader.readLine().trim().split(" ");
        int vertices = Integer.parseInt(header[0]);
        int edges = Integer.parseInt(header[1]);
        for (int i = 0; i < vertices; i++) {
            graph.add(new ArrayList<>());
        }
        for (int i = 0; i < edges; i++) {
            String[] edge = reader.readLine().trim().split(" ");
            int from = Integer.parseInt(edge[0]) - 1;
            int to = Integer.parseInt(edge[1]) - 1;
            graph.get(from).add(to);
            graph.get(to).add(from);
        }
        StringBuilder out = new StringBuilder();
        for (int value : distances(0)) {
            out.append(value).append('\n');
        }
        System.out.print(out);
    }
}
