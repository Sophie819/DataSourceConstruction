package name.sophy.queryLogProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.print.attribute.standard.Finishings;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

/*
 * 在得到的有查询结果的query中，删除那些select count 结果是0的情况
 */
public class QueryProcess {
	private ArrayList<String> queryEntryArray = new  ArrayList<String>();	//待处理的query
	private ArrayList<String> finalQueryEntryArray = new  ArrayList<String>();		//进行判断之后最终的query	
	private ArrayList<String> finalDecoderedQueryEntryArray = new ArrayList<String>();
	private URL queryURL;			//进行sparql查询
	private URLConnection connAPI;
    private ResultSet rs;
    private int deleteCount = 0;
	
	public ArrayList<String> getQueryEntryArray() {
		return queryEntryArray;
	}

	public void setQueryEntryArray(ArrayList<String> queryEntryArray) {
		this.queryEntryArray = queryEntryArray;
	}
    
    public ArrayList<String> getFinalQueryEntryArray() {
		return finalQueryEntryArray;
	}

	public void setFinalQueryEntryArray(ArrayList<String> finalQueryEntryArray) {
		this.finalQueryEntryArray = finalQueryEntryArray;
	}

	public ArrayList<String> getFinalDecoderedQueryEntryArray() {
		return finalDecoderedQueryEntryArray;
	}

	public void setFinalDecoderedQueryEntryArray(ArrayList<String> finalDecoderedQueryEntryArray) {
		this.finalDecoderedQueryEntryArray = finalDecoderedQueryEntryArray;
	}
	
	public int getDeleteCount() {
		return deleteCount;
	}

	public void setDeleteCount(int deleteCount) {
		this.deleteCount = deleteCount;
	}

	public static void main(String[] args) throws IOException {
		QueryProcess queryProcess = new QueryProcess();
		queryProcess.readFile("usewod/result/result-log-20150927.txt");
		queryProcess.readFile("usewod/result/result-log-20150927-1.txt");
		System.out.println("Finish loading " + queryProcess.getQueryEntryArray().size() + " query entries.");
		int i;
		for(i = 0; i < queryProcess.getQueryEntryArray().size(); i++){
//			String []tempArray = queryProcess.getQueryEntryArray().get(i).split(" ");
//			if(tempArray.length != 3)
//				System.err.println("Split error!");
//			String line = tempArray[0];
//			String undecoderedQueryStr = tempArray[1];
//			String resultCount = tempArray[2];
//			if(Integer.parseInt(resultCount) > 1){	//忽略结果数目大于1的
//				continue;
//			}
			queryProcess.deleteQueryOfOneResult(queryProcess.getQueryEntryArray().get(i));
			if(i % 100 == 0)
				System.out.println("i : " + i);
		}
		System.out.println("i : " + i);
		System.out.println("Finish deleting query (count result = 0).");
		queryProcess.writeToFile("usewod/full-result-log-20150927.txt", queryProcess.getFinalQueryEntryArray());
		queryProcess.writeToFile("usewod/full-query-log-20150927.txt", queryProcess.getFinalDecoderedQueryEntryArray());
		System.out.println("Finish writing to files, delete " + queryProcess.getDeleteCount() + " entries, " + queryProcess.getFinalQueryEntryArray().size()
				 + " " + queryProcess.getFinalDecoderedQueryEntryArray().size() + " remain.");
	}
    
	public void readFile(String fileName){
		File file = new File(fileName);	
        BufferedReader reader = null;
        String tempLine = null;
    	try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			while ((tempLine = reader.readLine()) != null)
				queryEntryArray.add(tempLine);
			reader.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void deleteQueryOfOneResult(String lineStr) throws IOException{
		String []tempArray = lineStr.split(" ");
		if(tempArray.length != 3)
			System.err.println("Split error!");
		String line = tempArray[0];
		String undecoderedQueryStr = tempArray[1];
		String resultCount = tempArray[2];
		if(Integer.parseInt(resultCount) > 1){	//忽略结果数目大于1的
			finalQueryEntryArray.add(line + " " + undecoderedQueryStr + " " + resultCount);
    		finalDecoderedQueryEntryArray.add(line + " " + URLDecoder.getURLDecoderString(undecoderedQueryStr));
    		return;
		}
		
		queryURL = new URL("http://localhost:8890/sparql?default-graph-uri=" + URLEncoder.encode("http://localhost:8890/dbpedia_data", "UTF-8") 
          + "&query=" + URLEncoder.encode(URLDecoder.getURLDecoderString(undecoderedQueryStr), "UTF-8") + "&format=xml%2Fhtml&timeout=0&debug=on");
        //System.out.println(queryURL);
    	connAPI = queryURL.openConnection();
        connAPI.setConnectTimeout(20000);
        connAPI.connect();
        rs = ResultSetFactory.fromXML(connAPI.getInputStream());
        if(rs.hasNext()) {
        	QuerySolution qs = rs.next();
        	//System.out.println(qs.toString());
        	if(qs.toString() == null || qs.toString().length() <= 0){
        		deleteCount++;
        		//System.out.println("***delete***");
        	}
        	else if(qs.toString().equals("( ?callret-0 = 0 )")){	//删去这个count & =0 的query
        		deleteCount++;
        		//System.out.println("***delete***");
        	}
        	else{
        		finalQueryEntryArray.add(line + " " + undecoderedQueryStr + " " + resultCount);
        		finalDecoderedQueryEntryArray.add(line + " " + URLDecoder.getURLDecoderString(undecoderedQueryStr));
        	}
        }		
	}
	
	public void writeToFile(String fileName, ArrayList<String> entryArray){
		try {
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); // 打开一个随机访问文件流，按读写方式
            long fileLength = randomFile.length(); // 文件长度，字节数
            randomFile.seek(fileLength);//将写文件指针移到文件尾。
            for(int i = 0; i < entryArray.size(); i++){
            	randomFile.writeBytes(entryArray.get(i) + "\r\n");
            }
            randomFile.close();
        } 
		catch (IOException e) {
            e.printStackTrace();
        }
	}
}
