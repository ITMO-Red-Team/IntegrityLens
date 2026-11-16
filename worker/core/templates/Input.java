import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.StringTokenizer;

class FastReader {
    BufferedReader br;
    StringTokenizer st;

    public FastReader() {
        br = new BufferedReader(new InputStreamReader(System.in));
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

    long nextLong() {
        return Long.parseLong(next());
    }

    double nextDouble() {
        return Double.parseDouble(next());
    }

    String nextLine() {
        String line = "";
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    int[] readArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = nextInt();
        }
        return a;
    }
}

class FastScanner {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    StringTokenizer st = new StringTokenizer("");

    String next() {
        while (!st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return st.nextToken();
    }

    int nextInt() {
        return Integer.parseInt(next());
    }

    long nextLong() {
        return Long.parseLong(next());
    }

    int[] readArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = nextInt();
        }
        return a;
    }

    long[] readLongArray(int n) {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) {
            a[i] = nextLong();
        }
        return a;
    }
}

class TokenReader {
    BufferedReader reader;
    StringTokenizer tokenizer;

    public TokenReader(InputStream stream) {
        reader = new BufferedReader(new InputStreamReader(stream));
        tokenizer = null;
    }

    public TokenReader() {
        try {
            reader = new BufferedReader(new FileReader("input.txt"));
            PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
            System.setOut(out);
        } catch (Exception e) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        }
    }

    public String next() {
        while (tokenizer == null || !tokenizer.hasMoreTokens()) {
            try {
                tokenizer = new StringTokenizer(reader.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tokenizer.nextToken();
    }

    public String nextOrThrow() throws IOException {
        while (tokenizer == null || !tokenizer.hasMoreTokens()) {
            tokenizer = new StringTokenizer(reader.readLine());
        }
        return tokenizer.nextToken();
    }

    public String nextLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public char nextChar() throws IOException {
        return nextOrThrow().charAt(0);
    }
}

class ByteReader {
    private static final char NONE = (char) 0;
    private final BufferedInputStream in = new BufferedInputStream(System.in);
    private final byte[] buf = new byte[1 << 16];
    private int size;
    private int position;
    private char current = NONE;
    private double scale = 1;

    private char getChar() {
        while (position == size) {
            try {
                size = in.read(buf);
            } catch (Exception e) {
                return NONE;
            }
            if (size == -1) {
                return NONE;
            }
            position = 0;
        }
        return (char) buf[position++];
    }

    public boolean hasNext() {
        if (current > 32) {
            return true;
        }
        while (true) {
            current = getChar();
            if (current == NONE) {
                return false;
            } else if (current > 32) {
                return true;
            }
        }
    }

    public String next() {
        StringBuilder result = new StringBuilder();
        while (current <= 32) {
            current = getChar();
        }
        while (current > 32) {
            result.append(current);
            current = getChar();
        }
        return result.toString();
    }

    public long nextLong() {
        scale = 1;
        boolean negative = false;
        if (current == NONE) {
            current = getChar();
        }
        for (; current < '0' || current > '9'; current = getChar()) {
            if (current == '-') {
                negative = true;
            }
        }
        long result = 0;
        for (; current >= '0' && current <= '9'; current = getChar()) {
            result = (result << 3) + (result << 1) + current - '0';
            scale *= 10;
        }
        return negative ? -result : result;
    }

    public double nextDouble() {
        double value = nextLong();
        return current != '.' ? value : value + nextLong() / scale;
    }
}

class ScanReader {
    private final InputStream in = System.in;
    private final byte[] buffer = new byte[1 << 16];
    private int index;
    private int total;

    int scan() throws IOException {
        if (index >= total) {
            index = 0;
            total = in.read(buffer);
            if (total <= 0) {
                return -1;
            }
        }
        return buffer[index++];
    }

    int nextInt() throws IOException {
        int c;
        int value = 0;
        for (c = scan(); c <= 32; c = scan());
        boolean negative = c == '-';
        if (c == '-' || c == '+') {
            c = scan();
        }
        for (; c >= '0' && c <= '9'; c = scan()) {
            value = (value << 3) + (value << 1) + (c & 15);
        }
        return negative ? -value : value;
    }

    String next() throws IOException {
        int c;
        for (c = scan(); c <= 32; c = scan());
        StringBuilder word = new StringBuilder();
        for (; c > 32; c = scan()) {
            word.append((char) c);
        }
        return word.toString();
    }
}

class InputReader {
    private final InputStream stream;
    private final byte[] buf = new byte[1 << 16];
    private int curChar;
    private int numChars;

    InputReader(InputStream stream) {
        this.stream = stream;
    }

    int read() {
        if (numChars == -1) {
            throw new InputMismatchException();
        }
        if (curChar >= numChars) {
            curChar = 0;
            try {
                numChars = stream.read(buf);
            } catch (IOException e) {
                throw new InputMismatchException();
            }
            if (numChars <= 0) {
                return -1;
            }
        }
        return buf[curChar++];
    }

    boolean isSpaceChar(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
    }

    int nextInt() {
        int c = read();
        while (isSpaceChar(c)) {
            c = read();
        }
        int sign = 1;
        if (c == '-') {
            sign = -1;
            c = read();
        }
        int result = 0;
        do {
            if (c < '0' || c > '9') {
                throw new InputMismatchException();
            }
            result = result * 10 + c - '0';
            c = read();
        } while (!isSpaceChar(c));
        return result * sign;
    }

    long nextLong() {
        int c = read();
        while (isSpaceChar(c)) {
            c = read();
        }
        int sign = 1;
        if (c == '-') {
            sign = -1;
            c = read();
        }
        long result = 0;
        do {
            if (c < '0' || c > '9') {
                throw new InputMismatchException();
            }
            result = result * 10 + c - '0';
            c = read();
        } while (!isSpaceChar(c));
        return result * sign;
    }

    double nextDouble() {
        int c = read();
        while (isSpaceChar(c)) {
            c = read();
        }
        int sign = 1;
        if (c == '-') {
            sign = -1;
            c = read();
        }
        double result = 0;
        while (!isSpaceChar(c) && c != '.') {
            if (c == 'e' || c == 'E') {
                return result * Math.pow(10, nextInt());
            }
            if (c < '0' || c > '9') {
                throw new InputMismatchException();
            }
            result *= 10;
            result += c - '0';
            c = read();
        }
        if (c == '.') {
            c = read();
            double m = 1;
            while (!isSpaceChar(c)) {
                if (c == 'e' || c == 'E') {
                    return result * Math.pow(10, nextInt());
                }
                if (c < '0' || c > '9') {
                    throw new InputMismatchException();
                }
                m /= 10;
                result += (c - '0') * m;
                c = read();
            }
        }
        return result * sign;
    }

    String next() {
        int c = read();
        while (isSpaceChar(c)) {
            c = read();
        }
        StringBuilder word = new StringBuilder();
        do {
            word.appendCodePoint(c);
            c = read();
        } while (!isSpaceChar(c));
        return word.toString();
    }
}

class DataReader {
    private static final int BUFFER_SIZE = 1 << 16;
    private DataInputStream din;
    private byte[] buffer;
    private int bufferPointer;
    private int bytesRead;

    public DataReader() {
        din = new DataInputStream(System.in);
        buffer = new byte[BUFFER_SIZE];
        bufferPointer = bytesRead = 0;
    }

    public DataReader(String name) throws IOException {
        din = new DataInputStream(new FileInputStream(name));
        buffer = new byte[BUFFER_SIZE];
        bufferPointer = bytesRead = 0;
    }

    public String readLine() throws IOException {
        byte[] line = new byte[64];
        int count = 0;
        int c;
        while ((c = read()) != -1) {
            if (c == '\n') {
                if (count != 0) {
                    break;
                } else {
                    continue;
                }
            }
            line[count++] = (byte) c;
        }
        return new String(line, 0, count);
    }

    public double nextDouble() throws IOException {
        double result = 0;
        double div = 1;
        byte c = read();
        while (c <= ' ') {
            c = read();
        }
        boolean negative = c == '-';
        if (negative) {
            c = read();
        }
        do {
            result = result * 10 + c - '0';
        } while ((c = read()) >= '0' && c <= '9');
        if (c == '.') {
            while ((c = read()) >= '0' && c <= '9') {
                result += (c - '0') / (div *= 10);
            }
        }
        if (negative) {
            return -result;
        }
        return result;
    }

    public void close() throws IOException {
        if (din == null) {
            return;
        }
        din.close();
    }

    private void fillBuffer() throws IOException {
        bytesRead = din.read(buffer, bufferPointer = 0, BUFFER_SIZE);
        if (bytesRead == -1) {
            buffer[0] = -1;
        }
    }

    private byte read() throws IOException {
        if (bufferPointer == bytesRead) {
            fillBuffer();
        }
        return buffer[bufferPointer++];
    }

    int nextInt() throws IOException {
        int result = 0;
        byte b = read();
        while (b <= ' ') {
            b = read();
        }
        boolean negative = b == '-';
        if (negative) {
            b = read();
        }
        do {
            result = result * 10 + b - '0';
        } while ((b = read()) >= '0' && b <= '9');
        return negative ? -result : result;
    }

    long nextLong() throws IOException {
        long result = 0;
        byte b = read();
        while (b <= ' ') {
            b = read();
        }
        boolean negative = b == '-';
        if (negative) {
            b = read();
        }
        do {
            result = result * 10 + b - '0';
        } while ((b = read()) >= '0' && b <= '9');
        return negative ? -result : result;
    }
}
