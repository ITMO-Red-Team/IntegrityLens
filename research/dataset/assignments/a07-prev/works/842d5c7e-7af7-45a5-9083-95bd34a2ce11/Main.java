import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;

public class Main {

    public static void main(String[] args) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine());
        StringTokenizer st = new StringTokenizer(br.readLine());
        int ar[] = new int[n];
        for(int i = 0; i < n; i++){
            ar[i] = Integer.parseInt(st.nextToken());
        }
        ArrayList<Integer> ones = new ArrayList<Integer>();
        ArrayList<Integer> zeroes = new ArrayList<Integer>();
        for(int i = 0; i < n; i++){
            if(ar[i] == 1)
                ones.add(i);
            else
                zeroes.add(i);
        }
        int r = ones.size();
        int c = zeroes.size();
        int time[][] = new int[r][c];
        System.out.println(calculateTime(time, r, c, 0, 0, ones, zeroes));
    }
    public static int calculateTime(int time[][], int r, int c, int currR, int currC, ArrayList<Integer> ones, ArrayList<Integer> zeroes){
        // System.out.println(currR + " " + currC);
        if(currR == r)
            return 0;
        if(currC == c)
            return (int)1e9;
        if(time[currR][currC] != 0)
            return time[currR][currC];
        return time[currR][currC] = Math.min((calculateTime(time, r, c, currR + 1, currC + 1, ones, zeroes) + Math.abs(ones.get(currR) - zeroes.get(currC))), calculateTime(time, r, c, currR, currC + 1, ones, zeroes));
    }
}