import java.util.Scanner;

// alice с переименованными переменными и другим оформлением
public class Main {
  public static void main(String[] a) {
    Scanner sc = new Scanner(System.in);
    int total = sc.nextInt();
    long acc = 0; int k = 0;
    for (int j = 0; j < total; j++) { int v = sc.nextInt();
      if (v > 0) { acc += v; k++; } }
    System.out.println(acc + " " + k);
  }
}
