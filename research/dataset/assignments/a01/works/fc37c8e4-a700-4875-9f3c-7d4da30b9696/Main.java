import java.util.*;
import java.io.*;

public class Solution {

    static LinkedList<Integer> graph[];
    static long key[][];
    static long value[][];

    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);
        StringBuffer out=new StringBuffer();
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

        int t=Integer.parseInt(br.readLine());
        String input[];

        OUTER:
        while(t--!=0) {

            int n=Integer.parseInt(br.readLine());

            value=new long[n][2];
            key=new long[n][2];

            for(int i=0; i<n; i++) {
                input= br.readLine().split("\\s");
                for(int j=0; j<2; j++) {
                    key[i][j] = Integer.parseInt(input[j]);
                }
            }

            graph=new LinkedList[n];
            for(int i=0; i<n; i++)
                graph[i]=new LinkedList();

            for(int i=1; i<n; i++) {
                input= br.readLine().split("\\s");
                int u=Integer.parseInt(input[0])-1, v=Integer.parseInt(input[1])-1;
                graph[u].add(v);
                graph[v].add(u);
            }

            int root=-1;
            for(int i=0; i<n; i++)
                if(graph[i].size()==1) {
                    root=i;
                    break;
                }

            solve(root, -1);

            out.append(Math.max(value[root][0], value[root][1])+"\n");
        }

        System.out.print(out);
    }

    private static void solve(int vertex, int parent) {

        for(int child: graph[vertex]) {
            if(child!=parent) {
                solve(child, vertex);
                for(int i=0; i<2; i++) {
                    long max=Long.MIN_VALUE;
                    for(int j=0; j<2; j++) {
                        max=Math.max(max, Math.abs(key[vertex][i]-key[child][j])+value[child][j]);
                    }
                    value[vertex][i]+=max;
                }
            }
        }

//        System.out.println(vertex+" "+Arrays.toString(key[vertex])+" "+Arrays.toString(value[vertex]));
    }
}