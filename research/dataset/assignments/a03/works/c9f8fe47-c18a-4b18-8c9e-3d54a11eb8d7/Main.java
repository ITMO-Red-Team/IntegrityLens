import java.util.*;
import java.io.*;
import java.lang.*;
import static java.lang.Math.*;

public class cp{
    static BufferedReader br;
    static StringTokenizer st;

    public static void main(String[] args)throws IOException{

        br = new BufferedReader(new InputStreamReader(System.in));
        List<int[]> answer = new ArrayList<int[]>();

        for(int t=Integer.parseInt(br.readLine()); t>0; t--){

            st = readLine();
            st = readLine();
            int n = tokenInt();
            int k = tokenInt();

            st = readLine();
            int[] posArr = readIntArray(k);
            st = readLine();
            int[] tempArr = readIntArray(k);

            answer.add(new Solver().solve(n, k, posArr, tempArr));

        }

        ListIterator<int[]> it = answer.listIterator();
        while (it.hasNext())
            printArr(it.next());

    }

    static StringTokenizer readLine() throws IOException { return new StringTokenizer(br.readLine()); }
    static int tokenInt() { return Integer.parseInt(st.nextToken()); }
    static long tokenLong(){ return Long.parseLong(st.nextToken()); }
    static double tokenDouble(){ return Double.parseDouble(st.nextToken()); }
    static char[] tokenChar(){ return st.nextToken().toCharArray(); }
    static int[] readIntArray(int n) {
        int[] a=new int[n];
        for (int i=0; i<n; i++) a[i]=tokenInt();
        return a;
    }

    static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b);}
    static int lcm(int a, int b) { return (a * b) / gcd(a,b);}

    static void printArr(int[] arr){
        for (int i=0; i<arr.length; i++) System.out.print(arr[i] + " ");
        System.out.println();
        return;
    }

}


class Solver{
    public int[] solve(int n, int k, int[] posArr, int[] tempArr) throws IOException{

        // System.out.println(n);
        // System.out.println(k);
        int[] answer = new int[n];
        for(int i=0; i<n; i++)
            answer[i] = Integer.MAX_VALUE / 2;
        for(int i=0; i<k; i++)
            answer[posArr[i]-1] = tempArr[i];

        for (int i=1; i<n; i++)
            answer[i] = min(answer[i-1]+1, answer[i]);

        for (int j=n-2; j>=0; j--)
            answer[j] = min(answer[j+1]+1, answer[j]);

        return answer;
    }
}


/*



*/
