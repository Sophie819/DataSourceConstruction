package name.sophy.dataSourceConstruction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class DataSourceMapIdToUri {
	private String[] fileNames = {"113","111","109","108","106","104","102","101"};
	private String[] triples = new String[38588187];	//存储11个文件的所有三元组
	private HashMap<String, HashSet<Integer>> dataIdMap = new HashMap<String, HashSet<Integer>>();	//存储被分配好的data source
	
	public void loadData() throws IOException {
		//存储所有三元组
		int count = 0;
		for(int i = 0; i < fileNames.length; i++){
			BufferedReader reader = null;
	        reader = new BufferedReader(new FileReader("E:\\SoftSet\\virtuoso-opensource\\database\\data_source\\data-source-" + 
	        											fileNames[i] + ".ttl"));
	        String tempLine = null;
	        while ((tempLine = reader.readLine()) != null) {
		        	if(tempLine.startsWith("<")){
		                triples[count] = tempLine;
		                count++;
	                }
	        }
	        reader.close();
	        System.out.println("***" + i + " : data-source-" + fileNames[i] + ".ttl, " + count + " triples.***");
		}
		
		/*File flist[] = new File("data").listFiles();
		int count = 0;
		for(int i = 0; i < flist.length; i++){
	        BufferedReader reader = null;
	        int count_1 = 0;
            reader = new BufferedReader(new FileReader(flist[i]));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
                if(tempLine.startsWith("<")){
	                triples[count] = tempLine;
	                count++;
	                count_1++;
                }
            }
            reader.close();
            System.out.println(flist[i].getAbsolutePath() + " : " + count_1 + "  " + count);
		}*/
		
		//读取145个文件，将id转化为uri
		File flist[] = new File("dataSourceId").listFiles();
		for(int i = 0; i < flist.length; i++){
			System.out.println(flist[i].getName());
	        BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(flist[i]));
            HashSet<Integer> indexSet = new HashSet<Integer>();
            dataIdMap.put(flist[i].getName(), indexSet);
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
                int tempIndex = Integer.parseInt(tempLine);
                dataIdMap.get(flist[i].getName()).add(tempIndex);
            }
            reader.close();
		}
	}
	
	public void mapIdToUri() throws IOException{
		int i = 0;
		for(String sourceKey : dataIdMap.keySet()){
			System.out.println(i + " : " + sourceKey + " : " + dataIdMap.get(sourceKey).size() + " triples.");
			File filename = new File("dataSource/" + sourceKey.replace("-id", "").replace(".txt", "") + ".ttl");
            filename.createNewFile();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8")));
            for(Integer index : dataIdMap.get(sourceKey)){
            	out.write(triples[index] + "\r\n");
            	//if(count % 1000000 == 0)
            		//System.out.print(triples[index]);
            }
            out.flush();
            out.close();
			i++;
		}
	}
	
	public static void main(String[] args) throws IOException {
		DataSourceMapIdToUri dataSourceMapIdToUri = new DataSourceMapIdToUri();
		dataSourceMapIdToUri.loadData();
		dataSourceMapIdToUri.mapIdToUri();
	}
}
