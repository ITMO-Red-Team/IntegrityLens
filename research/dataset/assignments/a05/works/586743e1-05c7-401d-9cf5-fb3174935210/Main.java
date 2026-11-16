import java.util.*;
public class B {
    //static int num = (int)Math.pow(10,6);
	static Scanner sc = null;
	public static void main(String[] args) {
		//System.out.println("Enter :");
		sc = new Scanner(System.in);
		int t = sc.nextInt();
		while(t-- >0){
		    int n = sc.nextInt();
		    sc.nextLine();
		    String a[] = new String[n];
		    for(int i=0;i<n;i++){
		        a[i] = sc.nextLine();
		    }
		    ArrayList<Character> a1 = new ArrayList<>();
		    a1.add('a');
		    a1.add('b');
		    a1.add('c');
		    a1.add('d');
		    a1.add('e');
		    int res = 0;
		    for(Character ch : a1){
		        ArrayList<Integer> a2 = new ArrayList<>();
		        for(int i=0;i<n;i++){
		             int f1 = 0;
		             int f2 = 0;
		             String s = a[i];
		             for(int j=0;j<s.length();j++){
		                 char c2 = s.charAt(j);
		                 if(c2==ch)
		                 f1++;
		                 else
		                 f2++;
		             }
		             a2.add(f1-f2);
		        }
		        Collections.sort(a2);
		        int sum = 0;
		        int count = 0;
		        for(int j = n-1;j>=0;j--){
		            int num = a2.get(j);
		            sum+=num;
		            if(sum>0){
		                count++;
		            }
		            else
		            break;
		        }
		        res = Math.max(res,count);
		    }
		    System.out.println(res);
		}
	}
}

