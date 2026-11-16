import java.io.*;
import java.util.Arrays;
import java.util.StringTokenizer;
// نورت الكود يا كبير اتفضل

// يا رب Accepted
public class InterestingStory {
    private static int[] freq;
    private static String[] strs;

    private static int[] countAlpha(char alpha) {

        int[] count = new int[strs.length];

        for (int i = 0; i < strs.length; i++)
            for (char c : strs[i].toCharArray())
                count[i] += c == alpha ? -1 : 1;

        return count;
    }

    private static int solve(char alpha) {
        int[] res = countAlpha(alpha);

        Arrays.sort(res);

        int freqSum = 0;
        for (int j : freq) freqSum += j;

        freqSum -= freq[alpha - 'a'];


        int k = res.length - 1;
        

        while (k >= 0 && freq[alpha - 'a'] <= freqSum)
            freqSum -= res[k--];
        
        return k + 1;
    }

    public static void main(String[] args) {
        FastReader in = new FastReader();
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

        int t = in.nextInt();
        while (t-- > 0) {

            int n = in.nextInt();
            freq = new int[5];
            strs = new String[n];

            for (int i = 0; i < n; i++)
                strs[i] = in.nextLine();

            for (String str : strs)
                for (char c : str.toCharArray())
                    freq[c - 'a']++;

            int max = 0;

            for (char c = 'a'; c < 'f'; c++)
                max = Math.max(max, solve(c));


            out.println(max);

        }

        out.close();
    }

    private static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        FastReader() {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        String nextLine() {
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }
}