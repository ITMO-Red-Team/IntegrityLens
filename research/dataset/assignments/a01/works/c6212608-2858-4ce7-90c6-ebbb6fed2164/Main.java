import java.util.*;
import static java.lang.Math.*;
import java.io.*;
public class S {
       
      public static void main(String args[])throws IOException{
              BufferedReader br = new  BufferedReader(new InputStreamReader(System.in));
              int test = Integer.parseInt(br.readLine());
              while(test > 0){
                    test--;
                        int n = Integer.parseInt(br.readLine());
                        int a[][] = new int[n][2];
                        for(int i = 0; i < n; i++){
                              StringTokenizer st = new StringTokenizer(br.readLine());
                              a[i][0] = Integer.parseInt(st.nextToken());
                              a[i][1] = Integer.parseInt(st.nextToken());
                        }
                        Map<Integer, List<Integer>> g = new HashMap<Integer, List<Integer>>();
                        for(int i = 0; i < n-1; i++){
                              StringTokenizer st = new StringTokenizer(br.readLine());
                              int u = Integer.parseInt(st.nextToken());
                              int v = Integer.parseInt(st.nextToken());
                              u--;
                              v--;
                              if(!g.containsKey(u))g.put(u, new ArrayList<Integer>());
                              g.get(u).add(v);
                              if(!g.containsKey(v))g.put(v, new ArrayList<Integer>());
                              g.get(v).add(u);
                        }
                        solve(n, a, g);
                    //System.out.println(s + " " + tar);
                    //StringTokenizer st = new StringTokenizer(br.readLine());
                    //int n = Integer.parseInt(st.nextToken());
                    //int l = Integer.parseInt(st.nextToken());
                    //st = new StringTokenizer(br.readLine());
                    //int a[] = new int[n];
                    //for(int i = 0; i < n; i++){
                  //      a[i] = Integer.parseInt(st.nextToken());
                    //}
             }
      }
      
      public static void solve(int n, int a[][], Map<Integer, List<Integer>> g){
            Map<Integer, long[]> dp = new HashMap<Integer, long[]>();
            boolean visited[] =  new boolean[n];
            long ans = 0;
            visited[0] = true;
            long part[] = dfs(a, g, 0, visited, dp);
            ans = Math.max(part[0], part[1]);
            System.out.println(ans);
      }
      
      public static long[] dfs(int a[][], Map<Integer, List<Integer>> g, int node, boolean visited[], Map<Integer, long[]> dp){
            if(dp.containsKey(node)){
                  return dp.get(node);
            }
            List<Integer> children = g.get(node);
            if(children == null)return new long[]{0, 0};
            long ansL = 0;
            long ansR = 0;
            for(int child : children){
                  if(!visited[child]){
                        visited[child] = true;
                        long sol[] = dfs(a, g, child, visited, dp);
                        ansL += Math.max(sol[0] + Math.abs(a[node][0] - a[child][0]), sol[1] + Math.abs(a[node][0] - a[child][1]));
                        ansR += Math.max(sol[0] + Math.abs(a[node][1] - a[child][0]), sol[1] + Math.abs(a[node][1] - a[child][1]));
                  }
            }
            long ans[] = new long[]{ansL, ansR};
            dp.put(node, ans);
            return ans;
            
      }
      
      public static int gcd(int a, int b){
            if(b == 0)return a;
            return gcd(b, a%b);
      }
}