import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class C {
    static char[] alphabet = {'a', 'b', 'c', 'd', 'e'};

    public static void main(String[] args) {
        Scanner scn = new Scanner(System.in);
        int test = scn.nextInt();
        for (int t = 0; t < test; t++) {
            int n = scn.nextInt();
            String[] words = new String[n];
            for (int i = 0; i < n; i++) {
                words[i] = scn.next();
            }
            int maxWords = 0;
            for (char target : alphabet) {
                Integer[] diff = new Integer[n];
                for (int i = 0; i < n; i++) {
                    String word = words[i];
                    diff[i] = wordDiff(word, target);
                }
                Arrays.sort(diff, Collections.reverseOrder());
                int pointer = 0;
                int sum = 0;
                for (; pointer < n && sum > -diff[pointer]; pointer++) {
                    sum += diff[pointer];
                }
                maxWords = Math.max(pointer, maxWords);
            }
            System.out.println(maxWords);
        }
    }

    static int wordDiff(String a, char target) {
        int ans = 0;
        for (char c : a.toCharArray()) {
            if (c == target) ans++;
        }
        return ans * 2 - a.length();
    }
}
