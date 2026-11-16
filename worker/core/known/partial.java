import java.util.Scanner;

// Чужая функция из base.java внутри своей программы.
public class Main {
    static long[] weightedSums(int[] values) {
        long[] sums = new long[values.length + 1];
        long weight = 1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] % 3 == 0) {
                weight += values[i] / 3;
            } else if (weight > 100) {
                weight = weight / 2 + 1;
            }
            long bonus = i % 2 == 0 ? weight * 2 : -weight;
            sums[i + 1] = sums[i] + values[i] * weight + bonus;
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
        System.out.println(weightedSums(codes)[codes.length]);
    }
}
