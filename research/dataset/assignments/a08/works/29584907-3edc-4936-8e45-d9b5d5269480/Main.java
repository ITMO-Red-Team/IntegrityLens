import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
      Node left[]=new Node[n];
      int index=0;
      Node ini[]=new Node[n];
      int tmp[]=new int[n];
      for(int i=0;i<n;i++){
         tmp[i]=sc.nextInt();
      }
      for(int i=0;i<n;i++){
         ini[i]=new Node(tmp[i],tmp[i]-sc.nextInt()+1);
      }
      Arrays.sort(ini);
      left[0]=ini[0];
      for(int i=1;i<n;i++){
         //System.out.println(ini[i].k+" "+ini[i].s);
         if(ini[i].s<=left[index].k&&ini[i].k>left[index].k){
            left[index].k=ini[i].k;
         }else if(ini[i].s>left[index].k){
            index++;
            left[index]=ini[i];
         }
      }
      long ans=0;
      for(int i=0;i<=index;i++){
         //System.out.println(left[i].k+" "+left[i].s);
         ans+=(long)(left[i].k-left[i].s+2)*(left[i].k-left[i].s+1)/2;
      }
      System.out.println(ans);


   }
}
class Node implements Comparable<Node>{
   int k,s;
   Node(int k,int s){
      this.s=s;
      this.k=k;
   }
   @Override
   public int compareTo(Node node) {
      return this.s-node.s;
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
