import java.util.*;

public class Main{
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        int t=Integer.valueOf(sc.nextLine());
        while (t-->0){
            int n=Integer.valueOf(sc.nextLine());
            int ans=100001;
            String a=sc.nextLine();
            String b=sc.nextLine();
            HashSet<Integer> listb=new HashSet<>();
            ArrayList<Integer> lista=new ArrayList<>();
            for (int i=0;i<n;i++){
                if(a.charAt(i)=='1') lista.add(i);
                if(b.charAt(i)=='1') listb.add(i);
            }
            int num=0;
            for (int i=0;i<lista.size();i++){
                if(listb.contains(lista.get(i))) num++;
            }
            //第一种情况
            if(lista.size()==listb.size()){
                ans=Math.min(ans,(listb.size()-num)*2);
            }
            //第二种情况 n-lista.size() listb.size()-num
            if(listb.size()-(n-lista.size())==1){
                ans=Math.min(ans,(num-1)*2+1);
            }
            System.out.println((ans==100001)?-1:ans);
        }
    }
}

