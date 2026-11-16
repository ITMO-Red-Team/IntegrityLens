// package Div3_734;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class ProblemC {
    public static void main(String[] args)throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        StringBuilder print=new StringBuilder();
        int test=Integer.parseInt(br.readLine());
        while(test--!=0){
            int n=Integer.parseInt(br.readLine());
            ArrayList<Story> stories[]=new ArrayList[5];
            for(int i=0;i<5;i++){
                stories[i]=new ArrayList<>();
            }
            for(int i=1;i<=n;i++){
                char c[]=br.readLine().toCharArray();
                int freq[]=new int[5];
                for(int j=0;j<c.length;j++){
                    freq[c[j]-97]++;
                }
                for(int j=0;j<5;j++){
                    stories[j].add(new Story(freq[j],c.length-freq[j]));
                }
            }
            for(int i=0;i<5;i++){
                Collections.sort(stories[i]);
            }
            int max=0;
            for(int i=0;i<5;i++){
                int def=0;
                int count=0;
                for(Story story:stories[i]){
                    int diff=story.diff;
                    if(def+diff<=0){
                        break;
                    }
                    else{
                        def+=diff;
                        count++;
                    }
                }
                max=Math.max(max,count);
            }
            print.append(max+"\n");
        }
        System.out.print(print);
    }
}

class Story implements Comparable<Story>{
    int x,y,diff;
    public Story(int x,int y){
        this.x=x;
        this.y=y;
        this.diff=this.x-this.y;
    }

    @Override
    public int compareTo(Story o) {
        return -this.diff+o.diff;
    }
}