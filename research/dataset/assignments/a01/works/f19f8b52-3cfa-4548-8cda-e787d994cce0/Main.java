import java.util.*;
import java.io.*;

public class Solution {
    public static void main(String str[]) throws IOException{
        Reader sc = new Reader();
        int t = sc.nextInt();
        while(t-->0) {
            int n = sc.nextInt();
            ArrayList<Tree> tt = new ArrayList<>();
            for(int i=0;i<n;i++){
                Tree temp = new Tree(sc.nextInt(), sc.nextInt(), i);
                tt.add(temp);
            }
            for(int i=0;i<n-1;i++){
                int x = sc.nextInt()-1;
                int y = sc.nextInt()-1;
                tt.get(x).al.add(tt.get(y));
                tt.get(y).al.add(tt.get(x));
            }
            Pair p = traversal(tt.get(0), null);
            System.out.println(Math.max(p.av, p.bv));
        }
    }
    static Pair traversal(Tree node, Tree Parent){
        if(node.al.size()==1 && Parent!=null){
            Pair p = new Pair(0,0);
            return p;
        }
        long lt = 0;
        long rt = 0;
        for(Tree t: node.al){
            if(t!=Parent){
                Pair p = traversal(t, node);
                long ll = Math.abs(node.l- t.l) + p.av;
                ll = Math.max(ll, Math.abs(node.l-t.r)+p.bv);
                long rr = Math.abs(node.r- t.l) + p.av;
                rr = Math.max(rr, Math.abs(node.r-t.r)+p.bv);
                lt+=ll;
                rt+=rr;
            }
        }
        Pair p = new Pair(lt, rt);
        return p;
    }
    static class Reader {
        final private int BUFFER_SIZE = 1 << 16;
        private DataInputStream din;
        private byte[] buffer;
        private int bufferPointer, bytesRead;

        public Reader()
        {
            din = new DataInputStream(System.in);
            buffer = new byte[BUFFER_SIZE];
            bufferPointer = bytesRead = 0;
        }

        public Reader(String file_name) throws IOException
        {
            din = new DataInputStream(
                    new FileInputStream(file_name));
            buffer = new byte[BUFFER_SIZE];
            bufferPointer = bytesRead = 0;
        }

        public String readLine() throws IOException
        {
            byte[] buf = new byte[64]; // line length
            int cnt = 0, c;
            while ((c = read()) != -1) {
                if (c == '\n') {
                    if (cnt != 0) {
                        break;
                    }
                    else {
                        continue;
                    }
                }
                buf[cnt++] = (byte)c;
            }
            return new String(buf, 0, cnt);
        }

        public int nextInt() throws IOException
        {
            int ret = 0;
            byte c = read();
            while (c <= ' ') {
                c = read();
            }
            boolean neg = (c == '-');
            if (neg)
                c = read();
            do {
                ret = ret * 10 + c - '0';
            } while ((c = read()) >= '0' && c <= '9');

            if (neg)
                return -ret;
            return ret;
        }

        public long nextLong() throws IOException
        {
            long ret = 0;
            byte c = read();
            while (c <= ' ')
                c = read();
            boolean neg = (c == '-');
            if (neg)
                c = read();
            do {
                ret = ret * 10 + c - '0';
            } while ((c = read()) >= '0' && c <= '9');
            if (neg)
                return -ret;
            return ret;
        }

        public double nextDouble() throws IOException
        {
            double ret = 0, div = 1;
            byte c = read();
            while (c <= ' ')
                c = read();
            boolean neg = (c == '-');
            if (neg)
                c = read();

            do {
                ret = ret * 10 + c - '0';
            } while ((c = read()) >= '0' && c <= '9');

            if (c == '.') {
                while ((c = read()) >= '0' && c <= '9') {
                    ret += (c - '0') / (div *= 10);
                }
            }

            if (neg)
                return -ret;
            return ret;
        }

        private void fillBuffer() throws IOException
        {
            bytesRead = din.read(buffer, bufferPointer = 0,
                    BUFFER_SIZE);
            if (bytesRead == -1)
                buffer[0] = -1;
        }

        private byte read() throws IOException
        {
            if (bufferPointer == bytesRead)
                fillBuffer();
            return buffer[bufferPointer++];
        }

        public void close() throws IOException
        {
            if (din == null)
                return;
            din.close();
        }
    }

}
class Pair{
    long av;
    long bv;
    Pair(long av, long bv){
        this.av = av;
        this.bv = bv;
    }

}
class Tree{
    int l;
    int r;
    int i;
    ArrayList<Tree> al = new ArrayList<>();
    Tree(int l, int r, int i){
        this.l = l;
        this.r = r;
        this.i = i;
    }

}


