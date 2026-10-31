import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

// Другая задача и те же шаблонные алгоритмы: другие имена и оформление, другой порядок.
public class Main {
    static long power(long a, long n, long m) {
        long answer = 1;
        a %= m;
        while (n > 0) {
            if (n % 2 == 1) answer = answer * a % m;
            a = a * a % m;
            n /= 2;
        }
        return answer;
    }

    static int firstNotLess(int[] data, int x) {
        int left = 0, right = data.length;
        while (left < right) {
            int middle = (left + right) / 2;
            if (data[middle] < x) left = middle + 1;
            else right = middle;
        }
        return left;
    }

    static class Reader {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tokens;

        int nextInt() {
            return Integer.parseInt(next());
        }

        String next() {
            while (tokens == null || !tokens.hasMoreElements()) {
                try {
                    tokens = new StringTokenizer(input.readLine());
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
            return tokens.nextToken();
        }
    }

    static String packRuns(String text) {
        StringBuilder packed = new StringBuilder();
        char previous = 0;
        int length = 0;
        for (char symbol : text.toCharArray()) {
            if (symbol == previous && length < 9) {
                length++;
                continue;
            }
            if (length > 0) {
                packed.append(previous).append(length > 1 ? String.valueOf(length) : "");
            }
            previous = Character.toLowerCase(symbol);
            length = 1;
        }
        return packed.append(previous).append(length).toString();
    }

    public static void main(String[] args) {
        Reader reader = new Reader();
        String line = reader.next();
        int shift = reader.nextInt();
        int[] codes = packRuns(line).chars().sorted().toArray();
        System.out.println(power(firstNotLess(codes, shift) + 2L, shift, 998_244_353L));
    }
}
