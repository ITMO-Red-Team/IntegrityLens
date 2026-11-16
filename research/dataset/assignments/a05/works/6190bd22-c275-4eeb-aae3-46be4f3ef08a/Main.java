import java.util.*;
import java.io.*;

public class Solution {
  static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  public static void solve() throws IOException {
    int n = Integer.parseInt(br.readLine());
    ArrayList<int[]> words = new ArrayList<>();
    ArrayList<Integer> lens = new ArrayList<>();
    //count [a,b,c,d,e]
    for (int i = 0; i < n; i++) {
      String temp = br.readLine();
      int[] word = new int[5];
      for (int j = 0; j < temp.length(); j++) {
        word[(int)(temp.charAt(j)) - 97]++;
      }
      words.add(word);
      lens.add(temp.length());
    }
    int ans = 0;
    for (int i = 0; i < 5; i++) {
      ArrayList<Integer> sums = new ArrayList<>();
      //target occ - other occ
      for (int j = 0; j < words.size(); j++) {
        sums.add(words.get(j)[i] - (lens.get(j) - words.get(j)[i]));
      }
      Collections.sort(sums);
      int loc = 0;
      int run = 0;
      for (int j = sums.size() - 1; j >= 0; j--) {
        if(run + sums.get(j) > 0) {
          loc++;
          run += sums.get(j);
        }
        else break;
      }
      ans = Math.max(ans, loc);
    }
    System.out.println(ans);
  }
  public static void main(String[] args) throws IOException {
    int t = Integer.parseInt(br.readLine());
    for (int i = 0; i < t; i++) {
      solve();
    }
  }
}
