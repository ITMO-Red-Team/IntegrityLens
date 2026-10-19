import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("a=");
        int a = sc.nextInt();
        System.out.print("b=");
        int b = sc.nextInt();
        int product = a * b;
        double ratio = a / (double) b;
        System.out.println("product=" + product);
        System.out.printf("ratio=%.2f%n", ratio);
    }
}