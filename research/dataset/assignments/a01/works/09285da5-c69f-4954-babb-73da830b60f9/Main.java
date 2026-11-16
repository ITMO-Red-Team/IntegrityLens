import java.util.*;


import java.io.*;
public class Test {
    static class Scan
    {
        private byte[] buf=new byte[1024];
        private int index;
        private InputStream in;
        private int total;
        public Scan()
        {
            in=System.in;
        }
        public int scan()throws IOException
        {
            if(total<0)
            throw new InputMismatchException();
            if(index>=total)
            {
                index=0;
                total=in.read(buf);
                if(total<=0)
                return -1;
            }
            return buf[index++];
        }
        public int scanInt()throws IOException
        {
            int integer=0;
            int n=scan();
            while(isWhiteSpace(n))
            n=scan();
            int neg=1;
            if(n=='-')
            {
                neg=-1;
                n=scan();
            }
            while(!isWhiteSpace(n))
            {
                if(n>='0'&&n<='9')
                {
                    integer*=10;
                    integer+=n-'0';
                    n=scan();
                }
                else throw new InputMismatchException();
            }
            return neg*integer;
        }
        public double scanDouble()throws IOException
        {
            double doub=0;
            int n=scan();
            while(isWhiteSpace(n))
            n=scan();
            int neg=1;
            if(n=='-')
            {
                neg=-1;
                n=scan();
            }
            while(!isWhiteSpace(n)&&n!='.')
            {
                if(n>='0'&&n<='9')
                {
                    doub*=10;
                    doub+=n-'0';
                    n=scan();
                }
                else throw new InputMismatchException();
            }
            if(n=='.')
            {
                n=scan();
                double temp=1;
                while(!isWhiteSpace(n))
                {
                    if(n>='0'&&n<='9')
                    {
                        temp/=10;
                        doub+=(n-'0')*temp;
                        n=scan();
                    }
                    else throw new InputMismatchException();
                }
            }
            return doub*neg;
        }
        public String scanString()throws IOException
        {
            StringBuilder sb=new StringBuilder();
            int n=scan();
            while(isWhiteSpace(n))
            n=scan();
            while(!isWhiteSpace(n))
            {
                sb.append((char)n);
                n=scan();
            }
            return sb.toString();
        }
        private boolean isWhiteSpace(int n)
        {
            if(n==' '||n=='\n'||n=='\r'||n=='\t'||n==-1)
            return true;
            return false;
        }
    }

    static class Print
    {
        private final BufferedWriter bw;
        public Print()
        {
            this.bw=new BufferedWriter(new OutputStreamWriter(System.out));
        }
        public void print(Object object)throws IOException
        {
            bw.append(""+object);
        }
        public void println(Object object)throws IOException
        {
            print(object);
            bw.append("\n");
        }
        public void close()throws IOException
        {
            bw.close();
        }
    }
    public static void main(String[] args) throws IOException{
        Scan scan = new Scan();
        Print print = new Print();
        int testcases = scan.scanInt();
        while(testcases-->0){
            int n = scan.scanInt();
            ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
            long range[][] = new long[n][2];
            for(int i=0; i<n; i++){
                
                range[i][0] = scan.scanInt();
                range[i][1] = scan.scanInt();
                list.add(new ArrayList<Integer>());
            }
            for(int i=0; i<n-1; i++){
                int a = scan.scanInt()-1;
                int b = scan.scanInt()-1;
                list.get(a).add(b);
                list.get(b).add(a);
            }
            long dp[][] = new long[n][2];
            // for(int i=0;i<n; i++){
            //     Arrays.fill(dp[i], 0);
            // }
            boolean vis[] = new boolean[n];
            dfs(list, vis, 0, dp, range);
            print.println(Math.max(dp[0][0], dp[0][1]));
            
        }
        print.close();
    }

    public static void dfs(ArrayList<ArrayList<Integer>> list, boolean[] vis, int parent, long dp[][] , long range[][]){
        vis[parent] = true;
        ArrayList<Integer> l = list.get(parent);

        for(int i=0; i<l.size(); i++){
            int next = l.get(i);
            if(vis[next] == false){
                dfs(list, vis, next, dp, range);
                dp[parent][0] += Math.max(Math.abs(range[parent][0]-range[next][0])+dp[next][0], Math.abs(range[parent][0]-range[next][1])+dp[next][1]); 
                dp[parent][1] += Math.max(Math.abs(range[parent][1]-range[next][0])+dp[next][0], Math.abs(range[parent][1]-range[next][1])+dp[next][1]); 
            }

        }
    }

    public static int gcd(int a, int b){
        if(b == 0){
            return a;
        }
        else{
            return gcd(b, a%b);
        }
    }
    
    

    
    
}
