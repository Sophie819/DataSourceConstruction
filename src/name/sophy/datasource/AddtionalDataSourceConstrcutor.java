package name.sophy.datasource;

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
import java.util.Map.Entry;

/*
 * 处理除了137个class之外的3千万的三元组
 * 1.如果这个class对应的三元组数目在1000~500,0000，则这个class作为分组依据
 * 2.将其他所有（不足1000的）class合并起来
 * 3.随机分组，保证同一主语的所有三元组在同一个数据源中即可
 */
public class AddtionalDataSourceConstrcutor {
	private String[] fileNames = {"113","111","109","108","106","104","144","143",
									"145","146","147","148","149"};
	private HashMap<String, HashSet<String>> classEntityMap = new HashMap<String, HashSet<String>>();			//实体对应class定义
	private HashMap<String, HashSet<Integer>> entityIndexMap = new HashMap<String, HashSet<Integer>>();			//存储实体对应的三元组id
	private HashMap<String, HashSet<Integer>> dataSourceIdMap = new HashMap<String, HashSet<Integer>>();//存储被分配好的data source
	int [] used = new int[38588187];	//该三元组是否被使用
	
	public  void initialClassEntity() throws IOException {
		System.out.println("1.initial class entity map");
		int count_1 = 0;
		for(int i = 0; i < fileNames.length; i++){
			BufferedReader reader = null;
	        reader = new BufferedReader(new FileReader("E:\\SoftSet\\virtuoso-opensource\\database\\data_source\\data-source-" + 
	        											fileNames[i] + ".ttl"));
	        String tempLine = null;
	        while ((tempLine = reader.readLine()) != null) {
		           String []tempArray = tempLine.split(" ");
		           if(tempArray[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") &&
		        		   tempArray[2].startsWith("<http://dbpedia.org/ontology/")){
		        	   count_1++;
		        	   if(classEntityMap.containsKey(tempArray[2])){
		        		   classEntityMap.get(tempArray[2]).add(tempArray[0]);
		        	   }
		        	   else{
		        		   HashSet<String> entities = new HashSet<String>();
		        		   entities.add(tempArray[0]);
		        		   classEntityMap.put(tempArray[2], entities);
		        	   }
		           }
	        }
	        reader.close();
	        System.out.println("***" + i + " : data-source-" + fileNames[i] + ".ttl, " + count_1 + " triples.***");
		   	
		}
		System.out.println("***" + count_1 + " triples 有class定义.***");
        for(String classKey: classEntityMap.keySet()){
        	System.out.println(classKey + " : " + classEntityMap.get(classKey).size());
        }
	}
	
	public void initialEntityTriple() throws IOException{
		System.out.println("2.initial entity index map");
		int count_1 = 0;
		for(int i = 0; i < fileNames.length; i++){
			BufferedReader reader = null;
	        reader = new BufferedReader(new FileReader("E:\\SoftSet\\virtuoso-opensource\\database\\data_source\\data-source-" + 
	        											fileNames[i] + ".ttl"));
	        String tempLine = null;
	        while ((tempLine = reader.readLine()) != null) {
		           String []tempArray = tempLine.split(" ");
		           String tempEntity = tempArray[0];
		           if(entityIndexMap.containsKey(tempEntity)){
		        	   entityIndexMap.get(tempEntity).add(count_1);
		           }
		           else{
		        	   HashSet<Integer> tempIndexSet = new HashSet<Integer>();
		        	   tempIndexSet.add(count_1);
		        	   entityIndexMap.put(tempEntity, tempIndexSet);
		           }
		           count_1++;
	        }
	        reader.close();
	        System.out.println("***" + i + " : data-source-" + fileNames[i] + ".ttl, " + count_1 + " triples.***");
		}
		System.out.println("******" + count_1 + " triples, " + entityIndexMap.size() + " entities.******");
		for(String classKey:classEntityMap.keySet()){
        	System.out.println(classKey + " : " + getTripleCountOfClass(classKey));
        }
		
//		HashSet<Integer> tempIndexSet = new HashSet<Integer>();
//		dataSourceIdMap.put("severalSmallClass", tempIndexSet);	//初始化,添加这个类
	}
	
	public void constructDataSource() throws IOException{
		//先将classEntityMap对class进行排序，按照从小到大的顺序遍历
		//如果对应的三元组数目在1000~500,0000，则构成一个数据源
		System.out.println("3.construct data source");
		System.out.println("3.1.sort out class entity map according to classes' tripleSystem.out.println count");
		List<Entry<String, HashSet<String>>> list = new ArrayList<Entry<String, HashSet<String>>>(classEntityMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, HashSet<String>>>() {
		    public int compare(Map.Entry<String,  HashSet<String>> o1, Map.Entry<String,  HashSet<String>> o2) {
		        //return 0;	
		    	return (getTripleCountOfClass(o1.getKey()) - getTripleCountOfClass(o2.getKey())); 
		    }
		});
		for (int i = 0; i < list.size(); i++) {
			String classKey = list.get(i).getKey();
		    System.out.println(list.get(i).getKey() + ", " + getTripleCountOfClass(classKey));
		    int count = getTripleCountOfClass(classKey);
			if(count >= 1000 && count <= 5000000){
				HashSet<Integer> indexSet = new HashSet<Integer>();
				dataSourceIdMap.put(classKey, indexSet);
	            for(String tempEntity:classEntityMap.get(classKey)){
	            	if(entityIndexMap.containsKey(tempEntity)){	//如果存在以这个实体作为主语出现的triple
						HashSet<Integer> entityIndexSet = entityIndexMap.get(tempEntity);//这个实体对应所有triple的index
						for(Integer index:entityIndexSet){	//这个实体的某一个triple
							if(used[index] == 0){			//判断这个triple是否已经被使用
								dataSourceIdMap.get(classKey).add(index);
								used[index] = 1;
							}
						}//for
					}//if
	            	entityIndexMap.remove(tempEntity);	//删去对应entity的数据
	            }//for
			}//if
		}//for
		System.out.println("******************************");
		
		for (int i = 0; i < list.size(); i++) {
			String classKey = list.get(i).getKey();
		    System.out.println(list.get(i).getKey() + ", " + getTripleCountOfClass(classKey));
//		    int count = getTripleCountOfClass(classKey);
//			if(count < 1000){	//如果对应的三元组数目在0~1000，则构成一个数据源
//	            for(String tempEntity:classEntityMap.get(classKey)){
//	            	if(entityIndexMap.containsKey(tempEntity)){	//如果存在以这个实体作为主语出现的triple
//						HashSet<Integer> entityIndexSet = entityIndexMap.get(tempEntity);//这个实体对应所有triple的index
//						for(Integer index:entityIndexSet){	//这个实体的某一个triple
//							if(used[index] == 0){			//判断这个triple是否已经被使用
//								dataSourceIdMap.get("severalSmallClass").add(index);
//								used[index] = 1;
//							}
//						}//for
//					}//if
//	            	entityIndexMap.remove(tempEntity);	//删去对应entity的数据
//	            }//for
//			}
		}
		System.out.println("******************************");
		
		//其他超过500,0000的数据
		int tempFileCount = 0;		//剩下的
		int tempTripleCount = 0;
		HashSet<Integer> indexSet = new HashSet<Integer>();
		dataSourceIdMap.put("additional" + tempFileCount, indexSet);
		for(String entityKey : entityIndexMap.keySet()){
			HashSet<Integer> entityIndexSet = entityIndexMap.get(entityKey);
			if(tempTripleCount + entityIndexSet.size() > 5000000){
				tempFileCount++;
				tempTripleCount = 0;
				HashSet<Integer> tempIndexSet = new HashSet<Integer>();
				dataSourceIdMap.put("additional" + tempFileCount, tempIndexSet);
			}
			for(Integer index:entityIndexSet){	//这个实体的某一个triple
				if(used[index] == 0){			//判断这个triple是否已经被使用
					dataSourceIdMap.get("additional" + tempFileCount).add(index);
					tempTripleCount++;
				}
			}//for
		}
	}
	
	public int getTripleCountOfClass(String classStr){
		int count = 0;
		for(String tempEntity:classEntityMap.get(classStr)){
			if(entityIndexMap.get(tempEntity) != null){
				for(Integer index:entityIndexMap.get(tempEntity)){	//注意判断这个三元组index有没有被使用了
					if(used[index] == 0)
						count++;
				}
			}
		}
		return count;
	}
	
	public void writeToFile() throws IOException{
		System.out.println("4.Begin to writning to files");
		int i = 0;
		for(String sourceKey : dataSourceIdMap.keySet()){
			System.out.println("	" + i + " " + sourceKey + " : " + dataSourceIdMap.get(sourceKey).size() + " triples.");
//			File filename = new File("dataSourceId/data-source-id-" + i + ".txt");
//            filename.createNewFile();
//            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8")));
//            for(Integer index : dataSourceIdMap.get(sourceKey))
//            	out.write(index + "\r\n");
//            out.flush();
//            out.close();
			i++;
		}
		System.out.println("******end******");
	}
	
	public static void main(String[] args) throws IOException {
		AddtionalDataSourceConstrcutor addtionalDataSourceConstrcutor = new AddtionalDataSourceConstrcutor();
		addtionalDataSourceConstrcutor.initialClassEntity();
		addtionalDataSourceConstrcutor.initialEntityTriple();
		addtionalDataSourceConstrcutor.constructDataSource();
		addtionalDataSourceConstrcutor.writeToFile();
	}
}
