export const COURSE_TEMPLATE = `public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> graph = readGraph(in, n, m);
        System.out.println(bfs(graph, 0));
    }`;

/** Собирает файл решения: импорты + стартовый код + методы студента. */
const solution = (imports: string, methods: string, bfs = "bfs", read = "readGraph") =>
  `${imports}\n\n${COURSE_TEMPLATE.replace("bfs(", `${bfs}(`).replace("readGraph(", `${read}(`)}\n\n${methods}\n}\n`;

export interface CorpusItem {
  id: string;
  fileName: string;
  submittedAt: string;
  code: string;
}

export const GROUND_TRUTH: [string, string][] = [
  ["S-014", "S-027"],
  ["S-008", "S-019"],
  ["S-014", "S-033"],
  ["S-027", "S-033"],
];

export const DEMO_CORPUS: CorpusItem[] = [
  {
    id: "S-014",
    fileName: "Main.java",
    submittedAt: "2026-10-12T21:40:00+03:00",
    code: solution(
      "import java.util.*;",
      `    static List<Integer> bfs(List<List<Integer>> graph, int start) {
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
    }`,
    ),
  },
  {
    id: "S-027",
    fileName: "Main.java",
    submittedAt: "2026-10-13T23:58:00+03:00",
    code: solution(
      "import java.util.ArrayDeque;\nimport java.util.ArrayList;\nimport java.util.Collections;\nimport java.util.Deque;\nimport java.util.List;\nimport java.util.Scanner;",
      `    static List<Integer> obhod(List<List<Integer>> g, int s) {
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
    }`,
      "obhod",
      "load",
    ),
  },
  {
    id: "S-033",
    fileName: "BfsSolution.java",
    submittedAt: "2026-10-13T19:12:00+03:00",
    code: solution(
      "import java.util.*;",
      `    static List<List<Integer>> readGraph(Scanner in, int n, int m) {
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
    }`,
    ),
  },
  {
    id: "S-003",
    fileName: "Main.java",
    submittedAt: "2026-10-10T14:05:00+03:00",
    code: solution(
      "import java.util.*;",
      `    static List<Integer> bfs(List<List<Integer>> adj, int root) {
        int n = adj.size();
        int[] line = new int[n];
        boolean[] used = new boolean[n];
        int head = 0, tail = 0;
        line[tail++] = root;
        used[root] = true;
        while (head < tail) {
            int cur = line[head++];
            for (int nxt : adj.get(cur)) {
                if (used[nxt]) continue;
                used[nxt] = true;
                line[tail++] = nxt;
            }
        }
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < tail; i++) res.add(line[i]);
        return res;
    }

    static List<List<Integer>> readGraph(Scanner in, int n, int m) {
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        while (m-- > 0) {
            int a = in.nextInt();
            int b = in.nextInt();
            adj.get(a).add(b);
            adj.get(b).add(a);
        }
        adj.forEach(Collections::sort);
        return adj;
    }`,
    ),
  },
  {
    id: "S-008",
    fileName: "Hw4.java",
    submittedAt: "2026-10-11T09:31:00+03:00",
    code: solution(
      "import java.util.*;",
      `    static List<List<Integer>> readGraph(Scanner in, int n, int m) {
        List<List<Integer>> g = new ArrayList<>();
        while (g.size() < n) g.add(new ArrayList<>());
        for (int e = 0; e < m; e++) {
            int x = in.nextInt();
            int y = in.nextInt();
            g.get(x).add(y);
            g.get(y).add(x);
        }
        return g;
    }

    static List<Integer> bfs(List<List<Integer>> g, int s) {
        List<Integer> order = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        ArrayDeque<Integer> dq = new ArrayDeque<>();
        seen.add(s);
        dq.addLast(s);
        while (!dq.isEmpty()) {
            int u = dq.pollFirst();
            order.add(u);
            List<Integer> nbrs = new ArrayList<>(g.get(u));
            Collections.sort(nbrs);
            for (int w : nbrs) {
                if (seen.contains(w)) {
                    continue;
                }
                seen.add(w);
                dq.addLast(w);
            }
        }
        return order;
    }`,
    ),
  },
  {
    id: "S-019",
    fileName: "Task4.java",
    submittedAt: "2026-10-13T22:47:00+03:00",
    code: solution(
      "import java.util.*;",
      `    // список смежности
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
    }`,
      "traverse",
      "build",
    ),
  },
  {
    id: "S-021",
    fileName: "Levels.java",
    submittedAt: "2026-10-12T17:20:00+03:00",
    code: solution(
      "import java.util.*;",
      `    static List<Integer> bfs(List<List<Integer>> graph, int start) {
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
    }`,
    ),
  },
  {
    id: "S-040",
    fileName: "Dz4.java",
    submittedAt: "2026-10-11T20:02:00+03:00",
    code: solution(
      "import java.util.*;",
      `    static List<Integer> bfs(List<List<Integer>> nb, int st) {
        var mark = new HashSet<Integer>(List.of(st));
        var dq = new ArrayDeque<Integer>(List.of(st));
        var ans = new ArrayList<Integer>();
        while (!dq.isEmpty()) {
            int x = dq.remove(); ans.add(x);
            for (int y : nb.get(x)) if (mark.add(y)) dq.add(y);
        }
        return ans;
    }

    static List<List<Integer>> readGraph(Scanner in, int n, int m) {
        List<TreeSet<Integer>> sets = new ArrayList<>();
        for (int i = 0; i < n; i++) sets.add(new TreeSet<>());
        for (int i = 0; i < m; i++) {
            int a = in.nextInt(), b = in.nextInt();
            sets.get(a).add(b); sets.get(b).add(a);
        }
        List<List<Integer>> res = new ArrayList<>();
        for (TreeSet<Integer> s : sets) res.add(new ArrayList<>(s));
        return res;
    }`,
    ),
  },
];