import java.util.Scanner;

// Чужая функция из base.java внутри своей программы.
public class Main {
    static long[] prefixSums(int[] values) {
        long[] sums = new long[values.length + 1];
        for (int i = 0; i < values.length; i++) {
            sums[i + 1] = sums[i] + values[i];
        }
        return sums;
    }

    static boolean isPalindrome(String text) {
        int left = 0;
        int right = text.length() - 1;
        while (left < right) {
            if (text.charAt(left) != text.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String word = in.next();
        if (isPalindrome(word)) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }
        int[] codes = word.chars().toArray();
        System.out.println(prefixSums(codes)[codes.length]);
    }
}
