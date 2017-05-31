package name.sophy.dataSourceFeatureExtraction;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*
 * 2017.5.21
 * 统计150个数据源中，每个数据源中涉及多少type和property
 */
public class TypeAndPropertyCounting {
	private ArrayList<HashMap<String, Integer>> typeCountArray = new ArrayList<HashMap<String, Integer>>();
	private ArrayList<HashMap<String, Integer>> propertyCountArray = new ArrayList<HashMap<String, Integer>>();
	
	private ParameterizedSparqlString sparqlstr;
    private URL queryURL;			//进行sparql查询
	private URLConnection connAPI;
    private ResultSet rs;
    
    public void initial(){
    	for(int i = 0; i < 150; i++){
    		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
    		typeCountArray.add(tempMap);
    	}
    	for(int i = 0; i < 150; i++){
    		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
    		propertyCountArray.add(tempMap);
    	}
    }
    
	public void countingType(int dataSourceID) throws IOException{	//用virtuoso查询全部，然后写文件
		sparqlstr = new ParameterizedSparqlString("SELECT ?o,count(?o)as ?count WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o} group by(?o) order by desc(count(?o))");
		queryURL = new URL("http://localhost:8890/sparql?default-graph-uri=" + URLEncoder.encode("http://localhost:8890/data_source_" + String.valueOf(dataSourceID) , "UTF-8") 
          + "&query=" + URLEncoder.encode(sparqlstr.toString(), "UTF-8") + "&format=xml%2Fhtml&timeout=0&debug=on");
        System.out.println(queryURL);
    	
        connAPI = queryURL.openConnection();
        connAPI.setConnectTimeout(20000);
        connAPI.connect();
        
        rs = ResultSetFactory.fromXML(connAPI.getInputStream());
        while(rs.hasNext()) {
        	QuerySolution qs = rs.next();
        	String type = qs.get("o").toString();
        	int countOfType = Integer.parseInt(qs.get("count").toString().split("\\^")[0]);
        	System.out.println(type + ", " + countOfType);
        	typeCountArray.get(dataSourceID).put(type, countOfType);
        }
        
        int numberOfResult = 0;	//记录查询的结果数目
		for(String type:typeCountArray.get(dataSourceID).keySet())	//显示type的统计结果
        	numberOfResult += typeCountArray.get(dataSourceID).get(type);
        System.out.println("data-source-type-" + dataSourceID + " : " + numberOfResult);
	}
	
	public void countingProperty(int dataSourceID) throws IOException{
		sparqlstr = new ParameterizedSparqlString("SELECT ?p,count(?p)as ?count WHERE {?s ?p ?o} group by(?p) order by desc(count(?p))");
		queryURL = new URL("http://localhost:8890/sparql?default-graph-uri=" + URLEncoder.encode("http://localhost:8890/data_source_" + String.valueOf(dataSourceID) , "UTF-8") 
          + "&query=" + URLEncoder.encode(sparqlstr.toString(), "UTF-8") + "&format=xml%2Fhtml&timeout=0&debug=on");
        System.out.println(queryURL);
    	
        connAPI = queryURL.openConnection();
        connAPI.setConnectTimeout(20000);
        connAPI.connect();
        
        rs = ResultSetFactory.fromXML(connAPI.getInputStream());
        while(rs.hasNext()) {
        	QuerySolution qs = rs.next();
        	String property = qs.get("p").toString();
        	int countOfProperty = Integer.parseInt(qs.get("count").toString().split("\\^")[0]);
        	//System.out.println(property + ", " + countOfProperty);
        	propertyCountArray.get(dataSourceID).put(property, countOfProperty);
        }
		
        int numberOfResult = 0;	//记录查询的结果数目
		for(String property: propertyCountArray.get(dataSourceID).keySet())	//显示property统计结果
			numberOfResult += propertyCountArray.get(dataSourceID).get(property);
		System.out.println("data-source-property-" + dataSourceID + " : " + numberOfResult);
	}
	
	public void writeToTypeFile() throws IOException{
		File filename = new File("dataSource/dataSourceTypeCount.txt");
        filename.createNewFile();
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8")));
        out.write("dataSourceID index type count" + "\r\n");
        for(int i = 0; i < 150; i++){
        	List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(typeCountArray.get(i).entrySet());
    		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
    		    public int compare(Map.Entry<String,  Integer> o1, Map.Entry<String,  Integer> o2) {
    		    	return (o2.getValue() - o1.getValue()); 
    		    }
    		});
    		for(int j = 0; j < list.size(); j++){
    			out.write("data-source-" + i + " " + j + " "+ list.get(j).getKey() + " " + list.get(j).getValue()+ "\r\n");
    		}
        }
        out.flush();
        out.close();
	}
	
	public void writeToPropertyFile() throws IOException{
		File filename = new File("dataSource/dataSourcePropertyCount.txt");
        filename.createNewFile();
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8")));
        out.write("dataSourceID index property count" + "\r\n");
        for(int i = 0; i < 150; i++){
        	List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(propertyCountArray.get(i).entrySet());
    		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
    		    public int compare(Map.Entry<String,  Integer> o1, Map.Entry<String,  Integer> o2) {
    		    	return (o2.getValue() - o1.getValue()); 
    		    }
    		});
    		for(int j = 0; j < list.size(); j++){
    			out.write("data-source-" + i + " " + j + " "+ list.get(j).getKey() + " " + list.get(j).getValue()+ "\r\n");
    		}
        }
        out.flush();
        out.close();
        
        int totalTripleCount = 0;
        for(int i = 0; i < 150; i++)
        	for(String property:propertyCountArray.get(i).keySet())
        		totalTripleCount += propertyCountArray.get(i).get(property);
        System.out.println("Total property count is " + totalTripleCount);
	}
	
	public void getTopFiveTypeAndProperty(){
		for(int i = 0; i < 150; i++){
        	List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(typeCountArray.get(i).entrySet());
    		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
    		    public int compare(Map.Entry<String,  Integer> o1, Map.Entry<String,  Integer> o2) {
    		    	return (o2.getValue() - o1.getValue()); 
    		    }
    		});
    		int count = 0;
    		for(int j = 0; j < list.size(); j++){
    			String type = list.get(j).getKey();
    			if(type.contains("http://dbpedia.org/ontology/")){
    				System.out.print(type + "	");
    				count++;
    			}
    			if(count == 5)
    				break;
    		}
    		System.out.println();
        }
		System.out.println("************************************************************************************");
		for(int i = 0; i < 150; i++){
	    	List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(propertyCountArray.get(i).entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			    public int compare(Map.Entry<String,  Integer> o1, Map.Entry<String,  Integer> o2) {
			    	return (o2.getValue() - o1.getValue()); 
			    }
			});
			for(int j = 0; j < ((list.size() < 5)?list.size():5); j++)
				System.out.print(list.get(j).getKey() + "	");
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws IOException {
		TypeAndPropertyCounting typeAndPropertyCounting = new TypeAndPropertyCounting();
		typeAndPropertyCounting.initial();
		for(int i = 0; i < 150; i++){
			typeAndPropertyCounting.countingType(i);
			System.out.println("***********************************************************************");
			typeAndPropertyCounting.countingProperty(i);
			System.out.println("=======================================================================");
		}
		//typeAndPropertyCounting.writeToTypeFile();
		//typeAndPropertyCounting.writeToPropertyFile();
		//typeAndPropertyCounting.getTopFiveTypeAndProperty();
	}
}
