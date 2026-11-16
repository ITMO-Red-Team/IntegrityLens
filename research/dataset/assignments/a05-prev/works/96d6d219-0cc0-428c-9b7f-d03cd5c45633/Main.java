import java.util.*;
import java.io.*;
public class Main {
// For fast input output
static class FastReader {
BufferedReader br;
StringTokenizer st;
public FastReader()
{ try {br = new BufferedReader(
new FileReader("input.txt"));
PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
System.setOut(out);}
catch(Exception e) { br = new BufferedReader(new InputStreamReader(System.in));}
}
String next()
{
while (st == null || !st.hasMoreElements()) {
try {st = new StringTokenizer(br.readLine());}
catch (IOException e) {
e.printStackTrace();}
}
return st.nextToken();
}
int nextInt() { return Integer.parseInt(next()); }
long nextLong() { return Long.parseLong(next()); }
double nextDouble() {return Double.parseDouble(next()); }
String nextLine()
{
String str = "";
try {
str = br.readLine();
}
catch (IOException e) {
e.printStackTrace();
}
return str;
}
}
// end of fast i/o code
public static void main(String[] args) {
  FastReader reader = new FastReader();
  int Q = reader.nextInt();
  outer: for (int q = 0; q < Q; q++) {
    int N = reader.nextInt();
    int[][] scores = new int[5][N];
    for (int i = 0; i < N; i++) {
      int[] occurs = new int[5];
      String word = reader.next();
      for (int j = 0; j < word.length(); j++) {
        occurs[word.charAt(j) - 'a']++;
      }
      for (int j = 0; j < 5; j++) {
        scores[j][i] = occurs[j] - (word.length() - occurs[j]) ;
      }
    }
    int bestCount = 0;
    for (int i = 0; i < 5; i++) {
      int[] curr = scores[i];
      Arrays.sort(curr);
      int currentCount = 1;
      int currentScore = curr[curr.length - 1];
      for (int j = curr.length - 2; j >= 0 && currentScore > 0; j--) {
        currentScore += curr[j];
        currentCount++;
      }
      if (currentScore <= 0) currentCount--;
      bestCount = Math.max(currentCount, bestCount);
    }
    System.out.println(bestCount);
  }
}

}
