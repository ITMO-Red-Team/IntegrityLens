import java.util.*;
public class Main{
    public static void main(String[] args){
        Scanner sc=new Scanner(System.in);
        int t=sc.nextInt();
        while(t--!=0){
            int n=sc.nextInt();
            int m=sc.nextInt();
            int x=sc.nextInt();
            int[] a=new int[n];
            int[] c=new int[n];
            Map<Integer,Stack<Integer>> mp=new HashMap<>();
            for(int i=0;i<n;i++){
                a[i]=sc.nextInt();
                //c[i]=a[i];
                if(mp.containsKey(a[i])){
                    Stack<Integer> l=mp.get(a[i]);
                    l.push(i);
                    mp.put(a[i],l);
                }
                else{
                    Stack<Integer> l=new Stack<>();
                    l.push(i);
                    mp.put(a[i],l);
                }
            }
            Arrays.sort(a);
            int[] b=new int[n];
           
            int cn=1;
            for(int i=0;i<n;i++){
                cn=cn%m;
                Stack<Integer> li=mp.get(a[i]);
                if(!li.isEmpty()){
                    int val=li.peek();
                    b[val]=cn+1;
                    c[i]+=a[i];
                    li.pop();
                    mp.put(a[i],li);
                }
                cn++;
            }
            int mv=c[0]-c[n-1];
            if(Math.abs(mv)>x) System.out.println("NO");
            else {
                System.out.println("YES");
                for(int i=0;i<n;i++){
                    System.out.print(b[i]+" ");
                }
                System.out.println();
            }
        }
    }
    
}