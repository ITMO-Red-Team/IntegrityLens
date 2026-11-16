import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main3 {
    public static void main(String[] args) {
        FastReader in = new FastReader();

        int t = in.nextInt();
        while(t-- > 0) {
            int n = in.nextInt();
            int m = in.nextInt();
            int x = in.nextInt();

            List<Block> arr = new ArrayList<>();
            for(int i = 0; i < n; i++) {
                arr.add(new Block(in.nextInt(), i));
            }

            if(m == 1) {
                System.out.println("YES");
                for(int i = 0; i < n; i++) {
                    System.out.print(1 + " ");
                }
                System.out.println();
                continue;
            }
            arr.sort(Comparator.reverseOrder());

            long[] towers = new long[m];
            int ind = 0;
            boolean right = true;

            int[] ans = new int[n];

            for(int i = 0; i < n; i++) {
                int nextInd;
                if(right) {
                    if(ind == m - 1) {
                        right = false;
                        nextInd = ind - 1;
                    } else {
                        nextInd = ind + 1;
                    }
                } else {
                    if(ind == 0) {
                        right = true;
                        nextInd = ind + 1;
                    } else {
                        nextInd = ind - 1;
                    }
                }

                if(towers[ind] <= towers[nextInd]) {
                    towers[ind] += arr.get(i).weight;

                    ans[arr.get(i).index] = ind + 1;
                } else {
                    towers[nextInd] += arr.get(i).weight;

                    ans[arr.get(i).index] = nextInd + 1;
                    ind = nextInd;
                }
            }

            boolean success = true;
            for(int i = 0; i < m - 1; i++) {
                if(Math.abs(towers[i] - towers[i + 1]) > x) {
                    success = false;
                    break;
                }
            }

            if(success) {
                System.out.println("YES");
                for(int i = 0; i < n; i++) {
                    System.out.print(ans[i] + " ");
                }
                System.out.println();
            } else {
                System.out.println("NO");
            }

        }
    }

    static class Block implements Comparable<Block> {
        int weight;
        int index;

        public Block(int weight, int index) {
            this.weight = weight;
            this.index = index;
        }

        @Override
        public int compareTo(Block o) {
            return Integer.compare(this.weight, o.weight);
        }
    }

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
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
    }
}
