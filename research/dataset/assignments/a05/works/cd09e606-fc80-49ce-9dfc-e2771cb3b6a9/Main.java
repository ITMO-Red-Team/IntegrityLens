import java.util.*;
public class shivam{
    public static int diff(String str, char ch){
        int cnt=0;
        for(int i=0;i<str.length();i++){
            if(ch==str.charAt(i)){
                cnt++;
            }
        }
        return cnt-(str.length()-cnt);
    }
    public static int process(char ch,int n,String  []arr){
        int[]a=new int[n];
            for(int i=0;i<n;i++){
                a[i]=diff(arr[i],ch);
            }
            Arrays.sort(a);
            int max=0;
            int sum=0;
            for(int i=n-1;i>=0;i--){
                sum+=a[i];
                if(sum>0){
                    max++;
                }
                else{
                    break;
                }
            }





        return max;
    }


    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        int k=sc.nextInt();
        while(k-->0){
            int n=sc.nextInt();
            String []arr=new String[n];
            for(int i=0;i<n;i++){
                arr[i]=sc.next();
            }
            int a=process('a',n,arr);
            int b=process('b',n,arr);
            int c=process('c',n,arr);
            int d=process('d',n,arr);
            int e=process('e',n,arr);
            System.out.println(Math.max(a,Math.max(b,Math.max(c,Math.max(d,e)))));
           
        }

    }
}