import java.util.Scanner;

public class fifth {

    public static void main(String[] args) {

        Scanner scn = new Scanner(System.in);
        int t = scn.nextInt();
        for (int i = 0; i < t; i++) {
            int n = scn.nextInt();
            int k = scn.nextInt();
            int[] a = new int[k];
            int[] b = new int[k];
            for (int j = 0; j < k; j++)
                a[j] = scn.nextInt();
            for (int j = 0; j < k; j++)
                b[j] = scn.nextInt();
            fun(n, a, b);
        }

    }

    public static void fun(int n, int[] a, int[] b) {
        int[] ans = new int[n];
        for (int i = 0; i < n; i++)
            ans[i] = Integer.MAX_VALUE;

        for (int i = 0; i < a.length; i++)
            ans[a[i] - 1] = b[i];

        for (int i = 1; i < n; i++)
            if (ans[i - 1] != Integer.MAX_VALUE)
                ans[i] = Math.min(ans[i], ans[i - 1] + 1);

        for (int i = n - 2; i >= 0; i--)
            ans[i] = Math.min(ans[i], ans[i + 1] + 1);

        for (int ele : ans)
            System.out.print(ele + " ");
        System.out.println();
    }

}

