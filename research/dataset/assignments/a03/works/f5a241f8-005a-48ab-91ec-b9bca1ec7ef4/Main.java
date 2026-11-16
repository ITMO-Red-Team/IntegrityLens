import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.StringTokenizer;

public class AirConditioner {
    public static void main(String[] args) {
        MyScanner sc = new MyScanner();

        int input = sc.nextInt();
        sc.nextLine();
        for(int i = 0; i < input; i++){
            String[] length = sc.nextLine().split(" ");
            String[] index = sc.nextLine().split(" ");
            String[] temperature = sc.nextLine().split(" ");
            long[] boxes = findTemp(length, index,temperature);
            for(int j = 0; j < boxes.length; j++){
                if(j == 0) System.out.print(boxes[j]);
                else System.out.print(" "+boxes[j]);
            }
            System.out.println();
            sc.nextLine();
        }
    }

    public static long[]  findTemp(String[] length, String[] index,String[] temperature) {
        long n = Long.parseLong(length[0]);
        HashSet<Integer> set = new HashSet<>();
        long airCond = Long.parseLong(length[1]);
        long[] boxes = new long[(int)n];
        Arrays.fill(boxes,Integer.MAX_VALUE);
        for(int i = 0 ; i < index.length; i++){
            boxes[Integer.parseInt(index[i]) - 1] = Long.parseLong(temperature[i]);
            set.add(Integer.parseInt(index[i]) - 1);
           // System.out.println(index[i]);
        }

        int prev = -1;
        for(int i = 0 ; i < boxes.length; i++) {
            //System.out.println(i +" "+set.contains(i));
            if(set.contains(i)){
                if(prev != - 1) boxes[i] = Math.min((Math.abs(i - prev) ) + boxes[prev],boxes[i]);
               // System.out.println(i + "  "+prev);
                prev = i;
            }
           if(prev == -1) continue;
           if(set.contains(i)){
               prev = i;
           }

           else{
               boxes[i] = (Math.abs(i - prev) ) + boxes[prev];
           }
        }
        prev = boxes.length;
        for(int i = boxes.length - 1; i >= 0; i--) {
            if(set.contains(i)){
              //  System.out.println(i);
                if(prev != boxes.length)  boxes[i] = Math.min((Math.abs(i - prev) ) + boxes[prev],boxes[i]);
                prev = i;
            }
            if(prev == boxes.length) continue;

            else{
                boxes[i] = Math.min((Math.abs(i - prev) ) + boxes[prev],boxes[i]);
            }
        }


        return boxes;
    }
}
class MyScanner {
    BufferedReader br;
    StringTokenizer st;

    public MyScanner() {
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

    long nextLong() {
        return Long.parseLong(next());
    }

    double nextDouble() {
        return Double.parseDouble(next());
    }

    String nextLine() {
        String str = "";
        try {
            str = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

}
