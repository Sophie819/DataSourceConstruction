package name.sophy.querylog;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.query.Query;

public class test {
	public static void main(String[] args) throws Exception {
//        String sparqlSentence = "%0A++++++++++++++++PREFIX+rdfs%3A+%3Chttp%3A//www.w3.org/2000/01/rdf-schema%23%3E%0A++++++++++++++++SELECT+%3Fpredicate+%3Fobject%0A++++++++++++++++WHERE+%7B+%3Chttp%3A//dbpedia.org/resource/Ebonyi_State%3E+%3Fpredicate+%3Fobject+%7D%0A++++++++++++++++";
//        
//        Query query = QueryFactory.create(URLDecoder.getURLDecoderString(sparqlSentence), "UTF-8");
//        query.addGraphURI("http://localhost:8890/dbpedia_data");
//        QueryExecution queryExecution= QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);
//        
//        ResultSet rs = queryExecution.execSelect();
//
//        int numberOfResult = 0;
//        while(rs.hasNext()) {
//        	 QuerySolution qs = rs.next();
//        	 numberOfResult++;
//        	 System.out.println("	result " + numberOfResult + " : " + qs.toString() + "\n");
//        }
	
//			try {
//	            RandomAccessFile randomFile = new RandomAccessFile("src/hello.txt", "rw"); // 打开一个随机访问文件流，按读写方式
//	            long fileLength = randomFile.length(); // 文件长度，字节数
//	            randomFile.seek(fileLength);//将写文件指针移到文件尾。
//	            for(int i = 0; i < 10; i++)
//	            	randomFile.writeBytes(i + " " + (i+1) + "\r\n");
//	            randomFile.close();
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//		ArrayList<Integer> arr = new ArrayList<Integer>();
//		for(int i = 0 ;i < 10; i++)
//			arr.add(i);
//		for(int i = 0 ;i < arr.size(); i++)
//			System.out.print(arr.get(i) + " ");
//		for(int i = 0; i < arr.size(); i++){
//			System.out.println(arr.get(i));
//			if(arr.get(i) % 2 == 0){
//				System.out.println("delete " + arr.get(i));
//				arr.remove(i);
////				for(int j = 0 ;j < arr.size(); j++)
////					System.out.print(arr.get(j) + " ");
//			}
//		}
//		for(int i = 0 ;i < arr.size(); i++)
//			System.out.print(arr.get(i) + " ");
		
		File filename = new File("hello.txt");
        filename.createNewFile();
        FileWriter fw = new FileWriter(filename); 
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8")));
        for(int i = 0; i < 5; i++)
        	out.write(i + "\r\n");
        out.flush();
        out.close();
	}
}
