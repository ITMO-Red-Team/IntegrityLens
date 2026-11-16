import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.InputMismatchException;

/**
 * Accomplished using the EduTools plugin by JetBrains https://plugins.jetbrains.com/plugin/10081-edutools
 */

public class Main {
   static InputReader sc=new InputReader(System.in);
   public static void main(String[] args) {
      // Write your solution here
      int t=sc.nextInt();
      while(t-->0){
         solve();
      }

   }

   private static void solve() {
      int n=sc.nextInt();
      char[] a=sc.nextLine().toCharArray();
      char[] b = sc.nextLine().toCharArray();
      int sml=0,nsml=0;
      boolean flag=false;
      int sm1=0;
      int nsm1=0;
      for(int i=0;i<n;i++){
         if(a[i]==b[i]){
            sml++;
            if(a[i]=='1')sm1++;
         }else {
            if (a[i] == '1') nsm1++;
            nsml++;
         }
      }
      int d1=sml-2*sm1;
      int d2=nsml-2*nsm1;
      int ans=-1;
      if(sml%2==1&&nsml%2==1&&d1==-1){
         ans=sml;
      }else if(sml%2==0&&nsml%2==0&&d2==0){
         ans=nsml;
      }else if(sml%2==1&&nsml%2==0&&(d1==-1||d2==0)){
         if(d1==-1&&d2!=0){
            ans=sml;
         }else if(d1!=-1&&d2==0){
            ans=nsml;
         }else if(d1==-1&&d2==0){
            ans=Math.min(sml,nsml);
         }
      }
      if(sml==n)ans=0;
      System.out.println(ans);
   }
}
class InputReader {

   private final InputStream stream;
   private final byte[] buf = new byte[8192];
   private int curChar, snumChars;

   public InputReader(InputStream st) {
      this.stream = st;
   }

   public int read() {
      if (snumChars == -1)
         throw new InputMismatchException();
      if (curChar >= snumChars) {
         curChar = 0;
         try {
            snumChars = stream.read(buf);
         } catch (IOException e) {
            throw new InputMismatchException();
         }
         if (snumChars <= 0)
            return -1;
      }
      return buf[curChar++];
   }

   public int nextInt() {
      int c = read();
      while (isSpaceChar(c)) {
         c = read();
      }
      int sgn = 1;
      if (c == '-') {
         sgn = -1;
         c = read();
      }
      int res = 0;
      do {
         res *= 10;
         res += c - '0';
         c = read();
      } while (!isSpaceChar(c));
      return res * sgn;
   }

   public long nextLong() {
      int c = read();
      while (isSpaceChar(c)) {
         c = read();
      }
      int sgn = 1;
      if (c == '-') {
         sgn = -1;
         c = read();
      }
      long res = 0;
      do {
         res *= 10;
         res += c - '0';
         c = read();
      } while (!isSpaceChar(c));
      return res * sgn;
   }

   public int[] nextIntArray(int n) {
      int a[] = new int[n];
      for (int i = 0; i < n; i++) {
         a[i] = nextInt();
      }
      return a;
   }

   public String readString() {
      int c = read();
      while (isSpaceChar(c)) {
         c = read();
      }
      StringBuilder res = new StringBuilder();
      do {
         res.appendCodePoint(c);
         c = read();
      } while (!isSpaceChar(c));
      return res.toString();
   }

   public String nextLine() {
      int c = read();
      while (isSpaceChar(c))
         c = read();
      StringBuilder res = new StringBuilder();
      do {
         res.appendCodePoint(c);
         c = read();
      } while (!isEndOfLine(c));
      return res.toString();
   }

   public boolean isSpaceChar(int c) {
      return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
   }

   private boolean isEndOfLine(int c) {
      return c == '\n' || c == '\r' || c == -1;
   }

}
