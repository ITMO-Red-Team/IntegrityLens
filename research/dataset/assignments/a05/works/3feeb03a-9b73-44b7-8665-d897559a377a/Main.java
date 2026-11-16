import java.io.*;
import java.util.*;

public class A1 {
    static final Reader s = new Reader();
    static final PrintWriter out = new PrintWriter(System.out);

    public static void main(String[] args) throws IOException {
        int t  = s.nextInt();
//        int t=1;
        for(int i=1; i<=t; ++i) {
//            out.print("Case #"+i+": ");
            new Solver();
        }
        out.close();
    }
    static class Solver {
    	Solver() {
    		int n = s.nextInt();
    		String[] a = new String[n];
    		int[][] cnt = new int[n][5];
    		for(int i=0;i<n;i++) {
    			char[] g = s.next().toCharArray();
    			for(int j=0;j<g.length;j++) {
    				cnt[i][g[j]-'a']++;
    			}
    		}
    		int g=0;
    		for(int i=0;i<5;i++) {
    			List<Integer> l = new ArrayList<>();
    			for(int j=0;j<n;j++) {
    				int sum=0;
    				for(int k=0;k<5;k++) {
    					if(i==k)sum+=cnt[j][k];
    					else sum-=cnt[j][k];
    				}
    				l.add(sum);
    			}
    			Collections.sort(l,Collections.reverseOrder());
    			int f=0;
    			int v=0;
    			for(int h:l) {
    				v += h;
    				if(v > 0)f++;
    				else break;
    			}
    			g = Math.max(g, f);
    		}
    		out.println(g);
    	}
    }
    static class Reader {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        String next() {
            while(st==null||!st.hasMoreTokens()) {
                try {
                    st=new StringTokenizer(in.readLine());
                } catch(Exception e) {}
            }
            return st.nextToken();
        }
        int nextInt() {
            return Integer.parseInt(next());
        }
        long nextLong() {
            return Long.parseLong(next());
        }
    }

}
