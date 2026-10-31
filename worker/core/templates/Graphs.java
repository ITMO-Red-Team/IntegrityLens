import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

class Graphs {
    static final int INF = Integer.MAX_VALUE;

    static List<List<Integer>> readGraph(int n, int[][] edges) {
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            graph.get(edge[0]).add(edge[1]);
            graph.get(edge[1]).add(edge[0]);
        }
        return graph;
    }

    static int[] bfs(List<List<Integer>> graph, int start) {
        int[] dist = new int[graph.size()];
        Arrays.fill(dist, -1);
        Queue<Integer> queue = new LinkedList<>();
        dist[start] = 0;
        queue.add(start);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : graph.get(u)) {
                if (dist[v] == -1) {
                    dist[v] = dist[u] + 1;
                    queue.add(v);
                }
            }
        }
        return dist;
    }

    static void bfsVisited(ArrayList<Integer>[] graph, int start, boolean[] visited) {
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        visited[start] = true;
        queue.offer(start);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : graph[u]) {
                if (!visited[v]) {
                    visited[v] = true;
                    queue.offer(v);
                }
            }
        }
    }

    static int[][] gridBfs(char[][] grid, int sx, int sy) {
        int n = grid.length;
        int m = grid[0].length;
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        int[][] dist = new int[n][m];
        for (int[] row : dist) {
            Arrays.fill(row, -1);
        }
        Queue<int[]> queue = new LinkedList<>();
        dist[sx][sy] = 0;
        queue.add(new int[] {sx, sy});
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            for (int d = 0; d < 4; d++) {
                int x = cur[0] + dx[d];
                int y = cur[1] + dy[d];
                if (x >= 0 && y >= 0 && x < n && y < m && grid[x][y] != '#' && dist[x][y] == -1) {
                    dist[x][y] = dist[cur[0]][cur[1]] + 1;
                    queue.add(new int[] {x, y});
                }
            }
        }
        return dist;
    }

    static void dfs(List<List<Integer>> graph, int u, boolean[] visited) {
        visited[u] = true;
        for (int v : graph.get(u)) {
            if (!visited[v]) {
                dfs(graph, v, visited);
            }
        }
    }

    static void dfsStack(List<List<Integer>> graph, int start, boolean[] visited) {
        Stack<Integer> stack = new Stack<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) {
                continue;
            }
            visited[u] = true;
            for (int v : graph.get(u)) {
                if (!visited[v]) {
                    stack.push(v);
                }
            }
        }
    }

    static void dfsTree(ArrayList<Integer>[] graph, int u, int parent, int[] depth, int[] size) {
        size[u] = 1;
        for (int v : graph[u]) {
            if (v != parent) {
                depth[v] = depth[u] + 1;
                dfsTree(graph, v, u, depth, size);
                size[u] += size[v];
            }
        }
    }

    static int components(List<List<Integer>> graph) {
        boolean[] visited = new boolean[graph.size()];
        int count = 0;
        for (int i = 0; i < graph.size(); i++) {
            if (!visited[i]) {
                dfs(graph, i, visited);
                count++;
            }
        }
        return count;
    }

    static boolean isBipartite(List<List<Integer>> graph) {
        int[] color = new int[graph.size()];
        Arrays.fill(color, -1);
        for (int s = 0; s < graph.size(); s++) {
            if (color[s] != -1) {
                continue;
            }
            Queue<Integer> queue = new LinkedList<>();
            color[s] = 0;
            queue.add(s);
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (int v : graph.get(u)) {
                    if (color[v] == -1) {
                        color[v] = 1 - color[u];
                        queue.add(v);
                    } else if (color[v] == color[u]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static boolean hasCycle(List<List<Integer>> graph, int u, boolean[] visited, boolean[] onStack) {
        visited[u] = true;
        onStack[u] = true;
        for (int v : graph.get(u)) {
            if (!visited[v]) {
                if (hasCycle(graph, v, visited, onStack)) {
                    return true;
                }
            } else if (onStack[v]) {
                return true;
            }
        }
        onStack[u] = false;
        return false;
    }

    static List<Integer> topologicalSort(List<List<Integer>> graph) {
        int n = graph.size();
        int[] indegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (int v : graph.get(u)) {
                indegree[v]++;
            }
        }
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                queue.add(i);
            }
        }
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (int v : graph.get(u)) {
                indegree[v]--;
                if (indegree[v] == 0) {
                    queue.add(v);
                }
            }
        }
        return order;
    }

    static void topologicalDfs(List<List<Integer>> graph, int u, boolean[] visited, List<Integer> order) {
        visited[u] = true;
        for (int v : graph.get(u)) {
            if (!visited[v]) {
                topologicalDfs(graph, v, visited, order);
            }
        }
        order.add(u);
        if (order.size() == graph.size()) {
            Collections.reverse(order);
        }
    }

    static long[] dijkstra(List<List<int[]>> graph, int source) {
        long[] dist = new long[graph.size()];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[source] = 0;
        PriorityQueue<long[]> queue = new PriorityQueue<>(Comparator.comparingLong(x -> x[1]));
        queue.add(new long[] {source, 0});
        while (!queue.isEmpty()) {
            long[] top = queue.poll();
            int u = (int) top[0];
            if (top[1] > dist[u]) {
                continue;
            }
            for (int[] edge : graph.get(u)) {
                int v = edge[0];
                long next = dist[u] + edge[1];
                if (next < dist[v]) {
                    dist[v] = next;
                    queue.add(new long[] {v, next});
                }
            }
        }
        return dist;
    }

    static int[] dijkstraArray(int[][] weight, int source) {
        int n = weight.length;
        int[] dist = new int[n];
        boolean[] done = new boolean[n];
        Arrays.fill(dist, INF);
        dist[source] = 0;
        for (int i = 0; i < n - 1; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!done[j] && (u == -1 || dist[j] < dist[u])) {
                    u = j;
                }
            }
            done[u] = true;
            for (int v = 0; v < n; v++) {
                if (!done[v] && weight[u][v] != 0 && dist[u] != INF && dist[u] + weight[u][v] < dist[v]) {
                    dist[v] = dist[u] + weight[u][v];
                }
            }
        }
        return dist;
    }

    static long[] bellmanFord(int n, int[][] edges, int source) {
        long[] dist = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[source] = 0;
        for (int i = 0; i < n - 1; i++) {
            for (int[] edge : edges) {
                if (dist[edge[0]] != Long.MAX_VALUE && dist[edge[0]] + edge[2] < dist[edge[1]]) {
                    dist[edge[1]] = dist[edge[0]] + edge[2];
                }
            }
        }
        return dist;
    }

    static void floydWarshall(long[][] dist) {
        int n = dist.length;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
    }

    static long kruskal(int n, int[][] edges) {
        Arrays.sort(edges, (a, b) -> a[2] - b[2]);
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        long total = 0;
        for (int[] edge : edges) {
            int a = find(parent, edge[0]);
            int b = find(parent, edge[1]);
            if (a != b) {
                parent[a] = b;
                total += edge[2];
            }
        }
        return total;
    }

    static int find(int[] parent, int x) {
        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }
        return parent[x];
    }

    static long prim(List<List<int[]>> graph) {
        boolean[] used = new boolean[graph.size()];
        PriorityQueue<int[]> queue = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        queue.add(new int[] {0, 0});
        long total = 0;
        while (!queue.isEmpty()) {
            int[] top = queue.poll();
            if (used[top[0]]) {
                continue;
            }
            used[top[0]] = true;
            total += top[1];
            for (int[] edge : graph.get(top[0])) {
                if (!used[edge[0]]) {
                    queue.add(edge);
                }
            }
        }
        return total;
    }

    static int lca(int a, int b, int[][] up, int[] depth) {
        if (depth[a] < depth[b]) {
            int t = a;
            a = b;
            b = t;
        }
        for (int k = up.length - 1; k >= 0; k--) {
            if (depth[a] - (1 << k) >= depth[b]) {
                a = up[k][a];
            }
        }
        if (a == b) {
            return a;
        }
        for (int k = up.length - 1; k >= 0; k--) {
            if (up[k][a] != up[k][b]) {
                a = up[k][a];
                b = up[k][b];
            }
        }
        return up[0][a];
    }
}
