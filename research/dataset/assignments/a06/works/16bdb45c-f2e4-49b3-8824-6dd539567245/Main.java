import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class BST {

    static class pair implements Comparable{

        int high;
        int idx;

        pair(int x , int y){
            high = x;
            idx = y;
        }

        public String toString(){
            return high + " " + idx;
        }


        @Override
        public int compareTo(Object o) {
            pair p = (pair)o;
            return high-p.high;
        }
    }

    public static void main(String[] args) throws IOException{

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine());
        while (t-->0){

            int n = Integer.parseInt(br.readLine());
            long [] arr = new long[n];
            StringTokenizer st = new StringTokenizer(br.readLine());
            for (int i = 0; i < n; i++) {
                long tmp = Long.parseLong(st.nextToken());
                arr[i] = tmp;

            }

            int h = 1;
            int v = 1;
            long minHor = arr[0];
            long minVir = arr[1];
            long sum0 = arr[0];
            long sum1 = arr[1];
            long total = (arr[0] + arr[1])*n;


            for (int i = 2; i < n; i++) {

                if(i%2==0){
                    h++;
                    sum0 += arr[i];
                    minHor = Math.min(arr[i] , minHor);
                    total = Math.min(total , minHor*(n-h+1)+(sum0-minHor)+minVir*(n-v+1)+(sum1-minVir));
                }else {
                    v++;
                    sum1 += arr[i];
                    minVir = Math.min(arr[i] , minVir);
                    total = Math.min(total , minHor*(n-h+1)+(sum0-minHor)+minVir*(n-v+1)+(sum1-minVir));
                }

            }


            System.out.println(total);

        }



    }




}