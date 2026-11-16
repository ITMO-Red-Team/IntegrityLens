import java.io.*;
import java.util.*;
 public class Solution{
    public static void main (String[] args) throws java.lang.Exception {
        FastReader sc = new FastReader();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
        int testCase = sc.nextInt();
        while (testCase-->0){
            int n = sc.nextInt();
            String[] strArr = new String[n];
            for(int i=0; i<n; i++) {
                strArr[i]=sc.nextLine();
            }
            int[] total = new int[5];
            ArrayList<int[]> al = new ArrayList<>();
            for(int i=0; i<n; i++){
                int[] arr= new int[5];
                for(int j=0; j<strArr[i].length(); j++){
                    arr[strArr[i].charAt(j)-'a']++;
                }
                for(int j=0; j<5; j++){
                    total[j]+=arr[j];
                }
                al.add(arr);
            }
            int ans=0;
            for(int i=0; i<5; i++){
                ArrayList<Integer> all = new ArrayList<>();
                for(int j=0; j<n; j++){
                    all.add(strArr[j].length()-2*al.get(j)[i]);
                }
                java.util.Collections.sort(all);
                int c=0, d=0;
                for(int j=0; j<n; j++){
                    c+=all.get(j);
                    if(c<0) d=j+1;
                }
                ans = Math.max(ans,d);
            }
            System.out.println(ans);
        }
    }
    // Fast Reader Class
    static class FastReader {
        BufferedReader br; StringTokenizer st;
        public FastReader() { br = new BufferedReader(new InputStreamReader(System.in)); }
        String next() { while (st == null || !st.hasMoreElements()) { try { st = new StringTokenizer(br.readLine()); } catch (IOException e) { e.printStackTrace(); } }return st.nextToken(); }
        int nextInt() {
            return Integer.parseInt(next());
        }
        long nextLong() {
            return Long.parseLong(next());
        }
        double nextDouble() {
            return Double.parseDouble(next());
        }
        String nextLine() { String str = ""; try { str = br.readLine(); }  catch (IOException e) { e.printStackTrace(); }return str; }
    }
}