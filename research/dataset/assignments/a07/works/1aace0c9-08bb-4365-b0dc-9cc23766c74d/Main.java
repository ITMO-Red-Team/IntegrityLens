
import java.util.*;

import java.lang.*;
import java.io.*;

/* Name of the class has to be "Main" only if the class is public. */
public final class Solution {
    

    public static void main(String[] args) throws Exception {
        
        Reader sc = new Reader();
        BufferedWriter op = new BufferedWriter(new OutputStreamWriter(System.out));
         
         int n=sc.nextInt();
          ArrayList<Integer> fill= new ArrayList<Integer>();
          ArrayList<Integer> unfilled= new ArrayList<>();
          for(int i=0;i<n;i++){
              int x =sc.nextInt();
              if(x==1){
                  fill.add(i);
              }else{
                  unfilled.add(i);
              }
          }
          Collections.sort(fill);
          Collections.sort(unfilled);
          
          
          long[][] dp =new long[fill.size()+1][unfilled.size()+1];
           for(int i=0;i<fill.size()+1;i++){
              for(int j=0;j<unfilled.size()+1;j++){
                  dp[i][j]=Integer.MAX_VALUE;
              }
          }
          for(int i=0;i<unfilled.size()+1;i++){
              dp[0][i]=0;
          }
        //   for(int j=0;j<fill.size()+1;j++){
        //       dp[j][0]=0;
        //   }
         

          for(int i=1;i<fill.size()+1;i++){
              for(int j=1;j<unfilled.size()+1;j++){
                  dp[i][j]=Math.min(dp[i][j-1],dp[i-1][j-1]+Math.abs(fill.get(i-1)-unfilled.get(j-1)));
              }
          }
          System.out.println(dp[fill.size()][unfilled.size()]);
        //   for(int i=0;i<fill.size()+1;i++){
        //       for(int j=0;j<unfilled.size()+1;j++)
        //       {
        //           System.out.print(dp[i][j]+" ");
        //       }
        //       System.out.println();
        //   }
         
    }




   
    }

class Reader {
    final private int BUFFER_SIZE = 1 << 16;
    private DataInputStream din;
    private byte[] buffer;
    private int bufferPointer, bytesRead;

    public Reader() {
        din = new DataInputStream(System.in);
        buffer = new byte[BUFFER_SIZE];
        bufferPointer = bytesRead = 0;
    }

    public Reader(String file_name) throws IOException {
        din = new DataInputStream(new FileInputStream(file_name));
        buffer = new byte[BUFFER_SIZE];
        bufferPointer = bytesRead = 0;
    }

    public String readLine() throws IOException {
        byte[] buf = new byte[64]; // line length
        int cnt = 0, c;
        while ((c = read()) != -1) {
            if (c == '\n') {
                if (cnt != 0) {
                    break;
                } else {
                    continue;
                }
            }
            buf[cnt++] = (byte) c;
        }
        return new String(buf, 0, cnt);
    }

    public int nextInt() throws IOException {
        int ret = 0;
        byte c = read();
        while (c <= ' ') {
            c = read();
        }
        boolean neg = (c == '-');
        if (neg)
            c = read();
        do {
            ret = ret * 10 + c - '0';
        } while ((c = read()) >= '0' && c <= '9');

        if (neg)
            return -ret;
        return ret;
    }

    public long nextLong() throws IOException {
        long ret = 0;
        byte c = read();
        while (c <= ' ')
            c = read();
        boolean neg = (c == '-');
        if (neg)
            c = read();
        do {
            ret = ret * 10 + c - '0';
        } while ((c = read()) >= '0' && c <= '9');
        if (neg)
            return -ret;
        return ret;
    }

    public double nextDouble() throws IOException {
        double ret = 0, div = 1;
        byte c = read();
        while (c <= ' ')
            c = read();
        boolean neg = (c == '-');
        if (neg)
            c = read();

        do {
            ret = ret * 10 + c - '0';
        } while ((c = read()) >= '0' && c <= '9');

        if (c == '.') {
            while ((c = read()) >= '0' && c <= '9') {
                ret += (c - '0') / (div *= 10);
            }
        }

        if (neg)
            return -ret;
        return ret;
    }

    private void fillBuffer() throws IOException {
        bytesRead = din.read(buffer, bufferPointer = 0, BUFFER_SIZE);
        if (bytesRead == -1)
            buffer[0] = -1;
    }

    private byte read() throws IOException {
        if (bufferPointer == bytesRead)
            fillBuffer();
        return buffer[bufferPointer++];
    }

    public void close() throws IOException {
        if (din == null)
            return;
        din.close();
    }
}

