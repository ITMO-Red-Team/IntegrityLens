import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Practice {
    static int[][] vals;
    static ArrayList<ArrayList<Integer>> adjList;
    static long[][] ans;
    public static void main(String[] args) throws IOException {
        BufferedReader bu=new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(bu.readLine());
        while (t-- > 0) {
            Integer n = Integer.parseInt(bu.readLine());
            vals = new int[2][n];
            adjList = new ArrayList<>();
            for(int i=0;i<n;i++){
                String st[]=bu.readLine().split(" ");
                vals[0][i] = Integer.parseInt(st[0]);
                vals[1][i] = Integer.parseInt(st[1]);
                adjList.add(new ArrayList<>());
            }
            for(int i=0;i<n-1;i++){
                String st[]=bu.readLine().split(" ");
                int source = Integer.parseInt(st[0]);; int dest = Integer.parseInt(st[1]);;
                adjList.get(source-1).add(dest-1);
                adjList.get(dest-1).add(source-1);
            }
            ans = new long[2][n];
            DFS(0, adjList, -1, ans, vals);
            System.out.println(Math.max(ans[0][0], ans[1][0]));
        }
    }

    private static void DFS(Integer current, ArrayList<ArrayList<Integer>> adjList, Integer prev, long[][] ans, int[][] vals) {
        ans[0][current] = 0L; ans[1][current] = 0L;
        for(Integer node: adjList.get(current)){
            if(node.equals(prev)) continue;
            DFS(node, adjList, current, ans, vals);
            ans[0][current] += Math.max(ans[0][node] + Math.abs(vals[0][current] - vals[0][node]), ans[1][node] + Math.abs(vals[0][current] - vals[1][node]));
            ans[1][current] += Math.max(ans[0][node] + Math.abs(vals[1][current] - vals[0][node]) , ans[1][node] + Math.abs(vals[1][current] - vals[1][node]));
        }
    }
}

