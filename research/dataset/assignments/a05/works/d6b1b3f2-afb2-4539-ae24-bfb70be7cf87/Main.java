import java.util.StringTokenizer;
import java.io.*;

public class CF_1551c{
    public static final void main(String[] args){
        Kattio io= new Kattio();
        int t= io.getInt();
        while(t-->0){
            int n= io.getInt();
            int[][] ps= new int[5][n];
            for(int i=0; i<n; i++){
                String w= io.getWord();
                int len= w.length();
                // count letters
                for(int j=0; j<len; j++)
                    ps[w.charAt(j)-'a'][i]++;   
                // calculate diffs letter-!letter
                // e.g. a-!a = a-(a+b+c+d+e-a) = a-(len-a) = a + (a-len) = 2a-len
                for(int k=0; k<5; k++)
                    ps[k][i]+= ps[k][i]-len;   
            }
            //sort diffs
            for(int k=0; k<5; k++)
                mergeSort(ps[k]);
            //calculate prefix sums of diffs (until they're non-positive)
            //start from the end as sort is ascending
            //pick largest index out of 5 letter at which sum of diffs is positive
            int max= 0;
            for(int k=0; k<5; k++){
                if(ps[k][n-1]<=0) continue;
                if(max==0) max= 1;
                for(int i=2; i<=n; i++){
                    ps[k][n-i]+= ps[k][n-i+1];
                    if(ps[k][n-i]<=0) break;
                    if(i>max) max= i;
                }
            }
            io.println(max);
        }
        io.close();
    }

    // using mergeSort to avoid Java quicksort TLE hacks
    static void mergeSort(int arr[]){
        int n= arr.length;
        for (int sz= 1; sz<=n-1; sz=2*sz){
            for (int l= 0; l<n-1; l+=2*sz){
                int m= Math.min(l + sz-1, n-1);
                int r= Math.min(l + 2*sz-1, n-1);
                int n1= m-l+1, n2= r-m;
                int L[] = new int[n1];
                for (int i= 0; i<n1; i++) L[i]= arr[l+i];
                int R[] = new int[n2];
                for (int j= 0; j<n2; j++) R[j]= arr[m+1+j];
                int i= 0, j= 0, k= l;
                for(;i<n1 && j<n2; k++){
                    if(L[i]<=R[j]){arr[k]= L[i]; i++;}
                    else{arr[k] = R[j]; j++;}
                }
                for(;i<n1; i++, k++) arr[k]= L[i];
                for(;j<n2; j++, k++) arr[k]= R[j];
            }
        }
    }    
    
    static class Kattio extends PrintWriter {
        private BufferedReader r;
        private String line, token;
        private StringTokenizer st;
    
        public Kattio(){this(System.in);}
        public Kattio(InputStream i){
        super(new BufferedOutputStream(System.out));
            r= new BufferedReader(new InputStreamReader(i));
        }
        public Kattio(InputStream i, OutputStream o){
        super(new BufferedOutputStream(o));
            r= new BufferedReader(new InputStreamReader(i));
        }
        public boolean isLineEnd(){
            return !st.hasMoreTokens();
        }
        public boolean hasMoreTokens(){
            return peekToken()!=null;
        }
        public int getInt(){
            return Integer.parseInt(nextToken());
        }
        public double getDouble(){ 
            return Double.parseDouble(nextToken());
        }
        public long getLong(){
            return Long.parseLong(nextToken());
        }
        public String getWord(){
            return nextToken();
        }
        public String getLine(){
            try{if(!st.hasMoreTokens())
                return r.readLine();
            }catch(IOException e){}
            return null;
        }
        private String peekToken(){
            if(token==null) try {
                while(st==null || !st.hasMoreTokens()) {
                    line= r.readLine();
                    if(line==null) return null;
                    st= new StringTokenizer(line);
                }
                token= st.nextToken();
            }catch(IOException e){}
            return token;
        }
        private String nextToken() {
            String ans= peekToken();
            token= null;
            return ans;
        }
    }
}
