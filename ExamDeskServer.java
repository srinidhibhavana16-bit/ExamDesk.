import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.io.*;
public class ExamDeskServer {
 static int[] nums(String s){return s.trim().isEmpty()?new int[0]:Arrays.stream(s.trim().split("[,\\s]+")).mapToInt(Integer::parseInt).toArray();}
 static String run(String id,String a,String b){switch(id){
 case "attendance":return ExamDesk.attendance(nums(a));
 case "grace":int target=Integer.parseInt(b.trim());if(target>1000000)throw new IllegalArgumentException("Target too large");return "Minimum grace units: "+ExamDesk.grace(nums(a),target);
 case "range":String[] rows=b.split(";");int[][] q=new int[rows.length][];for(int i=0;i<q.length;i++){q[i]=nums(rows[i]);if(q[i].length!=2)throw new IllegalArgumentException("Each range needs two indices");}return "Range totals: "+Arrays.toString(ExamDesk.ranges(nums(a),q));
 case "copy":if((long)a.length()*b.length()>4000000)throw new IllegalArgumentException("Answers too long");return "LCS score: "+ExamDesk.copying(a,b)+"\nSimilarity is not proof of copying.";
 case "load":return "Minimum maximum load: "+ExamDesk.evaluator(nums(a),Integer.parseInt(b.trim()));
 case "rank":List<ExamDesk.Student> st=new ArrayList<>();for(String line:a.split("\\R")){if(line.trim().isEmpty())continue;String[] p=line.split(",");if(p.length!=3)throw new IllegalArgumentException("Use name,total,maths on each line");st.add(new ExamDesk.Student(p[0].trim(),Integer.parseInt(p[1].trim()),Integer.parseInt(p[2].trim())));}String[] names=ExamDesk.rank(st.toArray(new ExamDesk.Student[0]));StringBuilder out=new StringBuilder();for(int i=0;i<names.length;i++)out.append(i+1).append(". ").append(names[i]).append('\n');return out.toString();
 default:throw new IllegalArgumentException("Unknown module");}}
 static void send(HttpExchange e,int status,String type,byte[] body)throws IOException{e.getResponseHeaders().set("Content-Type",type);e.sendResponseHeaders(status,body.length);try(OutputStream o=e.getResponseBody()){o.write(body);}}
 public static void main(String[] args)throws IOException{HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",8765),0);server.createContext("/api/run",e->{try{if(!e.getRequestMethod().equals("POST"))throw new IllegalArgumentException("POST required");Map<String,String> p=new HashMap<>();for(String item:new String(e.getRequestBody().readNBytes(200000),StandardCharsets.UTF_8).split("&")){String[] kv=item.split("=",2);if(kv.length==2)p.put(URLDecoder.decode(kv[0],"UTF-8"),URLDecoder.decode(kv[1],"UTF-8"));}send(e,200,"text/plain; charset=utf-8",run(p.getOrDefault("id",""),p.getOrDefault("a",""),p.getOrDefault("b","")).getBytes(StandardCharsets.UTF_8));}catch(Exception ex){send(e,400,"text/plain; charset=utf-8",("Invalid input: "+ex.getMessage()).getBytes(StandardCharsets.UTF_8));}});server.createContext("/",e->{String path=e.getRequestURI().getPath();String file=path.equals("/")?"index.html":path.substring(1);if(!Arrays.asList("index.html","style.css","app.js").contains(file)){send(e,404,"text/plain",new byte[0]);return;}send(e,200,file.endsWith("css")?"text/css":file.endsWith("js")?"application/javascript":"text/html; charset=utf-8",Files.readAllBytes(Paths.get("web",file)));});server.start();System.out.println("ExamDesk running at http://localhost:8765");}
}
