import java.util.ArrayList;
import java.util.List;

class Strings {
    static int[] prefixFunction(String s) {
        int n = s.length();
        int[] pi = new int[n];
        for (int i = 1; i < n; i++) {
            int j = pi[i - 1];
            while (j > 0 && s.charAt(i) != s.charAt(j)) {
                j = pi[j - 1];
            }
            if (s.charAt(i) == s.charAt(j)) {
                j++;
            }
            pi[i] = j;
        }
        return pi;
    }

    static List<Integer> kmpSearch(String text, String pattern) {
        int[] pi = prefixFunction(pattern);
        List<Integer> result = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            if (j == pattern.length()) {
                result.add(i - j + 1);
                j = pi[j - 1];
            }
        }
        return result;
    }

    static int[] zFunction(String s) {
        int n = s.length();
        int[] z = new int[n];
        int left = 0;
        int right = 0;
        for (int i = 1; i < n; i++) {
            if (i < right) {
                z[i] = Math.min(right - i, z[i - left]);
            }
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] > right) {
                left = i;
                right = i + z[i];
            }
        }
        return z;
    }

    static boolean isPalindrome(String s) {
        int i = 0;
        int j = s.length() - 1;
        while (i < j) {
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
            i++;
            j--;
        }
        return true;
    }

    static String reverse(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            result.append(s.charAt(i));
        }
        return result.toString();
    }

    static int[] manacher(String s) {
        int n = s.length();
        int[] odd = new int[n];
        int left = 0;
        int right = -1;
        for (int i = 0; i < n; i++) {
            int k = i > right ? 1 : Math.min(odd[left + right - i], right - i + 1);
            while (i + k < n && i - k >= 0 && s.charAt(i + k) == s.charAt(i - k)) {
                k++;
            }
            odd[i] = k;
            if (i + k - 1 > right) {
                left = i - k + 1;
                right = i + k - 1;
            }
        }
        return odd;
    }

    static long[] polynomialHash(String s, long base, long mod) {
        long[] hash = new long[s.length() + 1];
        for (int i = 0; i < s.length(); i++) {
            hash[i + 1] = (hash[i] * base + s.charAt(i)) % mod;
        }
        return hash;
    }

    static int[] letterCounts(String s) {
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) - 'a']++;
        }
        return count;
    }
}
