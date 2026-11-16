//import jdk.nashorn.internal.parser.Scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;
import java.util.*;

import javax.management.Query;
public class Test{
    public static void main(String[] args) throws IOException, InterruptedException{
      Scanner sc = new Scanner(System.in);
     int t = sc.nextInt();
    
     while(t-->0){
      int n = sc.nextInt();
      String [] words = new String[n];
      int [] occ = new int[5];
      int [] occWord = new int [5];
      boolean []  found ;
      for(int i =0;i<n;i++){
        words[i] = sc.nextLine();
        found = new boolean[5];
        for(int j=0 ; j<words[i].length();j++){
          occ[words[i].charAt(j)-'a']++;
          if(!found[words[i].charAt(j)-'a']){
            found[words[i].charAt(j)-'a']=true;
            occWord[words[i].charAt(j)-'a'] ++;
          }
        }
      }
      int maxRes =0;
      for(int i =0;i<5;i++){
      int maxChar = 'a' +i;
      
      PriorityQueue<Pair> pq = new PriorityQueue<>();
      for (String word : words){
          pq.add(new Pair(word,occOfMaxChar(word, maxChar)-occOfOtherChar(word, maxChar)));
      }
      int res = 0;
      int curr = 0;
      int maxCharCount = 0;
      int otherCharCount =0;
      while(!pq.isEmpty()){
        String word = pq.poll().x;
        maxCharCount +=occOfMaxChar(word, maxChar);
         otherCharCount += occOfOtherChar(word, maxChar);
         curr ++;
         if(maxCharCount >otherCharCount){
           res = curr;
         }
          
      }
      maxRes = Math.max(maxRes, res);
    }
    System.out.println(maxRes);}
   
  }
  public static int occOfMaxChar (String s, int maxChar){
     int occ = 0;
     for(int i =0 ;i<s.length();i++){
       if(s.charAt(i)==maxChar){
         occ++;
       }
      }
      return occ;
  }
  public static int occOfOtherChar (String s, int maxChar){
    int occ = 0;
    for(int i =0 ;i<s.length();i++){
      if(s.charAt(i)!=maxChar){
        occ++;
      }
     }
     return occ;
 }

    static int w;
    static int n;
    static long [][] memo;
    static int [] depth ;
    static long[] values;
    static ArrayList<Pair> gold ;
    public static long dp (int idx,int time){
      if ( idx == n){
       return 0;
      }
      if (memo[idx][time] != -1){
        return  memo[idx][time];
      }
      long take = 0;
      if (3 * w*depth[idx] <= time){
        take = values[idx]+ dp(idx+1, time-3*w*depth[idx]);
      }
      long leave = dp(idx+1, time);
      return memo[idx][time]=Math.max(take, leave);
    }


    static class Pair implements Comparable {
      String x;
      int y;
      public Pair (String x, int y) {
        this.x = x;
        this.y = y;
      }
      @Override
      public int compareTo(Object o){
        Pair p = (Pair) o;
        return p.y -y;
      }
    }
    static class Scanner {
      StringTokenizer st;
      BufferedReader br;
   
      public Scanner(InputStream s) {
        br = new BufferedReader(new InputStreamReader(s));
      }
   
      public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
      }
   
      public String next() throws IOException {
        while (st == null || !st.hasMoreTokens())
          st = new StringTokenizer(br.readLine());
        return st.nextToken();
      }
   
      public int nextInt() throws IOException {
        return Integer.parseInt(next());
      }
   
      public long nextLong() throws IOException {
        return Long.parseLong(next());
      }
   
      public String nextLine() throws IOException {
        return br.readLine();
      }
   
      public double nextDouble() throws IOException {
        String x = next();
        StringBuilder sb = new StringBuilder("0");
        double res = 0, f = 1;
        boolean dec = false, neg = false;
        int start = 0;
        if (x.charAt(0) == '-') {
          neg = true;
          start++;
        }
        for (int i = start; i < x.length(); i++)
          if (x.charAt(i) == '.') {
            res = Long.parseLong(sb.toString());
            sb = new StringBuilder("0");
            dec = true;
          } else {
            sb.append(x.charAt(i));
            if (dec)
              f *= 10;
          }
        res += Long.parseLong(sb.toString()) / f;
        return res * (neg ? -1 : 1);
      }
   
      public boolean ready() throws IOException {
        return br.ready();
      }
   
    }
     }

