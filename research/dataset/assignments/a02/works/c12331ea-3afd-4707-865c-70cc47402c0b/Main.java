//package codeforces.globalround18;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;

//Think through the entire logic before jump into coding!
//If you are out of ideas, take a guess! It is better than doing nothing!
//Read both C and D, it is possible that D is easier than C for you!

//Be aware of integer overflow!
//If you find an answer and want to return immediately, don't forget to flush before return!

public class C {
    static InputReader in;
    static PrintWriter out;

    public static void main(String[] args) {
        //initReaderPrinter(true);
        initReaderPrinter(false);
        solve(in.nextInt());
        //solve(1);        
    }

    static void solve(int testCnt) {
        for (int testNumber = 0; testNumber < testCnt; testNumber++) {
            int n = in.nextInt();
            char[] a = in.next().toCharArray(), b = in.next().toCharArray();
            int match0 = 0, match1 = 0, mismatch10 = 0, mismatch01 = 0;
            for(int i = 0; i < n; i++) {
                if(a[i] == b[i]) {
                    if(a[i] == '0') match0++;
                    else match1++;
                }
                else {
                    if(a[i] == '0') mismatch01++;
                    else mismatch10++;
                }
            }
            if(mismatch01 + mismatch10 == 0) out.println(0);
            else {
                if(match1 - match0 == 1 && mismatch01 == mismatch10) {
                    out.println(min(match0 + match1, mismatch01 + mismatch10));
                }
                else if(match1 - match0 == 1) {
                    out.println(match0 + match1);
                }
                else if(mismatch01 == mismatch10) {
                    out.println(mismatch01 + mismatch10);
                }
                else out.println(-1);
            }
        }
        out.close();
    }

    static void initReaderPrinter(boolean test) {
        if (test) {
            try {
                in = new InputReader(new FileInputStream("src/input.in"));
                out = new PrintWriter(new FileOutputStream("src/output.out"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            in = new InputReader(System.in);
            out = new PrintWriter(System.out);
        }
    }

    static class InputReader {
        BufferedReader br;
        StringTokenizer st;

        InputReader(InputStream stream) {
            try {
                br = new BufferedReader(new InputStreamReader(stream), 32768);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }

        String nextLine() {
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }

        Integer[] nextIntArray(int n) {
            Integer[] a = new Integer[n];
            for (int i = 0; i < n; i++) a[i] = nextInt();
            return a;
        }

        int[] nextIntArrayPrimitive(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = nextInt();
            return a;
        }

        int[] nextIntArrayPrimitiveOneIndexed(int n) {
            int[] a = new int[n + 1];
            for (int i = 1; i <= n; i++) a[i] = nextInt();
            return a;
        }

        Long[] nextLongArray(int n) {
            Long[] a = new Long[n];
            for (int i = 0; i < n; i++) a[i] = nextLong();
            return a;
        }

        long[] nextLongArrayPrimitive(int n) {
            long[] a = new long[n];
            for (int i = 0; i < n; i++) a[i] = nextLong();
            return a;
        }

        long[] nextLongArrayPrimitiveOneIndexed(int n) {
            long[] a = new long[n + 1];
            for (int i = 1; i <= n; i++) a[i] = nextLong();
            return a;
        }

        String[] nextStringArray(int n) {
            String[] g = new String[n];
            for (int i = 0; i < n; i++) g[i] = next();
            return g;
        }

        List<Integer>[] readGraphOneIndexed(int n, int m) {
            List<Integer>[] adj = new List[n + 1];
            for (int i = 0; i <= n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                int u = nextInt();
                int v = nextInt();
                adj[u].add(v);
                adj[v].add(u);
            }
            return adj;
        }

        List<Integer>[] readGraphZeroIndexed(int n, int m) {
            List<Integer>[] adj = new List[n];
            for (int i = 0; i < n; i++) {
                adj[i] = new ArrayList<>();
            }
            for (int i = 0; i < m; i++) {
                int u = nextInt() - 1;
                int v = nextInt() - 1;
                adj[u].add(v);
                adj[v].add(u);
            }
            return adj;
        }

        /*
            A more efficient way of building a graph using int[] instead of ArrayList to store each node's neighboring nodes.
            1-indexed.
         */
        int[][] buildGraph(int nodeCnt, int edgeCnt) {
            int[] end1 = new int[edgeCnt], end2 = new int[edgeCnt];
            int[] edgeCntForEachNode = new int[nodeCnt + 1], idxForEachNode = new int[nodeCnt + 1];
            for (int i = 0; i < edgeCnt; i++) {
                int u = in.nextInt(), v = in.nextInt();
                edgeCntForEachNode[u]++;
                edgeCntForEachNode[v]++;
                end1[i] = u;
                end2[i] = v;
            }
            int[][] adj = new int[nodeCnt + 1][];
            for (int i = 1; i <= nodeCnt; i++) {
                adj[i] = new int[edgeCntForEachNode[i]];
            }
            for (int i = 0; i < edgeCnt; i++) {
                adj[end1[i]][idxForEachNode[end1[i]]] = end2[i];
                idxForEachNode[end1[i]]++;
                adj[end2[i]][idxForEachNode[end2[i]]] = end1[i];
                idxForEachNode[end2[i]]++;
            }
            return adj;
        }
    }
}