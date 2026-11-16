import java.util.*;
import java.io.*;
import java.math.*;

public class Coder {
  static int n;
  static long c[];
  static StringBuilder str = new StringBuilder("");
  static void solve() {
      long mne=c[0];
      long mno=c[1];
      long ans=(c[0]+c[1])*n;
      long se=c[0];
      long so=c[1];
      long ecnt=1,ocnt=1;
      for(int i=2;i<n;i++){
          if(i%2==0){mne=Math.min(mne, c[i]);se+=c[i];ecnt++;}
          else{mno=Math.min(mno, c[i]);so+=c[i];ocnt++;}
          
          ans=Math.min(ans, se+mne*(n-ecnt)+so+mno*(n-ocnt));
      }
      str.append(ans).append("\n");
  }

  public static void main(String[] args) throws java.lang.Exception {
    BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
    int q = Integer.parseInt(bf.readLine().trim());
    while(q-->0) {
        n=Integer.parseInt(bf.readLine().trim());
        c=new long[n];
        String s[]=bf.readLine().trim().split("\\s+");
        for(int i=0;i<n;i++) c[i]=Long.parseLong(s[i]);
      solve();
    }
    System.out.print(str);
  }
}