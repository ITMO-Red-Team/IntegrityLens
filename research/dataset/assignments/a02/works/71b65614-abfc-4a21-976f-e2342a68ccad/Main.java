import java.io.*;
import java.util.*;

public class A {
    //--------------------------INPUT READER---------------------------------//
    static class fs {
        public BufferedReader br;
        StringTokenizer st = new StringTokenizer("");

        public fs() { this(System.in); }
        public fs(InputStream is) {
            br = new BufferedReader(new InputStreamReader(is));
        }
        String next() {
            while (!st.hasMoreTokens()) {
                try { st = new StringTokenizer(br.readLine()); }
                catch (IOException e) { e.printStackTrace(); }
            }
            return st.nextToken();
        }

        int ni() { return Integer.parseInt(next()); }
        long nl() { return Long.parseLong(next()); }
        double nd() { return Double.parseDouble(next()); }
        String ns() { return next(); }

        int[] na(long nn) {
            int n = (int) nn;
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = ni();
            return a;
        }

        long[] nal(long nn) {
            int n = (int) nn;
            long[] l = new long[n];
            for(int i = 0; i < n; i++) l[i] = nl();
            return l;
        }
    }
    //-----------------------------------------------------------------------//
    static int t = 0;
    //---------------------------PRINTER-------------------------------------//
    static class Printer {
        static PrintWriter w;
        public Printer() {this(System.out);}
        public Printer(OutputStream os) {
            w = new PrintWriter(os);
        }
        public void p(int i) {w.println(i);}
        public void p(long l) {w.println(l);}
        public void p(double d) {w.println(d);}
        public void p(String s) { w.println(s);}
        public void pr(int i) {w.print(i);}
        public void pr(long l) {w.print(l);}
        public void pr(double d) {w.print(d);}
        public void pr(String s) { w.print(s);}
        public void pl() {w.println();}
        public void close() {w.close();}
    }
    //-----------------------------------------------------------------------//

    //--------------------------VARIABLES------------------------------------//
    static fs sc = new fs();
    static OutputStream outputStream = System.out;
    static Printer w = new Printer(outputStream);
    static long lma = Long.MAX_VALUE, lmi = Long.MIN_VALUE;
    static long ima = Integer.MAX_VALUE, imi = Integer.MIN_VALUE;
    static long mod = 1000000007;
    //-----------------------------------------------------------------------//

    //--------------------------ADMIN_MODE-----------------------------------//
    private static void ADMIN_MODE() throws IOException {
        if (System.getProperty("ONLINE_JUDGE") == null) {
            w = new Printer(new FileOutputStream("output.txt"));
            sc = new fs(new FileInputStream("input.txt"));
        }
    }
    //-----------------------------------------------------------------------//
    static int tt = 0;
    //----------------------------START--------------------------------------//
    public static void main(String[] args)
            throws IOException {

        ADMIN_MODE();

        t = sc.ni();while(t-->0) {
            tt++;
            solve();
        }


        w.close();
    }

    static void solve() throws IOException {
        int n = sc.ni();
        String s1 = sc.ns();
        String s2 = sc.ns();
        char[] strr = s1.toCharArray();
        char[] strr2 = s2.toCharArray();
        HashSet<Integer> lia = new HashSet<>(), lib = new HashSet<>();
        for(int i = 0; i < n; i++) {
            if(strr[i]=='1') lia.add(i);
            else lib.add(i);
        }
        HashSet<Integer> liaa = new HashSet<>();
        for(int i = 0; i < n; i++) {
            if(strr2[i]=='1') liaa.add(i);
        }
        if(!(lia.size() == liaa.size() || lib.size()+1 == liaa.size())) {
            w.p(-1);
            return;
        }
        int ac = 0, bc = 0;
        for(int i: lia) {
            if(liaa.contains(i)) ac++;
        }
        bc = liaa.size()-ac;
        if(lia.size() == liaa.size() && lia.size() == ac) {
            w.p(0);
            return;
        }

        int nac = lia.size()-ac;
        int nbc = lib.size()-bc;

        long ansa = ac != 0 && (ac-1 == nbc)? Math.abs(ac-1+nbc): ima;
        long ansb = nac != 0 && (nac-1 == bc-1)? Math.abs(nac-1+bc): ima;


        if(ansa == ima && ansb == ima) {
            w.p(-1);
            return;
        }

        w.p(Math.min(ansa, ansb)+1);
    }

}