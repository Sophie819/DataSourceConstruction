package name.sophy.classdataprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
/*
 *	读取文件计数
 */
public class ClassEntitySPOCountByFiles {
	
	private HashMap<String, HashSet<String>> classEntityMap = new HashMap<String, HashSet<String>>();//每个class定义的所有实体
    private HashMap<String, Integer> entitySpoCountMap = new HashMap<String, Integer>();//在所有三元组中，每个实体作为主语出现了多少次
    private HashMap<String, Integer> classSpoCountMap = new HashMap<String, Integer>();//在所有三元组中，每个class定义的所有实体作为主语出现了多少次
    private int undefinedEntitySpoCount = 0;//在所有三元组中，未定义class的实体作为主语出现了多少次
    
	public void initialClassEntityMap() {
		File file = new File("ontology/class.txt");	//存储所有的class（除了thing之外），不包含左右尖括号
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
            	HashSet<String> entitySet = new HashSet<String>();
            	classEntityMap.put("<" + tempLine + ">", entitySet);
            }
            System.out.println("Class count : " + classEntityMap.size());
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
            
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
	}
	
	public void setClassEntityMap(){	 //读取两个type文件，构造classEntityMap
		File flist[] = new File("type").listFiles();
		for(int i = 0; i < flist.length; i++){
			System.out.println(flist[i].getAbsolutePath());
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(flist[i]));
	            String tempLine = null;
	            tempLine = reader.readLine();	//第一行舍去
	            while ((tempLine = reader.readLine()) != null) {
	                String [] tempArray = tempLine.split(" ");
	                if(classEntityMap.containsKey(tempArray[2]) && tempArray[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))
	                	classEntityMap.get(tempArray[2]).add(tempArray[0]);
	            }
	            reader.close();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
		}
	}
	
	public void setEntitySpoCountMap() {
		File flist[] = new File("data").listFiles();
		for(int i = 0; i < flist.length; i++){
			System.out.println(flist[i].getAbsolutePath());
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(flist[i]));
	            String tempLine = null;
	            tempLine = reader.readLine();	//第一行舍去
	            while ((tempLine = reader.readLine()) != null) {
	                String [] tempArray = tempLine.split(" ");
	                if(entitySpoCountMap.containsKey(tempArray[0])){
	                	int newCount = entitySpoCountMap.get(tempArray[0]) + 1;
	                	entitySpoCountMap.put(tempArray[0], newCount);
	                }
	                else {
						entitySpoCountMap.put(tempArray[0], 1);
					}	
	            }
	            reader.close();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
		}
	}
	
	public void sumUp() {
		int id = 1;
		for(String classKey:classEntityMap.keySet()){	//每个class
			System.out.print(classKey + "	");
			int spoCount = 0;
			HashSet<String> entitySet = classEntityMap.get(classKey);
			System.out.print(entitySet.size() + "	");
			for(String entity:entitySet){				//每个class对应的所有实体
				if(entitySpoCountMap.containsKey(entity))
					spoCount += entitySpoCountMap.get(entity);
			}
			System.out.print(spoCount + "\n");
			classSpoCountMap.put(classKey, spoCount);
			id++;
		}
	}
	
	public static void main(String[] args) {
		ClassEntitySPOCountByFiles classEntitySPOCountByFiles = new ClassEntitySPOCountByFiles();
		classEntitySPOCountByFiles.initialClassEntityMap();
		classEntitySPOCountByFiles.setClassEntityMap();
		classEntitySPOCountByFiles.setEntitySpoCountMap();
		classEntitySPOCountByFiles.sumUp();
	}
}
