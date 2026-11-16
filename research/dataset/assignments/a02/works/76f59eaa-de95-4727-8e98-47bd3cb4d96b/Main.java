import javax.print.attribute.standard.MediaSize;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

public class Program {

    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

    public static void main(String[] args) throws NumberFormatException, IOException {

        int cases = Integer.parseInt(reader.readLine());
        int mod = 998244353;

        while(cases-- > 0) {

            String[] firstLine = reader.readLine().split(" ");
            int n = Integer.parseInt(firstLine[0]);

            String a = reader.readLine();
            String b = reader.readLine();

            int ans = Integer.MAX_VALUE;

            int oneZeroPos = -1;
            int oneOnePos = -1;
            int oneZero = 0;
            int zeroOne = 0;

            for(int i=0;i<n;i++) {
                if(a.charAt(i)=='0' && b.charAt(i)=='1') {
                    zeroOne++;
                }
                if(a.charAt(i)=='1' && b.charAt(i)=='0') {
                    oneZero++;
                }
                if(a.charAt(i)=='1' && b.charAt(i)=='0' && oneZeroPos == -1) {
                    oneZeroPos = i;
                }
                if(a.charAt(i)=='1' && b.charAt(i)=='1' && oneOnePos == -1) {
                    oneOnePos = i;
                }
            }

            if(oneZero==0&&zeroOne==0) {
                printNumber(0);
                continue;
            }

            if(oneZero==zeroOne) {
                ans = oneZero+zeroOne;
            }

            int a1 = func(a, b, oneZeroPos);
            int b1 = func(a, b, oneOnePos);

            if(a1 != Integer.MAX_VALUE) {
                ans = Math.min(ans, 1+a1);
            }

            if(b1 != Integer.MAX_VALUE) {
                ans = Math.min(ans, 1+b1);
            }

            if(ans == Integer.MAX_VALUE) {
                printNumber(-1);
            } else {
                printNumber(ans);
            }

        }

        out.flush();

    }


    public static int func(String str, String comparing, int index) {

        if(index==-1) return Integer.MAX_VALUE;

        char[] ch = str.toCharArray();
        char[] comp = comparing.toCharArray();

        for(int i=0;i<ch.length;i++) {
            if(i!=index) {
                if(ch[i]=='1') ch[i] = '0';
                else ch[i] = '1';
            }
        }

        int oneZero = 0;
        int zeroOne = 0;

        for(int i=0;i<str.length();i++) {
            if(ch[i] == '1' && comp[i] == '0') {
                oneZero++;
            } else if(ch[i] == '0' && comp[i] == '1') {
                zeroOne++;
            }
        }

        if(oneZero == zeroOne) {
            return oneZero+zeroOne;
        }

        return Integer.MAX_VALUE;

    }

     public static int ub(long[] arr, long num) {
        int start = 0;
        int end = arr.length;
        int mid = 0;

        while(start<end) {
            mid = start + (end-start)/2;
            if(arr[mid] < num) {
                start = mid+1;
            }else {
                end = mid;
            }
        }
        return start;
     }

    public static int[] convertToIntPrimitiveArray() throws IOException {
        String[] str = reader.readLine().split(" ");
        return Stream.of(str).mapToInt(Integer::parseInt).toArray();
    }

    public static Integer[] convertToIntArray(String[] str) {
        Integer[] arr = new Integer[str.length];
        for(int i=0;i<str.length;i++) {
            arr[i] = Integer.parseInt(str[i]);
        }
        return arr;
    }

    public static Long[] convertToLongArray(String[] str) {
        Long[] arr = new Long[str.length];
        for(int i=0;i<str.length;i++) {
            arr[i] = Long.parseLong(str[i]);
        }
        return arr;
    }

    public static long[] convertToLongPrimitiveArray() throws IOException {
        String[] str = reader.readLine().split(" ");
        return Stream.of(str).mapToLong(Long::parseLong).toArray();
    }

    public static void printYes() throws IOException {
        out.append("YES" + "\n");
    }

    public static void printNo() throws IOException {
        out.append("NO" + "\n");
    }

    public static void printNumber(long num) throws IOException {
        out.append(num + "\n");
    }

    public static long hcf(long a, long b) {
        if(b==0) return a;
        return hcf(b, a%b);
    }

    public static int findSet(int[] parent, int[] rank, int v) {
        if(v==parent[v]) {
            return v;
        }
        parent[v] = findSet(parent, rank, parent[v]);
        return parent[v];
    }

    public static void unionSet(int[] parent, int[] rank, int a, int b) {

        a = findSet(parent, rank, a);
        b = findSet(parent, rank, b);

        if(a == b) {
            return;
        }

        if(rank[a] < rank[b]) {
            int temp = a;
            a = b;
            b = temp;
        }

        parent[b] = a;
        if(rank[a] == rank[b]) {
            rank[a]++;
        }

    }

    public static void makeSet(int[] parent, int[] rank, int v) {
        parent[v] = v;
        rank[v] = 0;
    }

    public static long modularDivision(long a, long b, int mod) {
        if(a==0) return 1;
        a %= mod;
        long res = 1L;
        while(b > 0) {
            if((b&1)==1) {
                res = (res * a)%mod;
            }
            a = (a*a)%mod;
            b >>= 1;
        }
        return res;
    }

    public static void customSort(int[] arr) {
        int n = arr.length;
        Random rnd = new Random();
        for(int i=0; i<n; ++i){
            int tmp = arr[i];
            int randomPos = i + rnd.nextInt(n-i);
            arr[i] = arr[randomPos];
            arr[randomPos] = tmp;
        }
        Arrays.sort(arr);
        return;
    }

    public static void customSortLong(long[] arr) {
        int n = arr.length;
        Random rnd = new Random();
        for(int i=0; i<n; ++i){
            long tmp = arr[i];
            int randomPos = i + rnd.nextInt(n-i);
            arr[i] = arr[randomPos];
            arr[randomPos] = tmp;
        }
        Arrays.sort(arr);
        return;
    }

    public static long[] factorial(int n, int mod) {
        long[] fact = new long[n+1];
        fact[0] = 1;
        for(int i=1;i<fact.length;i++) {
            fact[i] = (i*fact[i-1] + mod)%mod;
        }
        return fact;
    }

    public static List<Integer> primeNumbers(int num) {
        boolean[] arr = new boolean[num+1];
        List<Integer> primes = new ArrayList<>();

        for(int i=2;i*i<=num;i++) {
            if(!arr[i]) {
                for(int j=i;j*i<=num;j++) {
                    arr[j*i] = true;
                }
            }
        }

        for(int i=2;i<=num;i++) {
            if(!arr[i]) {
                primes.add(i);
            }
        }
        return primes;
    }


}