
import java.io.*;
import java.util.*;

public class Main {

  public static class StringPair implements Comparable{
    String s;
    Integer charCount;
    public StringPair(String s, Integer charCount){
      this.s = s;
      this.charCount = charCount;
    }

    @Override
    public int compareTo(Object o) {
      StringPair f = (StringPair) o;

      if(f.charCount < this.charCount){
        return 1;
      }
      else if(f.charCount == this.charCount){
        if(f.s.length() - f.charCount > this.s.length() - this.charCount){
          return -1;
        }
        else if(f.s.length() - f.charCount < this.s.length() - this.charCount){
          return 1;
        }
        else{
          return 0;
        }
      }
      else{
        return -1;
      }

    }
  }


  public static void main(String[] args) throws IOException {
    Scanner sc = new Scanner(System.in);
    int t = sc.nextInt();
    while (t-- > 0) {

      int n = sc.nextInt();

      String[] s = new String[n];

      for(int i = 0 ; i < n ; i++){
        s[i] = sc.next();
      }

      Integer[][] count = new Integer[n][5];
      Integer[] totalCount = new Integer[5];

      for(int i = 0 ; i < n ; i++){
        for(int j = 0 ; j < 5 ; j++){
          count[i][j]=0;
        }
      }
      for(int i = 0 ; i < 5 ; i++){
        totalCount[i] = 0;
      }


      Integer total = 0;

      for(int i = 0 ; i < n ; i++){
        for(int j = 0 ; j < s[i].length() ; j++){
          int val = s[i].charAt(j) - 'a';
          count[i][val]++;
          totalCount[val]++;
          total++;
        }
      }

      //2*count[]char > len of total


      int ans = -1;
      for(int ch = 0 ; ch < 5 ; ch++){
        List<StringPair> ls = new ArrayList<>();
        for(int i = 0 ; i < n ; i++){
          int c = s[i].length() - (2 * count[i][ch]);
          ls.add(new StringPair(s[i],c));
        }
        Collections.sort(ls);

        int finalCount = n;

        int thisTotal = total;

        int thisTotalCharcount = totalCount[ch];

        int idx = n-1;

        while(finalCount > ans && 2*thisTotalCharcount <= thisTotal && idx >= 0){//condition true){
          thisTotal -= ls.get(idx).s.length();

          thisTotalCharcount -= (ls.get(idx).s.length() - ls.get(idx).charCount)/2;
          idx--;
          finalCount--;
        }
        ans = Integer.max(ans,finalCount);
      }

      System.out.println(ans);
    }
  }
}
