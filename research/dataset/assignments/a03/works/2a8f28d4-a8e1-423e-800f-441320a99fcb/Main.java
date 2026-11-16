import java.util.*;
import java.io.*;

public class E_Air_Conditioners{
    public static void main(String[] args) {
        FastScanner s= new FastScanner();
        StringBuilder res = new StringBuilder();
        int t=s.nextInt();
        int p=0;
        while(p<t){
        int n=s.nextInt();
        int k=s.nextInt();
        int pos[]= new int[k];
        int temp[]= new int[k];
        int min=Integer.MAX_VALUE;
        int ans[]= new int[n];
        HashMap<Integer,ArrayList<Integer>> map = new HashMap<Integer,ArrayList<Integer>>();
            HashMap<Integer,Integer> count1 = new  HashMap<Integer,Integer> ();
        for(int i=0;i<k;i++){
            pos[i]=s.nextInt();
        }
        for(int i=0;i<k;i++){
            temp[i]=s.nextInt();
            ans[pos[i]-1]=temp[i];
            min=Math.min(temp[i],min);
            if(map.containsKey(temp[i])){
                map.get(temp[i]).add(pos[i]-1);
                int a=count1.get(temp[i]);
                a++;
                count1.remove(temp[i]);
                count1.put(temp[i],a);
            }
            else{
                ArrayList<Integer> obj = new ArrayList<Integer>();
                obj.add(pos[i]-1);
                map.put(temp[i],obj);
                count1.put(temp[i],1);
            }
        }
     
        int num=min;
       
        while(true){
           
        
           if(!map.containsKey(num)){
               break;
           }
          ArrayList<Integer> obj2 = map.get(num);
          
          for(int i=0;i<obj2.size();i++){
              int index=obj2.get(i);
              if(ans[index]!=0){
                  if(ans[index]<num){
                     
                    if(index+1<n && (ans[index+1]>(num+1)|| ans[index+1]==0)){
                        ans[index+1]=num+1;
                       
                        if(map.containsKey(num+1)){
                            map.get(num+1).add(index+1);
                        }
                        else{
                            ArrayList<Integer> object = new  ArrayList<Integer>();
                            object.add(index+1);
                            map.put(num+1,object);
                        }
                    }

                    if(index-1>=0 && (ans[index-1]>(num+1)|| ans[index-1]==0)){
                        ans[index-1]=num+1;
                     
                        if(map.containsKey(num+1)){
                            map.get(num+1).add(index-1);
                        }
                        else{
                            ArrayList<Integer> object = new  ArrayList<Integer>();
                            object.add(index-1);
                            map.put(num+1,object);
                        }
                    }

                  }
                  else if(ans[index]==num){
                   
                      if(index+1<n && (ans[index+1]>(num+1)|| ans[index+1]==0)){
                        ans[index+1]=num+1;
                   
                        if(map.containsKey(num+1)){
                            map.get(num+1).add(index+1);
                        }
                        else{
                            ArrayList<Integer> object = new  ArrayList<Integer>();
                            object.add(index+1);
                            map.put(num+1,object);
                        }
                    }

                    if(index-1>=0 && (ans[index-1]>(num+1)|| ans[index-1]==0)){
                        ans[index-1]=num+1;
                    
                        if(map.containsKey(num+1)){
                            map.get(num+1).add(index-1);
                        }
                        else{
                            ArrayList<Integer> object = new  ArrayList<Integer>();
                            object.add(index-1);
                            map.put(num+1,object);
                        }
                    }
                  }

              }
            
            }
           num++;
        }
        for(int i=0;i<ans.length;i++){
            res.append(ans[i]+" ");
        }
        res.append("\n");
        p++;
    }
    System.out.println(res);


    }
    static class FastScanner {
		BufferedReader br;
		StringTokenizer st;
 
		public FastScanner(String s) {
			try {
				br = new BufferedReader(new FileReader(s));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 
		public FastScanner() {
			br = new BufferedReader(new InputStreamReader(System.in));
		}
 
		String nextToken() {
			while (st == null || !st.hasMoreElements()) {
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return st.nextToken();
		}
 
		int nextInt() {
			return Integer.parseInt(nextToken());
		}
 
		long nextLong() {
			return Long.parseLong(nextToken());
		}
 
		double nextDouble() {
			return Double.parseDouble(nextToken());
		}
	}




}