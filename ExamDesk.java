import java.util.*;

public class ExamDesk {
    // 1. Attendance: hashing, expected O(n) time.
    static String attendance(int[] rolls) {
        Set<Integer> seen = new HashSet<>();
        int repeats = 0; Integer first = null;
        for (int roll : rolls) if (!seen.add(roll)) {
            repeats++; if (first == null) first = roll;
        }
        return "repeats=" + repeats + ", distinct=" + seen.size() + ", first repeat=" + first;
    }
    // 2. Grace marks: unbounded coin change, O(target * units.length).
    static int grace(int[] units, int target) {
        if (target < 0) throw new IllegalArgumentException("Negative target");
        for (int u : units) if (u <= 0) throw new IllegalArgumentException("Units must be positive");
        int[] dp = new int[target + 1]; Arrays.fill(dp, Integer.MAX_VALUE); dp[0] = 0;
        for (int i = 1; i <= target; i++)
            for (int u : units) if (u <= i && dp[i-u] != Integer.MAX_VALUE)
                dp[i] = Math.min(dp[i], dp[i-u] + 1);
        return dp[target] == Integer.MAX_VALUE ? -1 : dp[target];
    }
    // 3. Prefix sums: O(n + q), inclusive zero-based ranges.
    static long[] ranges(int[] marks, int[][] queries) {
        long[] prefix = new long[marks.length + 1];
        for (int i = 0; i < marks.length; i++) prefix[i+1] = prefix[i] + marks[i];
        long[] result = new long[queries.length];
        for (int i = 0; i < queries.length; i++) {
            int l = queries[i][0], r = queries[i][1];
            if (l < 0 || r < l || r >= marks.length) throw new IllegalArgumentException("Invalid range");
            result[i] = prefix[r+1] - prefix[l];
        }
        return result;
    }
    // 4. Longest common subsequence: O(n*m) time and space.
    static int copying(String a, String b) {
        int[][] dp = new int[a.length()+1][b.length()+1];
        for (int i=1; i<=a.length(); i++) for (int j=1; j<=b.length(); j++)
            dp[i][j] = a.charAt(i-1)==b.charAt(j-1) ? 1+dp[i-1][j-1] : Math.max(dp[i-1][j],dp[i][j-1]);
        return dp[a.length()][b.length()];
    }
    // 5. Binary search on answer: O(n log(S+1)).
    static long evaluator(int[] bundles, int k) {
        if (k <= 0) throw new IllegalArgumentException("Need positive evaluator count");
        long low=0, high=0;
        for (int x:bundles) { if(x<0) throw new IllegalArgumentException("Negative bundle"); low=Math.max(low,x); high+=x; }
        while(low<high) {
            long mid=low+(high-low)/2;
            int used=1; long load=0;
            for(int x:bundles) { if(load+x>mid){used++;load=0;} load+=x; }
            if(used<=k) high=mid; else low=mid+1;
        }
        return low;
    }
    // 6. Sorting by total descending, then maths descending.
    static class Student {
        String name; int total, maths;
        Student(String n,int t,int m){name=n;total=t;maths=m;}
    }
    static String[] rank(Student[] students) {
        Student[] copy=students.clone();
        Arrays.sort(copy, Comparator.comparingInt((Student s)->s.total).reversed()
            .thenComparing(Comparator.comparingInt((Student s)->s.maths).reversed()));
        String[] names=new String[copy.length];
        for(int i=0;i<copy.length;i++) names[i]=copy[i].name;
        return names;
    }
    static void check(Object expected,Object actual){
        if(!Objects.equals(expected,actual)) throw new AssertionError("Expected "+expected+" but got "+actual);
    }
    static Student[] sampleStudents(){return new Student[]{new Student("Asha",410,88),new Student("Bala",425,90),new Student("Chitra",410,95),new Student("Dev",398,99)};}
    static void tests(){
        check("repeats=2, distinct=4, first repeat=101",attendance(new int[]{101,102,103,101,104,102}));
        check("repeats=0, distinct=0, first repeat=null",attendance(new int[]{}));
        check(4,grace(new int[]{1,2,5},13)); check(-1,grace(new int[]{2},3)); check(0,grace(new int[]{2},0));
        check("[221, 226]",Arrays.toString(ranges(new int[]{72,85,64,91,58,77},new int[][]{{0,2},{3,5}})));
        check("[4294967294]",Arrays.toString(ranges(new int[]{Integer.MAX_VALUE,Integer.MAX_VALUE},new int[][]{{0,1}})));
        check(6,copying("abcbdacb","abdcabcb")); check(0,copying("","abc"));
        check(127L,evaluator(new int[]{42,38,51,29,47,33,60,25},3)); check(0L,evaluator(new int[]{},3)); check(7L,evaluator(new int[]{7},1));
        check("[Bala, Chitra, Asha, Dev]",Arrays.toString(rank(sampleStudents())));
        check("[]",Arrays.toString(rank(new Student[]{})));
        System.out.println("All automated tests passed.");
    }
    // Count comparable primitive operations on deterministic synthetic data.
    static void measure(){
        System.out.println("n | attendance brute/hash | ranges brute/prefix | rank selection/merge comparisons");
        for(int n:new int[]{128,256,512,1024}){
            int[] a=new int[n]; long seed=42;
            for(int i=0;i<n;i++){seed=(1664525L*seed+1013904223L)&0xffffffffL;a[i]=(int)(seed%(n/2));}
            long brute=0; for(int i=0;i<n;i++)for(int j=0;j<i;j++){brute++;if(a[i]==a[j])break;}
            Set<Integer> set=new HashSet<>();long hash=0;for(int x:a){set.add(x);hash++;}
            long rb=0,rp=n; long[] p=new long[n+1]; for(int i=0;i<n;i++)p[i+1]=p[i]+a[i];
            for(int q=0;q<n;q++){int l=q/2,r=n-1;long sum=0;for(int j=l;j<=r;j++){sum+=a[j];rb++;}check(sum,p[r+1]-p[l]);rp++;}
            int[] b=a.clone();long sc=0;for(int i=0;i<n;i++){int min=i;for(int j=i+1;j<n;j++){sc++;if(b[j]<b[min])min=j;}int t=b[i];b[i]=b[min];b[min]=t;}
            int[] c=a.clone();long mc=merge(c,new int[n],0,n);check(Arrays.toString(b),Arrays.toString(c));
            System.out.printf("%d | %d/%d | %d/%d | %d/%d%n",n,brute,hash,rb,rp,sc,mc);
        }
    }
    static long merge(int[] a,int[] tmp,int l,int r){if(r-l<2)return 0;int m=(l+r)/2;long c=merge(a,tmp,l,m)+merge(a,tmp,m,r);int i=l,j=m,k=l;while(i<m&&j<r){c++;tmp[k++]=a[i]<=a[j]?a[i++]:a[j++];}while(i<m)tmp[k++]=a[i++];while(j<r)tmp[k++]=a[j++];System.arraycopy(tmp,l,a,l,r-l);return c;}
    static int[] array(Scanner s){int n=s.nextInt();if(n<0)throw new IllegalArgumentException("Negative length");int[] a=new int[n];for(int i=0;i<n;i++)a[i]=s.nextInt();return a;}
    public static void main(String[] args){
        if(args.length>0){tests();measure();return;}
        Scanner s=new Scanner(System.in);
        while(true){System.out.println("\nEXAMDESK\n1 Attendance\n2 Grace marks\n3 Range totals\n4 Copying check\n5 Evaluator load\n6 Rank list\n7 Tests\n8 Measurements\n0 Exit");
            if(!s.hasNextInt())return;int choice=s.nextInt();if(choice==0)return;
            try{switch(choice){
                case 1: System.out.println("Enter count followed by roll numbers:");System.out.println(attendance(array(s)));break;
                case 2: System.out.println("Enter unit count, units, then target:");System.out.println(grace(array(s),s.nextInt()));break;
                case 3: System.out.println("Enter marks count, marks, query count, then each start/end index:");int[] a=array(s);int q=s.nextInt();int[][] qr=new int[q][2];for(int i=0;i<q;i++){qr[i][0]=s.nextInt();qr[i][1]=s.nextInt();}System.out.println(Arrays.toString(ranges(a,qr)));break;
                case 4: System.out.println("Enter two answer strings on separate lines:");s.nextLine();System.out.println(copying(s.nextLine(),s.nextLine()));break;
                case 5: System.out.println("Enter bundle count, bundle sizes, then evaluator count:");System.out.println(evaluator(array(s),s.nextInt()));break;
                case 6: System.out.println("Enter student count, then name (one word), total, maths for each:");int n=s.nextInt();Student[] st=new Student[n];for(int i=0;i<n;i++)st[i]=new Student(s.next(),s.nextInt(),s.nextInt());System.out.println(Arrays.toString(rank(st)));break;
                case 7:tests();break;case 8:measure();break;default:System.out.println("Invalid choice");
            }}catch(RuntimeException e){System.out.println("Invalid input: "+e.getMessage());if(s.hasNextLine())s.nextLine();else return;}
        }
    }
}
