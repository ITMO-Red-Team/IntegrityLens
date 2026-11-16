import java.util.*;
import java.lang.*;
import java.io.*;



public class ecf106 {

//HashMap<Integer,Integer> hm=new HashMap<>();
//ArrayList<Integer> list=new ArrayList<>();
//HashSet<Integer> hs=new HashSet<>();
//Stack<Integer> st=new Stack<>();
//Queue<Integer> q=new LinkedList<>();
//PriorityQueue<Integer> pq=new PriorityQueue<>();
// static ArrayList<ArrayList<Integer>> gp=new ArrayList<>();
//int[] arr=new int[n];
//System.out.println("YES");
//1 hr- atleast 2 qs d-2
//Mt_do 3 qs d-2  
//Mt_do 4 qs d-3
//StringBuilder sb=new StringBuilder();

public static void main(String[] args) throws Exception{
    InputStreamReader ip=new InputStreamReader(System.in);
    BufferedReader br = new BufferedReader(ip);
    int t= Integer.parseInt(br.readLine());
    
    while(t-->0){
       int n=Integer.parseInt(br.readLine());//Integer.parseInt(strs[0]);
        String[] strs=(br.readLine()).trim().split(" ");
        // int n=Integer.parseInt(strs[0]),k=Integer.parseInt(strs[1]);//,k=Integer.parseInt(strs[2]);

        // strs=(br.readLine()).trim().split(" ");
         long[] arr=new long [n];

        for(int i=0;i<n;i++){
            arr[i]=Integer.parseInt(strs[i]);
        }

        long min1=0,min2=0;
        long x=0,y=0;
        long hor=arr[0],vert=arr[1];
        long ans=(n*hor)+(n*vert);
        
        for(int i=2;i<n;i++){
            if(i%2==0){
                x++;
                if(arr[i]>=hor){   //v.v.imp  donot update horizontal
                    min1+=arr[i];  
                }else{
                    min1+=hor;  hor=arr[i];
                }
            }else{
                y++;
                if(arr[i]>=vert){   //v.v.imp  donot update vertical
                    min2+=arr[i];    
                }else{
                    min2+=vert;  vert=arr[i];
                }
            }

            long rem=(n-x)*hor+(n-y)*vert;

            ans=Math.min(ans, min1+min2+rem);
        }

        System.out.println(ans);
    }

    }
}










// public static void main(String[] args) throws Exception{
//     InputStreamReader ip=new InputStreamReader(System.in);
//     BufferedReader br = new BufferedReader(ip);
//     int t= Integer.parseInt(br.readLine());
    
//     while(t-->0){
//     //    int n=Integer.parseInt(br.readLine());//Integer.parseInt(strs[0]);
//         // String[] strs=(br.readLine()).trim().split(" ");
//         // int n=Integer.parseInt(strs[0]),k=Integer.parseInt(strs[1]);//,k=Integer.parseInt(strs[2]);

//         // strs=(br.readLine()).trim().split(" ");
//         String str=(br.readLine()).trim();

//         char[] arr=str.toCharArray();
//             int  n=arr.length;
//             int i=0, l=0, r=n;
//             for (i=1; i<n; i++)
//                 if (arr[i-1]==arr[i] && arr[i]=='0')
//                     l=i;


//             for (i=n-2; i>=0; i--)
//                 if (arr[i]==arr[i+1] && arr[i]=='1')
//                     r=i;
//             if (l<=r)
//                System.out.println("YES");
//             else
//             System.out.println("NO");

//         // int cz=0,co=0;
//         // int lz=0;
//         // int n=str.length();

//         // boolean fg1=true;
//         // for(int i=0;i<n;i++){
//         //     if(arr[i]=='1'){co++;}else{cz++;  lz=i;}
//         //     if(i>0 && arr[i-1]>arr[i]){fg1=false; }
//         // }

//         // if(co==n || cz==n || fg1 || co==1 ||cz==1){
//         //     System.out.println("YES");  continue;
//         // }
       
//         // // String s1="";
//         // // String s2="";
//         // StringBuilder sb1=new StringBuilder();
//         // StringBuilder sb2=new StringBuilder();
//         // for(int i=0;i<n;i+=2){
//         //     // s1+=arr[i];
//         //     sb1.append(arr[i]);
//         // }

//         // for(int i=1;i<n;i+=2){
//         //     // s2+=arr[i];
//         //     sb2.append(arr[i]);
//         // }

//         // char[] arr1=(sb1.toString()).toCharArray();
//         // char[] arr2=(sb2.toString()).toCharArray();
//         // boolean fg2=true;
//         // boolean fg3=true;
//         // for(int i=0;i<arr1.length;i++){
//         //     if(i>0 && arr1[i-1]>arr1[i]){fg2=false;  break;}
//         // }

        

//         // for(int i=0;i<arr2.length;i++){
//         //     if(i>0 && arr2[i-1]>arr2[i]){fg3=false;}
//         // }

//         // if(fg2 ||fg3){
//         //     System.out.println("YES");
//         // }else{
//         //     System.out.println("NO");
//         // }
//     }

// }
// }







        // int az=0,ao=0;
        // ArrayList<Integer> list=new ArrayList<>();
        // for(int i=0;i<n;i++){
        //     if(arr[i]=='0'){az++; if(ao!=0){list.add(ao);}    ao=0;  }
        //     else{ao++;   if(az!=0){list.add(az); }   az=0;  }
        // }
        // if(az!=0){list.add(az); }
        // if(ao!=0){list.add(ao);}

        // boolean fl=true;
        // for(int i=1;i<list.size();i++){
        //     int v1=list.get(i-1),v2=list.get(i);

        //     if(v1>1 && v2>1){fl=false;  break;}
        // }

        // if(fl){System.out.println("YES");}
        // else{System.out.println("NO");}
//         }

//     }
// }
        //c1    000001111


        // boolean fg=true;
        // int last=-2;
        
        // for(int i=0;i<=lz;i++){
        //     if(arr[i]=='0'){ if((last+1)==i){fg=false;  break;}  last=i; }
        // }



        // if(fg){System.out.println("YES");  continue;}
        // //c2 11111
        // fg=true;
        //  last=-2;
        // for(int i=0;i<n;i++){
        //     if(arr[i]=='0'){ if((last+1)==i){fg=false;  break;}  last=i; }
        // }

        // if(fg){System.out.println("YES");  continue;}
        // //c3  0000
        // fg=true;
        //  last=-2;
        // for(int i=0;i<n;i++){
        //     if(arr[i]=='1'){ if((last+1)==i){fg=false;  break;}  last=i; }
        // }

        // if(fg){System.out.println("YES");}
        // else{System.out.println("NO");}
    // }

    // }
    // }



// public static void main(String[] args) throws Exception{
//     InputStreamReader ip=new InputStreamReader(System.in);
//     BufferedReader br = new BufferedReader(ip);
//     int t= Integer.parseInt(br.readLine());
    
//     while(t-->0){
//     //    int n=Integer.parseInt(br.readLine());//Integer.parseInt(strs[0]);
//         String[] strs=(br.readLine()).trim().split(" ");
//         int n=Integer.parseInt(strs[0]),k1=Integer.parseInt(strs[1]),k2=Integer.parseInt(strs[2]);//,k=Integer.parseInt(strs[2]);

//         strs=(br.readLine()).trim().split(" ");
        
//         int w=Integer.parseInt(strs[0]),b=Integer.parseInt(strs[1]);

//         int tw=k1+k2;
//         int tb=(2*n)-tw;

//         if((tw/2)>=w && (tb/2)>=b){
//             System.out.println("YES");
//         }else{
//             System.out.println("NO");
//         }
//     }

//     }
// }
