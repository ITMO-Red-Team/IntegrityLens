    //import java.io.IOException;

    import java.io.IOException;
    import java.io.InputStream;
    import java.io.PrintWriter;
    import java.util.InputMismatchException;
    import java.util.PriorityQueue;

    public class InterestingStory {
        static InputReader inputReader=new InputReader(System.in);
        static void solve()
        {
            int n=inputReader.nextInt();
            String arr[]=new String[n];
            for (int i=0;i<n;i++)
            {
                arr[i]=inputReader.readString();
            }
            int larr[]=new int[n];
            for (int i=0;i<n;i++) {
                larr[i] = arr[i].length();
            }
            int freqarr[][]=new int[n][5];
            for (int i=0;i<n;i++)
            {
                freqarr[i]=freq(arr[i],larr[i]);
            }
            int max=Integer.MIN_VALUE;
            for (char c='a';c<='e';c++)
            {
                max=Math.max(max,helper(freqarr,larr,n,c));
            }
            out.println(max);
        }
        static  int helper(int freqarr[][],int larr[],int n,char c)
        {
            int sum=0;
            PriorityQueue<Integer>priorityQueue=new PriorityQueue<>();
            for (int i=0;i<n;i++)
            {
                int score=freqarr[i][c-'a']-(larr[i]-freqarr[i][c-'a']);
                priorityQueue.add(score);
                sum+=score;
            }
            while (!priorityQueue.isEmpty()&&sum<=0) {
                sum -= priorityQueue.poll();
            }
            return priorityQueue.size();
        }
       static int []freq(String str,int len)
        {
            int arr[]=new int[5];
            for (int i=0;i<len;i++)
            {
                arr[str.charAt(i)-'a']++;
            }
            return arr;
        }
        static PrintWriter out=new PrintWriter((System.out));
        public static void main(String args[])throws IOException
        {
            int t=inputReader.nextInt();
            while(t-->0)
            {
                solve();
            }
            out.close();
        }
        static class InputReader {

            private InputStream stream;
            private byte[] buf = new byte[8192];
            private int curChar, snumChars;
            private SpaceCharFilter filter;

            public InputReader(InputStream stream) {
                this.stream = stream;
            }

            public int snext() {
                if (snumChars == -1)
                    throw new InputMismatchException();
                if (curChar >= snumChars) {
                    curChar = 0;
                    try {
                        snumChars = stream.read(buf);
                    } catch (IOException e) {
                        throw new InputMismatchException();
                    }
                    if (snumChars <= 0)
                        return -1;
                }
                return buf[curChar++];
            }

            public int nextInt() {
                int c = snext();
                while (isSpaceChar(c))
                    c = snext();
                int sgn = 1;
                if (c == '-') {
                    sgn = -1;
                    c = snext();
                }
                int res = 0;
                do {
                    if (c < '0' || c > '9')
                        throw new InputMismatchException();
                    res *= 10;
                    res += c - '0';
                    c = snext();
                } while (!isSpaceChar(c));
                return res * sgn;
            }

            public long nextLong() {
                int c = snext();
                while (isSpaceChar(c))
                    c = snext();
                int sgn = 1;
                if (c == '-') {
                    sgn = -1;
                    c = snext();
                }
                long res = 0;
                do {
                    if (c < '0' || c > '9')
                        throw new InputMismatchException();
                    res *= 10;
                    res += c - '0';
                    c = snext();
                } while (!isSpaceChar(c));
                return res * sgn;
            }

            public int[] nextIntArray(int n) {
                int a[] = new int[n];
                for (int i = 0; i < n; i++)
                    a[i] = nextInt();
                return a;
            }

            public String readString() {
                int c = snext();
                while (isSpaceChar(c))
                    c = snext();
                StringBuilder res = new StringBuilder();
                do {
                    res.appendCodePoint(c);
                    c = snext();
                } while (!isSpaceChar(c));
                return res.toString();
            }

            public boolean isSpaceChar(int c) {
                if (filter != null)
                    return filter.isSpaceChar(c);
                return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
            }

            public interface SpaceCharFilter {
                public boolean isSpaceChar(int ch);
            }
        }
    }
