import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Numbers {
    static final int MOD = 1_000_000_007;

    static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    static long gcdIf(long a, long b) {
        if (b == 0) {
            return a;
        }
        return gcdIf(b, a % b);
    }

    static long gcdSwapped(long a, long b) {
        if (a == 0) {
            return b;
        }
        return gcdSwapped(b % a, a);
    }

    static long gcdLoop(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    static long lcm(long a, long b) {
        return a / gcdLoop(a, b) * b;
    }

    static long lcmProduct(long a, long b) {
        return (a * b) / gcdLoop(a, b);
    }

    static int ceilDivide(int x, int y) {
        return x % y == 0 ? x / y : x / y + 1;
    }

    static boolean isPowerOfTwo(long n) {
        return n != 0 && (n & (n - 1)) == 0;
    }

    static int countDigits(long n) {
        return (int) Math.floor(Math.log10(n) + 1);
    }

    static long log2(long n) {
        return (long) (Math.log(n) / Math.log(2));
    }

    static long normalize(long x, long mod) {
        return (x % mod + mod) % mod;
    }

    static long addMod(long a, long b, long mod) {
        return normalize(normalize(a, mod) + normalize(b, mod), mod);
    }

    static long[] extendedGcd(long a, long b) {
        if (b == 0) {
            return new long[] {a, 1, 0};
        }
        long[] r = extendedGcd(b, a % b);
        return new long[] {r[0], r[2], r[1] - a / b * r[2]};
    }

    static long power(long base, long exp) {
        long result = 1;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result *= base;
            }
            base *= base;
            exp >>= 1;
        }
        return result;
    }

    static long modPow(long base, long exp, long mod) {
        long result = 1;
        base %= mod;
        while (exp > 0) {
            if (exp % 2 == 1) {
                result = result * base % mod;
            }
            base = base * base % mod;
            exp /= 2;
        }
        return result;
    }

    static long modPowBits(long a, long b, long mod) {
        long result = 1;
        a = a % mod;
        while (b > 0) {
            if ((b & 1) != 0) {
                result = (result * a) % mod;
            }
            a = (a * a) % mod;
            b = b >> 1;
        }
        return result;
    }

    static long powerShift(long x, long y) {
        long result = 1;
        while (y > 0) {
            if (y % 2 == 1) {
                result = result * x;
            }
            y >>= 1;
            x = x * x;
        }
        return result;
    }

    static long modPowHalves(long base, long n, long mod) {
        if (n == 0) {
            return 1;
        }
        if (n == 1) {
            return base % mod;
        }
        long half = modPowHalves(base, n / 2, mod);
        if (n % 2 == 0) {
            return (half * half) % mod;
        } else {
            return (((half * half) % mod) * base) % mod;
        }
    }

    static long modPowRecursive(long base, long exp, long mod) {
        if (exp == 0) {
            return 1;
        }
        long half = modPowRecursive(base, exp / 2, mod);
        long result = half * half % mod;
        if (exp % 2 == 1) {
            result = result * base % mod;
        }
        return result;
    }

    static long modInverse(long a, long mod) {
        return modPow(a, mod - 2, mod);
    }

    static boolean isPrime(long n) {
        if (n < 2) {
            return false;
        }
        for (long i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    static boolean isPrimeSixK(int n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    static boolean[] sieve(int n) {
        boolean[] prime = new boolean[n + 1];
        Arrays.fill(prime, true);
        prime[0] = false;
        prime[1] = false;
        for (int i = 2; i * i <= n; i++) {
            if (prime[i]) {
                for (int j = i * i; j <= n; j += i) {
                    prime[j] = false;
                }
            }
        }
        return prime;
    }

    static List<Integer> primesUpTo(int n) {
        boolean[] composite = new boolean[n + 1];
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            if (!composite[i]) {
                primes.add(i);
                for (long j = (long) i * i; j <= n; j += i) {
                    composite[(int) j] = true;
                }
            }
        }
        return primes;
    }

    static int[] smallestPrimeFactor(int n) {
        int[] spf = new int[n + 1];
        for (int i = 2; i <= n; i++) {
            if (spf[i] == 0) {
                for (int j = i; j <= n; j += i) {
                    if (spf[j] == 0) {
                        spf[j] = i;
                    }
                }
            }
        }
        return spf;
    }

    static List<Long> factorize(long n) {
        List<Long> factors = new ArrayList<>();
        for (long i = 2; i * i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        if (n > 1) {
            factors.add(n);
        }
        return factors;
    }

    static List<Integer> divisors(int n) {
        List<Integer> result = new ArrayList<>();
        for (int i = 1; i * i <= n; i++) {
            if (n % i == 0) {
                result.add(i);
                if (i != n / i) {
                    result.add(n / i);
                }
            }
        }
        return result;
    }

    static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    static long factorialRecursive(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorialRecursive(n - 1);
    }

    static long[] inverses(int n, long p) {
        long[] inverse = new long[n + 1];
        inverse[0] = inverse[1] = 1;
        for (int i = 2; i <= n; i++) {
            inverse[i] = inverse[(int) (p % i)] * (p - p / i) % p;
        }
        return inverse;
    }

    static long[] factorials(int n, long mod) {
        long[] fact = new long[n + 1];
        fact[0] = 1;
        for (int i = 1; i <= n; i++) {
            fact[i] = fact[i - 1] * i % mod;
        }
        return fact;
    }

    static long nCr(int n, int r, long[] fact, long mod) {
        if (r < 0 || r > n) {
            return 0;
        }
        return fact[n] * modInverse(fact[r], mod) % mod * modInverse(fact[n - r], mod) % mod;
    }

    static long[][] pascal(int n, long mod) {
        long[][] c = new long[n + 1][n + 1];
        for (int i = 0; i <= n; i++) {
            c[i][0] = 1;
            for (int j = 1; j <= i; j++) {
                c[i][j] = (c[i - 1][j - 1] + c[i - 1][j]) % mod;
            }
        }
        return c;
    }

    static long fibonacci(int n) {
        long a = 0;
        long b = 1;
        for (int i = 0; i < n; i++) {
            long c = a + b;
            a = b;
            b = c;
        }
        return a;
    }

    static int fibonacciRecursive(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2);
    }

    static int digitSum(long n) {
        int sum = 0;
        while (n > 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }

    static long reverseNumber(long n) {
        long result = 0;
        while (n > 0) {
            result = result * 10 + n % 10;
            n /= 10;
        }
        return result;
    }

    static long[][] multiply(long[][] a, long[][] b, long mod) {
        int n = a.length;
        long[][] c = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    c[i][j] = (c[i][j] + a[i][k] * b[k][j]) % mod;
                }
            }
        }
        return c;
    }

    static long[][] matrixPower(long[][] a, long exp, long mod) {
        int n = a.length;
        long[][] result = new long[n][n];
        for (int i = 0; i < n; i++) {
            result[i][i] = 1;
        }
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = multiply(result, a, mod);
            }
            a = multiply(a, a, mod);
            exp >>= 1;
        }
        return result;
    }
}
