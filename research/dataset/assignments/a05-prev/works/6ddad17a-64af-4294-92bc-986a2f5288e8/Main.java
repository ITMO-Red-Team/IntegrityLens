import java.io.*;
import java.util.*;
public class Solution{
    public static int in = 0, count=0;
    static class comparator implements Comparator<int[]>{
        public int compare(int[] arr1 ,int[] arr2){
            int gain1 =0, gain2=0;
            for(int i:arr1) gain1+=i;
            for(int i:arr2) gain2+=i;
            if((gain1-2*arr1[in])>(gain2-2*arr2[in]))
                return 1;
            else if((gain1-2*arr1[in])==(gain2-2*arr2[in])) {
                count++;
                return 0;
            }
            return -1;
        }
    }
    public static int solve(ArrayList<int[]> al, int[] total){
        ArrayList<int[]> c = (ArrayList<int[]>) al.clone();
        java.util.Collections.sort(c, new comparator());

        int i= al.size()-1;
        int sum=0; for(int ii:total) sum+=ii;
        int check = total[in];
        sum-=check;
        for(; i>=0; i--){
            if(check>sum) return i+1;
            else{
                int newSum=0; for(int ii:c.get(i)) newSum+=ii;
                newSum-=c.get(i)[in];
                sum-=newSum;
                check-=c.get(i)[in];
            }
        }
        return 0;
    }
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
            int ans = Integer.MIN_VALUE;
            for(int i=0; i<5; i++) {
                in = i;
                ans = Math.max(solve(al, total), ans);
                count=0;
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