package name.sophy.queryLogProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;

/*
 * 此类用于解析log文件中的内容，选取那些select的请求，并进行sparql查询
 * 记录所有存在查询结果的log
 * 注意：考虑如果查询结果超过10000条，http请求的方法就行不通了，就要使用jdbc查询的方法了
 */
public class LogDecoder {
	private HashMap<Integer, String> fullIdUrlMap = new HashMap<Integer, String>();	//存储文件中所有的select语句对应的line和query(未解码)
	private int fullLine = 0;	//1025818;	
	
	private ArrayList<Integer> idArrayList = new ArrayList<Integer>();
	private HashMap<Integer, String> idUrlMap = new HashMap<Integer, String>();	//存储这个文件中有查询结果对应的ID(从1开始)和query(未解码)
	private HashMap<Integer, Integer> idResultMap = new HashMap<Integer, Integer>(); //ID对应的result条数
	
	private ArrayList<Integer> wrongIdArrayList = new ArrayList<Integer>();
	private HashMap<Integer, String> wrongIdUrlMap = new HashMap<Integer, String>();//存储这个文件中查询出错(可能有语法错误或者解析错误)对应的ID（从1开始）和query(未解码)
	
    private URL queryURL;			//进行sparql查询
	private URLConnection connAPI;
    private ResultSet rs;
	
	public HashMap<Integer, String> getIdUrlMap() {
		return idUrlMap;
	}

	public void setIdUrlMap(HashMap<Integer, String> idUrlMap) {
		this.idUrlMap = idUrlMap;
	}

	public HashMap<Integer, Integer> getIdResultMap() {
		return idResultMap;
	}

	public void setIdResultMap(HashMap<Integer, Integer> idResultMap) {
		this.idResultMap = idResultMap;
	}

	public ArrayList<Integer> getIdArrayList() {
		return idArrayList;
	}

	public void setIdArrayList(ArrayList<Integer> idArrayList) {
		this.idArrayList = idArrayList;
	}

	public ArrayList<Integer> getWrongIdArrayList() {
		return wrongIdArrayList;
	}

	public void setWrongIdArrayList(ArrayList<Integer> wrongIdArrayList) {
		this.wrongIdArrayList = wrongIdArrayList;
	}

	public HashMap<Integer, String> getWrongIdUrlMap() {
		return wrongIdUrlMap;
	}

	public void setWrongIdUrlMap(HashMap<Integer, String> wrongIdUrlMap) {
		this.wrongIdUrlMap = wrongIdUrlMap;
	}
	
	public static void main(String[] args) {
		LogDecoder logDecoder = new LogDecoder();
		logDecoder.readFile("usewod/access.log-20150927");  //读取文件，存储所有的query
		System.out.println("Finish reading file.");
		logDecoder.executeSparqlQuery();					//进行sparql查询，并存储相应结果
		System.out.println("Finish sparql query.");
		logDecoder.writeToFile("usewod/result-log-20150927.txt", logDecoder.getIdArrayList(), logDecoder.getIdUrlMap(), logDecoder.getIdResultMap());
		System.out.println("Finish result.txt.");
		logDecoder.writeToFile("usewod/wrong-query-log-20150927.txt", logDecoder.getWrongIdArrayList(), logDecoder.getWrongIdUrlMap());
		System.out.println("Finish wrong query.txt.");
	}
	
	public void readFile(String fileName){
		File file = new File(fileName);	//解析access.log-20150927文件
        BufferedReader reader = null;
        String tempLine = null;
    	try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			while ((tempLine = reader.readLine()) != null) {
				fullLine++;
				String tempStr = tempLine.split(" ")[7];
				if(tempStr.toLowerCase().contains("select")){
			    	int beginIndex = tempStr.indexOf("query=") + 6;
			    	if(tempStr.substring(beginIndex).indexOf("&") != -1){	//找到query=之后的&的index
			    		int endIndex = beginIndex + tempStr.substring(beginIndex).indexOf("&");
			    		tempStr = tempStr.substring(beginIndex, endIndex);	
			    	}
			    	else
			    		tempStr =tempStr.substring(beginIndex);	
			    	fullIdUrlMap.put(fullLine, tempStr);	//存储所有select语句	
			    }//if
			}//while
			reader.close();
			System.out.println("Finish " + fullLine + " lines, begin to write to files.");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void executeSparqlQuery(){
		for(int line = 1; line <= 500000; line++){
			if(fullIdUrlMap.get(line) != null){
				String undecoderedQueryStr = fullIdUrlMap.get(line);
				try {
					queryURL = new URL("http://localhost:8890/sparql?default-graph-uri=" + URLEncoder.encode("http://localhost:8890/dbpedia_data", "UTF-8") 
		              + "&query=" + URLEncoder.encode(URLDecoder.getURLDecoderString(undecoderedQueryStr), "UTF-8") + "&format=xml%2Fhtml&timeout=0&debug=on");
		            
		        	connAPI = queryURL.openConnection();
		            connAPI.setConnectTimeout(20000);
		            connAPI.connect();
					
		            rs = ResultSetFactory.fromXML(connAPI.getInputStream());
		            int numberOfResult = 0;
		            while(rs.hasNext()) {
		            	//QuerySolution qs = rs.next();
		            	rs.next(); 
		            	numberOfResult++;
		            }
		        	getIdUrlMap().put(line, undecoderedQueryStr);
		        	getIdResultMap().put(line, numberOfResult);
		        	if(numberOfResult > 0){
		        		getIdArrayList().add(line);
		        		getIdUrlMap().put(line, undecoderedQueryStr);
		        		getIdResultMap().put(line, numberOfResult);
		        	}
				
				} 
				catch (Exception e) {	//异常处理
					getWrongIdArrayList().add(line);
					getWrongIdUrlMap().put(line, undecoderedQueryStr);
				}
			}//if
			if(line % 1000 == 0)
				System.out.println(line);
		}//for
	}
	
	public void writeToFile(String fileName, ArrayList<Integer> idArray, HashMap<Integer, String> idQueryMap){
		try {
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); // 打开一个随机访问文件流，按读写方式
            long fileLength = randomFile.length(); // 文件长度，字节数
            randomFile.seek(fileLength);//将写文件指针移到文件尾。
            for(int i = 0; i < idArray.size(); i++){
            	int lineIndex = idArray.get(i);
            	randomFile.writeBytes(lineIndex + " " + idQueryMap.get(lineIndex) + "\r\n");
            }
            randomFile.close();
        } 
		catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void writeToFile(String fileName, ArrayList<Integer> idArray, HashMap<Integer, String> idQueryMap, HashMap<Integer, Integer> idResMap){
		try {
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); // 打开一个随机访问文件流，按读写方式
            long fileLength = randomFile.length(); // 文件长度，字节数
            randomFile.seek(fileLength);//将写文件指针移到文件尾。
            for(int i = 0; i < idArray.size(); i++){
            	int lineIndex = idArray.get(i);
            	randomFile.writeBytes(lineIndex + " " + idQueryMap.get(lineIndex) + " " + idResMap.get(lineIndex) + "\r\n");
            }
            randomFile.close();
        } 
		catch (IOException e) {
            e.printStackTrace();
        }
	} 
}
