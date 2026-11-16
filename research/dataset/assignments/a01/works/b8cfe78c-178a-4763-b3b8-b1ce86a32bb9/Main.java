
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;

public class Main {

    public static long[] ans(List<List<Integer>> edges, long[][] range, int root, boolean[] visited, PrintStream out) {
        if (visited[root]) {
            return new long[2];
        }
        visited[root] = true;
        long[] ans = new long[2];
        for (int x : edges.get(root)) {
            if (!visited[x]) {
                long[] temp = ans(edges, range, x, visited, out);
                ans[0] += Math.max(Math.abs(range[root][0] - range[x][0]) + temp[0], Math.abs(range[root][0] - range[x][1]) + temp[1]);
                ans[1] += Math.max(Math.abs(range[root][1] - range[x][0]) + temp[0], Math.abs(range[root][1] - range[x][1]) + temp[1]);
            }
        }
        return ans;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = new PrintStream(System.out);
        int t = Integer.parseInt(reader.readLine());
        while (t-->0) {
            int n = Integer.parseInt(reader.readLine());
            long[][] range = new long[n][2];
            for (int i = 0; i < n; i++) {
                String[] input = reader.readLine().split(" ");
                range[i][0] = Integer.parseInt(input[0]);
                range[i][1] = Integer.parseInt(input[1]);
            }
            List<List<Integer>> edges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                edges.add(new ArrayList<>());
            }
            for (int i = 0; i < n - 1; i++) {
                String[] input = reader.readLine().split(" ");
                int u = Integer.parseInt(input[0]) - 1, v = Integer.parseInt(input[1]) - 1;
                edges.get(u).add(v);
                edges.get(v).add(u);
            }
            int root = 0;
            for (int i = 0; i < n; i++) {
                if (edges.get(i).size() > 1) {
                    root = i;
                    break;
                }
                if (edges.get(i).size() == 1) {
                    root = i;
                }
            }
            long[] ans = ans(edges, range, root, new boolean[n], out);
            out.println(Math.max(ans[0], ans[1]));
        }
        out.close();
    }
}
