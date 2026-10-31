import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class DynamicProgramming {
    static long kadane(int[] a) {
        long best = a[0];
        long current = a[0];
        for (int i = 1; i < a.length; i++) {
            current = Math.max(a[i], current + a[i]);
            best = Math.max(best, current);
        }
        return best;
    }

    static int lisQuadratic(int[] a) {
        int n = a.length;
        int[] dp = new int[n];
        int best = 0;
        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            for (int j = 0; j < i; j++) {
                if (a[j] < a[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            best = Math.max(best, dp[i]);
        }
        return best;
    }

    static int lis(int[] a) {
        List<Integer> tails = new ArrayList<>();
        for (int x : a) {
            int position = Collections.binarySearch(tails, x);
            if (position < 0) {
                position = -position - 1;
            }
            if (position == tails.size()) {
                tails.add(x);
            } else {
                tails.set(position, x);
            }
        }
        return tails.size();
    }

    static int lcs(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[n][m];
    }

    static int editDistance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[n][m];
    }

    static int knapsackTable(int[] weight, int[] value, int capacity) {
        int n = weight.length;
        int[][] dp = new int[n + 1][capacity + 1];
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                dp[i][w] = dp[i - 1][w];
                if (w >= weight[i - 1]) {
                    dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - weight[i - 1]] + value[i - 1]);
                }
            }
        }
        return dp[n][capacity];
    }

    static long knapsack(int[] weight, int[] value, int capacity) {
        long[] dp = new long[capacity + 1];
        for (int i = 0; i < weight.length; i++) {
            for (int w = capacity; w >= weight[i]; w--) {
                dp[w] = Math.max(dp[w], dp[w - weight[i]] + value[i]);
            }
        }
        return dp[capacity];
    }

    static long coinWays(int[] coins, int amount) {
        long[] dp = new long[amount + 1];
        dp[0] = 1;
        for (int coin : coins) {
            for (int i = coin; i <= amount; i++) {
                dp[i] += dp[i - coin];
            }
        }
        return dp[amount];
    }

    static int minCoins(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (coin <= i && dp[i - coin] != Integer.MAX_VALUE) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }
        return dp[amount];
    }

    static long[] prefixSums(int[] a) {
        long[] sums = new long[a.length + 1];
        for (int i = 0; i < a.length; i++) {
            sums[i + 1] = sums[i] + a[i];
        }
        return sums;
    }

    static long[][] prefixSums2d(int[][] a) {
        int n = a.length;
        int m = a[0].length;
        long[][] sums = new long[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                sums[i][j] = sums[i - 1][j] + sums[i][j - 1] - sums[i - 1][j - 1] + a[i - 1][j - 1];
            }
        }
        return sums;
    }

    static int countSubsets(int[] a, long target) {
        int n = a.length;
        int count = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            long sum = 0;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    sum += a[i];
                }
            }
            if (sum == target) {
                count++;
            }
        }
        return count;
    }

    static boolean nextPermutation(int[] a) {
        int i = a.length - 2;
        while (i >= 0 && a[i] >= a[i + 1]) {
            i--;
        }
        if (i < 0) {
            return false;
        }
        int j = a.length - 1;
        while (a[j] <= a[i]) {
            j--;
        }
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
        for (int l = i + 1, r = a.length - 1; l < r; l++, r--) {
            t = a[l];
            a[l] = a[r];
            a[r] = t;
        }
        return true;
    }

    static void permutations(int[] a, int k, List<int[]> result) {
        if (k == a.length) {
            result.add(a.clone());
            return;
        }
        for (int i = k; i < a.length; i++) {
            int t = a[k];
            a[k] = a[i];
            a[i] = t;
            permutations(a, k + 1, result);
            t = a[k];
            a[k] = a[i];
            a[i] = t;
        }
    }

    static int queens(int row, int n, boolean[] column, boolean[] diagonal, boolean[] anti) {
        if (row == n) {
            return 1;
        }
        int count = 0;
        for (int c = 0; c < n; c++) {
            if (!column[c] && !diagonal[row + c] && !anti[row - c + n]) {
                column[c] = true;
                diagonal[row + c] = true;
                anti[row - c + n] = true;
                count += queens(row + 1, n, column, diagonal, anti);
                column[c] = false;
                diagonal[row + c] = false;
                anti[row - c + n] = false;
            }
        }
        return count;
    }

    static long fibonacciMemo(int n, long[] memo) {
        if (n <= 1) {
            return n;
        }
        if (memo[n] != 0) {
            return memo[n];
        }
        memo[n] = fibonacciMemo(n - 1, memo) + fibonacciMemo(n - 2, memo);
        return memo[n];
    }
}
