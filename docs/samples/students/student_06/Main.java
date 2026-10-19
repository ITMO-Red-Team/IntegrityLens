public class Main {
    public static void main(String[] args) {
        int n = 20;
        long total = 0;
        for (int i = 1; i <= n; i++) {
            total += i * i;
        }
        System.out.println("sum of squares (1.." + n + ") = " + total);
    }
}