import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

public class C{
    private static int maxWords = 0;
    private static int[][] words;
    private static int n;
    private static int[] netwrtchar(int index){
        ArrayList<Integer> list = new ArrayList<>();
        for(int i=0; i<n; i++){
            int sum = 0;
            for(int j=0; j<words[i].length; j++){
                if(j==index) continue;
                sum += words[i][j];
            }
            list.add(words[i][index] - sum);
            // f[i] = words[i][index] - sum;
        }
        Collections.sort(list, Collections.reverseOrder());
        int[] f = new int[list.size()];
        for(int i=0; i<list.size(); i++){
            f[i] = list.get(i);
        }
        return f;
    }
    private static int maxWindow(int[] f){
        int count = 0, sum = 0;
        int index = 0;
        while(index<f.length && sum+f[index]>0){
            sum += f[index++];
            count++;
        }
        return count;
    }
    public static void main(String[] args){
        FS sc = new FS();
        PrintWriter pw = new PrintWriter(System.out);
        int t = sc.nextInt();
        while(t-->0){
            n = sc.nextInt();
            words = new int[n][5];
            maxWords = 0;
            for(int i=0; i<n; i++){
                String s = sc.next();
                for(int j=0; j<s.length(); j++){
                    words[i][s.charAt(j)-'a']++;
                }
            }
            int maxWindow = 0;
            for(int i=0; i<5; i++){
                int[] f = netwrtchar(i);
                int current = maxWindow(f);
                maxWindow = Math.max(maxWindow, current);
            }
            System.out.println(maxWindow);
        }
        pw.flush();
        pw.close();
    }

    static class FS{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");
    
        String next(){
            while(!st.hasMoreElements()){
                try{
                    st = new StringTokenizer(br.readLine());
                } catch(Exception ignored){
                }
            }
            return st.nextToken();
        }
        
        int[] nextArray(int n){
            int[] a = new int[n];
            for(int i = 0; i < n; i++){
                a[i] = nextInt();
            }
            return a;
        }
        
        long[] nextLongArray(int n){
            long[] a = new long[n];
            for(int i = 0; i < n; i++){
                a[i] = nextLong();
            }
            return a;
        }
        
        int nextInt(){
            return Integer.parseInt(next());
        }
        
        long nextLong(){
            return Long.parseLong(next());
        }
        
        double nextDouble(){
            return Double.parseDouble(next());
        }
    }
}