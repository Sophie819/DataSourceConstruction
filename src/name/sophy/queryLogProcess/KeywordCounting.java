package name.sophy.queryLogProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

/*
 * 2017.5.23
 * 统计每条query中的sparql关键词
 * 统计范围是之前获得的所有有查询结果的query
 */
public class KeywordCounting {
	private HashMap<String, Integer> keywordSet = new HashMap<String, Integer>();	//存储所有单个关键词
	private HashMap<String, Integer> twoKeywordSet = new HashMap<String, Integer>();//存储双词的关键词（4个）
	private ArrayList<String> undecodeQueryArray = new ArrayList<String>();			//所有待解码的query
	private ArrayList<String> indexArray = new ArrayList<String>();					//存储所有待解码的query的index
	private int queryCount = 0;		//query总数
	
	private ArrayList<HashMap<String, Integer>> queryKeywordsArray = new ArrayList<HashMap<String, Integer>>();//存储每个query的关键词及其次数
	
	public void initialKeywordSet() throws IOException{
		File file = new File("usewod/keywords.txt");	
        BufferedReader reader = null;
        String tempLine = null;
		reader = new BufferedReader(new FileReader(file));
		while ((tempLine = reader.readLine()) != null)
			keywordSet.put(tempLine.toLowerCase(), 0);
		reader.close();
		
		twoKeywordSet.put("NOT EXISTS".toLowerCase(), 0);	//初始化twoKeywordSet
		twoKeywordSet.put("GROUP BY".toLowerCase(), 0);
		twoKeywordSet.put("ORDER BY".toLowerCase(), 0);
		twoKeywordSet.put("NOT IN".toLowerCase(), 0);
		
		System.out.println("1.load " + keywordSet.size() + " keywords.");
	}
	
	public void initialQueryArray() throws IOException{
		File file = new File("usewod/full-result-log-20150927.txt");	
        BufferedReader reader = null;
        String tempLine = null;
		reader = new BufferedReader(new FileReader(file));
		while ((tempLine = reader.readLine()) != null){
			String [] tempArray = tempLine.split(" ");
			indexArray.add(tempArray[0]);
			undecodeQueryArray.add(tempArray[1]);
			
			HashMap<String, Integer> tempMap = new HashMap<String, Integer>();	//初始化queryKeywordsArray
			queryKeywordsArray.add(tempMap);
			
			queryCount++;
		}
		reader.close();
		System.out.println("2.load " + undecodeQueryArray.size() + " queries.");
	}
	
	/*
	 * 需要做以下预处理：
	 * 1.删除所有<>中的内容
	 * 2.判断group by、order by、not exists、not in等关键词是否存在
	 * 3.去掉所有的空格、换行和标点（除了-和_），然后依次判断关键词存在与否
	 */
	public void countKeywordsOfQuery(){	
		for(int i = 0; i < queryCount; i++){	//修改
			//System.out.println(i + " " + URLDecoder.getURLDecoderString(undecodeQueryArray.get(i)));
			String tempDecodeQuery = deleteUri(URLDecoder.getURLDecoderString(undecodeQueryArray.get(i)).toLowerCase());	//删除<>
			for(String tempKeyword: twoKeywordSet.keySet()){															//判断4个两位关键词是否存在，及其存在次数
				int tempCount = getStrCount(tempDecodeQuery, tempKeyword);
				if(tempCount > 0)
					queryKeywordsArray.get(i).put(tempKeyword, tempCount);
			}
			
			StringTokenizer st = new StringTokenizer(tempDecodeQuery.replace("(", " ").replace(")", " ")
																	.replace("{", " ").replace("}", " ")
																	.replace("<", " ").replace(">", " ")
																	.replace("!", " "));//去掉部分符号，然后判断
	        while (st.hasMoreTokens()) {			
	        	String tempKeyword = st.nextToken();
	        	if(keywordSet.containsKey(tempKeyword)){	//遍历，判断是否是关键词
	        		if(queryKeywordsArray.get(i).containsKey(tempKeyword))
	        			queryKeywordsArray.get(i).put(tempKeyword, queryKeywordsArray.get(i).get(tempKeyword) + 1);
	        		else
	        			queryKeywordsArray.get(i).put(tempKeyword, 1);
	        	}
	        }
	        //System.out.println("***************************************************************************");
		}
	}
	
	public int getStrCount(String str, String subStr){	//获取字符串出现次数
		int index = str.indexOf(subStr);
		int count = 0;
		while(index != -1){
			count++;
			str = str.substring(index + subStr.length());
			index = str.indexOf(subStr);
		}
		return count;
	}
	
	public String deleteUri(String query){	//删除<>中的内容（包括尖括号）
		String tempQuery = query;
		int firstLeftIndex = query.indexOf("<");
		int firstRightIndex = query.indexOf(">");
		while(firstLeftIndex != -1 && firstRightIndex != -1){
			tempQuery = tempQuery.substring(0, firstLeftIndex) + 
					tempQuery.substring(firstRightIndex + 1);
			firstLeftIndex = tempQuery.indexOf("<");
			firstRightIndex = tempQuery.indexOf(">");
			if(firstLeftIndex > firstRightIndex){
				System.err.println("< 出现在 > 的后面");
				//System.out.println(query);
				return tempQuery;
			}
			if(firstLeftIndex == -1 || firstRightIndex == -1)
				return tempQuery;
		}
		return tempQuery;
	}
	
	public void writeToFile() throws IOException{
		File filename = new File("usewod/keywordCounting.txt");
		filename.createNewFile();
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8")));
		out.write("queryID	keyword:count	keyword:count	..." + "\r\n");
		for(int i = 0; i < queryKeywordsArray.size(); i++){
			out.write(indexArray.get(i));//String.valueOf(i+1));
		  	for(String tempKeyword: queryKeywordsArray.get(i).keySet())
		  		out.write("	" + tempKeyword + ":" + queryKeywordsArray.get(i).get(tempKeyword));
		  	out.write("	" + "\r\n");
		 }
		 out.flush();
		 out.close();
	}
	
	public void getKeywordsDistribution(){	//统计所有关键词的出现频率，即（每个关键词在query中出现次数/query总数目）
		HashMap<String, Integer> keywordCountMap = new HashMap<String, Integer>();
		for(String tempKeyword: keywordSet.keySet())
			keywordCountMap.put(tempKeyword, 0);
		for(String tempKeyword: twoKeywordSet.keySet())
			keywordCountMap.put(tempKeyword, 0);
		for(int i = 0; i < queryKeywordsArray.size(); i++){
			HashMap<String, Integer> tempKeywordCount = queryKeywordsArray.get(i);
			for(String tempKeyword: tempKeywordCount.keySet())
				keywordCountMap.put(tempKeyword, keywordCountMap.get(tempKeyword) + 1);
		}
		//排序
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(keywordCountMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
		    public int compare(Map.Entry<String,  Integer> o1, Map.Entry<String,  Integer> o2) {
		    	return (o2.getValue() - o1.getValue()); 
		    }
		});
		for(int i = 0; i < list.size(); i++)
			System.out.println(list.get(i).getKey() + "	" + (double)list.get(i).getValue()/(double)queryCount);
	}
	
	public static void main(String[] args) throws IOException {
		KeywordCounting keywordCounting = new KeywordCounting();
		keywordCounting.initialKeywordSet();
		keywordCounting.initialQueryArray();
		keywordCounting.countKeywordsOfQuery();
		keywordCounting.writeToFile();
		keywordCounting.getKeywordsDistribution();
	}
}
