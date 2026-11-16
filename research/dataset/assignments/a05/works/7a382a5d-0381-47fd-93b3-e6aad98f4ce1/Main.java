import java.util.*;

public class contestC {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int t = scanner.nextInt();
        while (t-->0){
            int n = scanner.nextInt();
            ArrayList<String> list = new ArrayList<>();
            for(int i=0;i<n;++i){
                String s = scanner.next();
                list.add(s);
            }
            int ans = 0;
            for(char a='a';a<='e';++a ){
                ArrayList<Integer> num = new ArrayList<>();
                for(String s : list){
                    int cnt = 0;
                    for(int i=0;i<s.length();++i){
                        if(s.charAt(i)==a) cnt++;
                        else cnt--;
                    }
                    num.add(cnt);
                }
                Collections.sort(num,Collections.reverseOrder());
                int sum = 0;
                for(int i=0;i<num.size();++i){
                    sum+=num.get(i);
                    if(sum<=0){
                        ans = Math.max(ans,i);
                        break;
                    }
                }
                if(sum > 0) {
                    ans = n;
                    break;
                }
            }
            System.out.println(ans);
        }
    }
}
