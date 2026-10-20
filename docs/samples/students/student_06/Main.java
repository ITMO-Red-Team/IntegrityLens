import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        List<List<Integer>> graph = readGraph(in, n, m);
        System.out.println(bfs(graph, 0));
    }

    static List<Integer> bfs(List<List<Integer>> nb, int st) {
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
    }
}