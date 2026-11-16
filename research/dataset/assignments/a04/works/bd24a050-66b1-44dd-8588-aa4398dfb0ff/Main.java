import java.io.*;
import java.util.*;

public class C14G {

    public static void main(String[] args) throws IOException {
        init_io();
        int t = nint();
        while(t-- > 0) {
            int N = nint(), M = nint(), X = nint();
            int[] ans = new int[N];
            TreeSet<Tower> pq = new TreeSet<>();
            Block[] blocks = new Block[N];
            for (int i = 0; i < N; i++) {
                blocks[i] = new Block(i, nint());
            }
            Arrays.sort(blocks);
            for (int i = 0; i < M; i++) {
                pq.add(new Tower(i));
            }
            for (int i = 0; i < N; i++) {
                Tower ct = pq.pollFirst();
                ans[blocks[i].idx] = ct.idx;
                ct.sum += blocks[i].height;
                pq.add(ct);
            }
            if (pq.last().sum - pq.first().sum > X) {
                out.println("NO");
            }
            else {
                out.println("YES");
                for (int i = 0; i < N; i++) {
                    out.print(ans[i]+1);
                    out.print(" ");
                }
                out.println();
            }
        }
        out.close();
    }
    static class Block implements Comparable<Block> {
        int idx, height;
        public Block(int idx, int height) {
            this.idx = idx; this.height = height;
        }
        public int compareTo(Block b) {
            return b.height - height;
        }
    }
    static class Tower implements Comparable<Tower> {
        int idx;
        int sum;
        public Tower(int idx) {
            this.idx = idx;
        }
        public int compareTo(Tower t) {
            return sum == t.sum ? idx - t.idx : sum - t.sum;
        }
    }
    static StreamTokenizer in;
    static PrintWriter out;
    static BufferedReader br;

    static int nint() throws IOException {
        in.nextToken();
        return (int) in.nval;
    }

    static void init_io() throws IOException {
        br = new BufferedReader(new InputStreamReader(System.in));
        in = new StreamTokenizer(br);
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
    }
}