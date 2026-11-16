import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.*;

public class ReverseString {




    static class pair implements Comparable<pair>{

        String s;
        int contribution;
        pair(String s , int contribution){
            this.s = s;
            this.contribution = contribution;
        }



        @Override
        public int compareTo(pair pair) {
            return this.contribution-pair.contribution;
        }


        public String toString(){
            return s + " " + contribution;
        }

        public int getValue(){
            return contribution;
        }




    }


    static int factorOfLetter(String s , char c){

        int sum = 0;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i)==c)sum++;
        }
        int rem = Math.abs(sum-s.length());

        return sum-rem;
    }



    public static void main(String[] args)  throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);

        int t = Integer.parseInt(br.readLine());
        while (t-->0){


            int n = Integer.parseInt(br.readLine());

            int max = Integer.MIN_VALUE;

            String [] arr = new String[n];
            for (int i = 0; i < n; i++) {
                arr[i] = br.readLine();
            }



            for (int i = 0; i < 5; i++) {
                int word = 0;
                ArrayList<pair> tmp = new ArrayList<>();
                for (int j = 0; j < arr.length; j++) {

                    tmp.add(new pair(arr[j] , factorOfLetter(arr[j] , (char)(i+97))));

                }

                Collections.sort(tmp);
                //System.out.println(tmp);
                int acc = 0;
                for (int j = tmp.size()-1; j >=0 ; j--) {

                    acc += tmp.get(j).contribution;
                    if (acc<=0)break;
                    else word++;

                }

                max = Math.max(max , word);
            }


            System.out.println(max);









        }


    }



}