import java.io.*;
import java.util.*;

public class q3 {
    public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//    public static long mod = 1000000007;


    public static void solve() throws Exception {
        String[] parts = br.readLine().split(" ");
        int n = Integer.parseInt(parts[0]);

        StringBuilder a = new StringBuilder(br.readLine());
        StringBuilder b = new StringBuilder(br.readLine());

        boolean flag = true;
        for(int i = 0;i < n;i++) if(a.charAt(i) != b.charAt(i)) flag = false;
        if(flag){
            System.out.println(0);
            return;
        }

        if(n == 1){
            System.out.println(-1);
            return;
        }

        int aco = 0,bco = 0;
        for(int i = 0;i < a.length();i++) {
            if(a.charAt(i) == '1') aco++;
            if(b.charAt(i) == '1') bco++;
        }

        if(aco == 0 || bco == 0){
            System.out.println(-1);
            return;
        }

        if(aco == bco || n - aco + 1 == bco){

        }else{
            System.out.println(-1);
            return;
        }

        int ez = 0,eo = 0,nez = 0,neo = 0;
        for(int i = 0;i < n;i++){
            if(a.charAt(i) == b.charAt(i)){
                if(a.charAt(i) == '1') eo++;
                else ez++;
            }else{
                if(a.charAt(i) == '1') neo++;
                else nez++;
            }
        }

        HashSet<String> vis = new HashSet<>();
        Queue<String> que = new LinkedList<>();

        vis.add(ez + " " + eo + " " + nez + " " + neo);
        que.add(ez + " " + eo + " " + nez + " " + neo);

        int level = 0;
        while(que.size() > 0){
            for(int s = que.size() - 1;s >= 0;s--) {
                String[] rem = que.remove().split(" ");
                int v1 = Integer.parseInt(rem[0]);
                int v2 = Integer.parseInt(rem[1]);
                int v3 = Integer.parseInt(rem[2]);
                int v4 = Integer.parseInt(rem[3]);

                if (v3 == 0 && v4 == 0) {
                    System.out.println(level);
                    return;
                }

                String first = (v4) + " " + (v3 + 1) + " " + (v2 - 1) + " " + (v1);
                if (vis.add(first)) que.add(first);

                String second = (v4 - 1) + " " + (v3) + " " + (v2) + " " + (v1 + 1);
                if (vis.add(second)) que.add(second);
            }
            level++;
        }

        System.out.println(-1);
    }

    public static void main(String[] args) throws Exception {
        int tests = Integer.parseInt(br.readLine());
        for (int test = 1; test <= tests; test++) {
            solve();
        }
    }

//    public static ArrayList<Integer> primes;
//    public static void seive(int n){
//        primes = new ArrayList<>();
//        boolean[] arr = new boolean[n + 1];
//        Arrays.fill(arr,true);
//
//        for(int i = 2;i * i <= n;i++){
//            if(arr[i]) {
//                for (int j = i * i; j <= n; j += i) {
//                    arr[j] = false;
//                }
//            }
//        }
//        for(int i = 2;i <= n;i++) if(arr[i]) primes.add(i);
//    }


//    public static void sort(int[] arr){
//        ArrayList<Integer> temp = new ArrayList<>();
//        for(int val : arr) temp.add(val);
//
//        Collections.sort(temp);
//
//        for(int i = 0;i < arr.length;i++) arr[i] = temp.get(i);
//    }

//    public static void sort(long[] arr){
//        ArrayList<Long> temp = new ArrayList<>();
//        for(long val : arr) temp.add(val);
//
//        Collections.sort(temp);
//
//        for(int i = 0;i < arr.length;i++) arr[i] = temp.get(i);
//    }
//
//    public static long power(long a,long b,long mod){
//        if(b == 0) return 1;
//
//        long p = power(a,b / 2,mod);
//        p = (p * p) % mod;
//
//        if(b % 2 == 1) return (p * a) % mod;
//        return p;
//    }
//    public static long modDivide(long a,long b,long mod){
//        return ((a % mod) * (power(b,mod - 2,mod) % mod)) % mod;
//    }
//
//    public static int GCD(int a,int b){
//        return b == 0 ? a : GCD(b,a % b);
//    }
//    public static long GCD(long a,long b){
//        return b == 0 ? a : GCD(b,a % b);
//    }
//
//    public static int LCM(int a,int b){
//        return a * b / GCD(a,b);
//    }
//    public static long LCM(long a,long b){
//        return a * b / GCD(a,b);
//    }
}
