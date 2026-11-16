import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.*;
import java.io.*;
public class Main {
    public static void main(String args[])throws Exception{
        Input sc=new Input();
        StringBuilder sb=new StringBuilder();
        int t=sc.readInt();
        for(int f=0;f<t;f++){
            int n=sc.readInt();
            String str[]=new String[n];
            for(int i=0;i<n;i++){
                str[i]=sc.readString();
            }
            char ch[]={'a','b','c','d','e'};
            int ans=0;
            for(int i=0;i<5;i++){
                char c=ch[i];
                ArrayList<pair> lst=new ArrayList<>();
                int a[]=new int[n];
                for(int j=0;j<n;j++)
                {
                    int count=0;
                    for(int k=0;k<str[j].length();k++){
                        if(str[j].charAt(k)==c)
                            count++;
                    }
                    lst.add(new pair(count,(str[j].length()-count)));
                }
                Collections.sort(lst);
                int s1=0;int s2=0;
//                for(int j=0;j<lst.size();j++){
//                    System.out.println(lst.get(j).a+" "+lst.get(j).b);
//                }
//                System.out.println("--------");
                int co=0;
                for(int j=0;j<lst.size();j++){
                    int v1=lst.get(j).a;
                    int v2=lst.get(j).b;
                    if((s1+v1)>(s2+v2)){
                        s1+=v1;
                        s2+=v2;
                        co++;
                    }
                    //System.out.println(lst.get(j).a+" "+lst.get(j).b);
                }
                ans=Math.max(co,ans);

            }
            // count all 'a' in each string
            sb.append(ans+"\n");
        }
        System.out.print(sb);
    }
}
class pair implements Comparable<pair>{
    int a;
    int b;
    pair(int a,int b){
        this.a=a;
        this.b=b;
    }

    @Override
    public int compareTo(pair o) {
//        if(this.a!=o.a){
//            return o.a-this.a;
//        }else
//            return this.b-o.b;
        return -(this.a-this.b)+(o.a-o.b);
    }
}
class Input{
    BufferedReader br;
    StringTokenizer st;
    Input(){
        br=new BufferedReader(new InputStreamReader(System.in));
        st=new StringTokenizer("");
    }
    public int[] readArray() throws Exception{
        st=new StringTokenizer(br.readLine());
        int a[]=new int[st.countTokens()];
        for(int i=0;i<a.length;i++){
            a[i]=Integer.parseInt(st.nextToken());
        }
        return a;
    }
    public long[] readArrayLong() throws Exception{
        st=new StringTokenizer(br.readLine());
        long a[]=new long[st.countTokens()];
        for(int i=0;i<a.length;i++){
            a[i]=Long.parseLong(st.nextToken());
        }
        return a;
    }
    public int readInt() throws Exception{
        st=new StringTokenizer(br.readLine());
        return Integer.parseInt(st.nextToken());
    }
    public long readLong() throws Exception{
        st=new StringTokenizer(br.readLine());
        return Long.parseLong(st.nextToken());
    }
    public String readString() throws Exception{
        return br.readLine();
    }
    public int[][] read2dArray(int n,int m)throws Exception{
        int a[][]=new int[n][m];
        for(int i=0;i<n;i++){
            st=new StringTokenizer(br.readLine());
            for(int j=0;j<m;j++){
                a[i][j]=Integer.parseInt(st.nextToken());
            }
        }
        return a;
    }
}


