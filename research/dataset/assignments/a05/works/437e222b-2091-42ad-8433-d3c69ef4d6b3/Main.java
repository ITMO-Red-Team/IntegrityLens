
/*
 
    /$$$$$  /$$$$$$  /$$    /$$  /$$$$$$                                                                  
   |__  $$ /$$__  $$ |$$    |$$ /$$__  $$                                                                 
          | $$| $$  \ $$|    $$|$$| $$  \ $$                                                                 
          | $$| $$$$$$$$|  $$ / $$/| $$$$$$$$                                                                 
/ $$  | $$| $$__  $$ \  $$ $$/ | $$__  $$                                                                 
| $$  | $$| $$  | $$  \  $$$/  | $$  | $$                                                                 
|  $$$$$$/| $$  | $$   \  $/   | $$  | $$                                                                 
 \______/ |__/  |__/    \_/    |__/  |__/    
 
/$$$$$$$  /$$$$$$$   /$$$$$$   /$$$$$$  /$$$$$$$   /$$$$$$  /$$      /$$ /$$      /$$ /$$$$$$$$ /$$$$$$$ 
| $$__  $$| $$__  $$ /$$__  $$ /$$__  $$| $$__  $$ /$$__  $$| $$$    /$$$| $$$    /$$$| $$_____/| $$__  $$
| $$  \ $$| $$  \ $$| $$  \ $$| $$  \__/| $$  \ $$| $$  \ $$| $$$$  /$$$$| $$$$  /$$$$| $$      | $$  \ $$
| $$$$$$$/| $$$$$$$/| $$  | $$| $$ /$$$$| $$$$$$$/| $$$$$$$$| $$ $$/$$ $$| $$ $$/$$ $$| $$$$$   | $$$$$$$/
| $$____/ | $$__  $$| $$  | $$| $$|_  $$| $$__  $$| $$__  $$| $$  $$$| $$| $$  $$$| $$| $$__/   | $$__  $$
| $$      | $$  \ $$| $$  | $$| $$  \ $$| $$  \ $$| $$  | $$| $$\  $ | $$| $$\  $ | $$| $$      | $$  \ $$
| $$      | $$  | $$|  $$$$$$/|  $$$$$$/| $$  | $$| $$  | $$| $$ \/  | $$| $$ \/  | $$| $$$$$$$$| $$  | $$
|__/      |__/  |__/ \______/  \______/ |__/  |__/|__/  |__/|__/     |__/|__/     |__/|________/|__/  |__/
                                                                                                                                                                                                                
                                                                                                                                                                                                                
 
*/

import java.util.*;
import java.lang.*;
import java.io.*;

public class InterestingStrory {
    public static void main(String[] args) throws java.lang.Exception {
        // your code goes here
        try {
            // Scanner sc=new Scanner(System.in);
            FastReader sc = new FastReader();
            int t =sc.nextInt();
            while (t-- > 0) {
                int n=sc.nextInt();
                int[][] occ=new int[n][5];
                for(int i=0;i<n;i++){
                    char[] warr=sc.next().toCharArray();
                    for(char ch:warr){
                        occ[i][ch-'a']++;
                    }
                }
                int ans=Integer.MIN_VALUE;;
                for(int c=0;c<5;c++){
                    int[] sums=new int[n];
                    for(int i=0;i<n;i++){
                        int sum=0;
                        for(int j=0;j<5;j++){
                            if(c==j){
                                sum+=occ[i][j];
                            }
                            else{
                                sum-=occ[i][j];
                            }
                        }
                        sums[i]=sum;
                    }
                    Arrays.sort(sums);
                    int currSum=0;
                    int l=n-1;
                    for(l=n-1;l>=0;l--){
                        if(currSum+sums[l]<=0){
                            break;
                        }
                        else{
                            currSum+=sums[l];
                        }
                    }
                    ans=Math.max(ans,n-l-1);
                }
                System.out.println(ans);
            }
        } catch (Exception e) {
            return;
        }

    }
    

    static int BS(int[] arr, int l, int r, int element) {
        int low = l;
        int high = r;
        while (high - low > 1) {
            int mid = low + (high - low) / 2;
            if (arr[mid] < element) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        if (arr[low] == element) {
            return low;
        } else if (arr[high] == element) {
            return high;
        }
        return -1;
    }

    static int lower_bound(int[] arr, int l, int r, int element) {
        int low = l;
        int high = r;
        while (high - low > 1) {
            int mid = low + (high - low) / 2;
            if (arr[mid] < element) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        if (arr[low] >= element) {
            return low;
        } else if (arr[high] >= element) {
            return high;
        }
        return -1;
    }

    static int upper_bound(int[] arr, int l, int r, int element) {
        int low = l;
        int high = r;
        while (high - low > 1) {
            int mid = low + (high - low) / 2;
            if (arr[mid] <= element) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        if (arr[low] > element) {
            return low;
        } else if (arr[high] > element) {
            return high;
        }
        return -1;
    }
    public static long gcd_long(long a,long b){
        //a/b,a-> dividant b-> divisor
        if(b==0)return a;
        return gcd_long(b, a%b);
    }
    public static int gcd_int(int a,int b){
        //a/b,a-> dividant b-> divisor
        if(b==0)return a;
        return gcd_int(b, a%b);
    }
    public static int lcm(int a,int b){
        int gcd=gcd_int(a, b);
        return (a*b)/gcd;
    }

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        String nextLine() {
            String s = "";
            try {
                s = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }

        Long nextLong() {
            return Long.parseLong(next());
        }
    }
}

/*
 * public static boolean lie(int n,int m,int k){ if(n==1 && m==1 && k==0){
 * return true; } if(n<1 || m<1 || k<0){ return false; } boolean
 * tc=lie(n-1,m,k-m); boolean lc=lie(n,m-1,k-n); if(tc || lc){ return true; }
 * return false; }
 */
