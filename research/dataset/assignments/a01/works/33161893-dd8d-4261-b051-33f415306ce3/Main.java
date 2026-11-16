import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.StringTokenizer;

public class A
{
    static int[][] A;
    static long[][] B;
    static ArrayList<ArrayList<Integer>> ar;
    public static void main(String[] args)
    {
        CP sc =new CP();
        int tt = sc.nextInt();
        while (tt-- > 0) {
            int n = sc.nextInt();
            ar = new ArrayList<>();
            for(int i=0;i<n+1;i++)
                ar.add(new ArrayList<>());
            A = new int[n+1][2];
            B = new long[n+1][2];
            for(int i=1;i<n+1;i++) {
                A[i][0] = sc.nextInt();
                A[i][1] = sc.nextInt();
            }
            for(int i=0;i<n-1;i++){
                int u=  sc.nextInt();
                int v = sc.nextInt();
                ar.get(u).add(v);
                ar.get(v).add(u);
            }
//            for(int i=0;i<n;i++) {
//                for (int j = 0; j < 2; j++)
//                    System.out.print(A[i][j] + " ");
//                System.out.println();
//            }
//            for(int i=0;i<ar.size();i++)
//                System.out.println(ar.get(i));
            //System.out.println(ar.get(1).get(0));
            //System.out.println(ar.get(3).size());
            DFS(1, -1);
            System.out.println(Math.max(B[1][0], B[1][1]));
        }
    }
    static long max = Long.MIN_VALUE;
    static void DFS(int curr, int parent){
        B[curr][0] =0; B[curr][1] = 0;
        for(int i : ar.get(curr)){
            if(parent!=i) {
                DFS(i, curr);
                B[curr][0] += Math.max(B[i][0] + Math.abs(A[curr][0] - A[i][0]), B[i][1] + Math.abs(A[curr][0] - A[i][1]));
                B[curr][1] += Math.max(B[i][0] + Math.abs(A[curr][1] - A[i][0]), B[i][1] + Math.abs(A[curr][1] - A[i][1]));

            }
        }
        //System.out.println(B[curr][0]+" "+B[curr][1]);
    }
    /*****************************************************************************/
    static class CP
    {
        BufferedReader bufferedReader;
        StringTokenizer stringTokenizer;
        public CP() {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        }
        int nextInt() {
            return Integer.parseInt(NNNN());
        }
        long nextLong() {
            return Long.parseLong(NNNN());
        }
        double nextDouble() {
            return Double.parseDouble(NNNN());
        }
        String NNNN() {
            while (stringTokenizer == null || !stringTokenizer.hasMoreElements()) {
                try {
                    stringTokenizer = new StringTokenizer(bufferedReader.readLine());
                }
                catch (IOException  e) {
                    e.printStackTrace();
                }
            }
            return stringTokenizer.nextToken();
        }
        String nextLine() {
            String spl = "";
            try {
                spl = bufferedReader.readLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return spl;
        }
    }
    /*****************************************************************************/
}
