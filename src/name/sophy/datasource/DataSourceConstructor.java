package name.sophy.datasource;
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
/*
 * 此类用于构造合适的数据源，将所以的11个文件进行拆分
 */
public class DataSourceConstructor {
	private HashMap<String, HashSet<String>> classEntityMap = new HashMap<String, HashSet<String>>();//每个class定义的所有实体
	private HashMap<String, HashSet<Integer>> entityIndexMap = new HashMap<String, HashSet<Integer>>();	//存储SPO中每个（主语）实体对应出现的index
	
	private HashMap<String, HashSet<Integer>> dataSourceIdMap = new HashMap<String, HashSet<Integer>>();	//存储被分配好的data source
	
	private int[] used = new int[103295215 - 22];	//是否已经被分到某一个data source
	
	public void initialDataSourceMap(){
		File file = new File("ontology/classPartition.txt");	//存储所有137个class，用于构造数据源
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
            	String [] tempArray = tempLine.split("	");
            	HashSet<Integer> entitySet = new HashSet<Integer>();
            	dataSourceIdMap.put(tempArray[0], entitySet);
            }
            System.out.println("******2 Class partition count : " + dataSourceIdMap.size() + ".******");
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();  
        } 
	}
	
	public void initialClassEntityMap() {	//读取755个class
		File file = new File("ontology/class.txt");	//存储所有755个class
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
            	String [] tempArray = tempLine.split("	");
            	HashSet<String> entitySet = new HashSet<String>();
            	classEntityMap.put(tempArray[0], entitySet);
            }
            System.out.println("******1 Class count : " + classEntityMap.size() + ".******");
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();  
        } 
	}
	
	public void setClassEntityMap(){	 //读取2个type文件，构造classEntityMap
		File flist[] = new File("type").listFiles();
		for(int i = 0; i < flist.length; i++){
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(flist[i]));
	            String tempLine = null;
	            while ((tempLine = reader.readLine()) != null) {
	                if(tempLine.startsWith("<")){
	                	String [] tempArray = tempLine.split(" ");
		                if(classEntityMap.containsKey(tempArray[2]) && tempArray[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))
		                	classEntityMap.get(tempArray[2]).add(tempArray[0]);
	                }
	            }
	            reader.close();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		System.out.println("******3 Finish loading entities of " + classEntityMap.size() + " classes.******");
	}
	
	public void setSPOTriple() {
		File flist[] = new File("data").listFiles();
		int count = 0;
		for(int i = 0; i < flist.length; i++){
	        BufferedReader reader = null;
	        int count_1 = 0;
	        try {
	            reader = new BufferedReader(new FileReader(flist[i]));
	            String tempLine = null;
	            while ((tempLine = reader.readLine()) != null) {
	                if(tempLine.startsWith("<")){
	                	String [] tempArray = tempLine.split(" ");
		                String subject = tempArray[0];
		                if(entityIndexMap.containsKey(subject)){	//已经存在这个subject
		                	entityIndexMap.get(subject).add(count);
		                }
		                else{	//不存在这个subject
		                	HashSet<Integer> indexSet = new HashSet<Integer>();
		                	indexSet.add(count);
		                	entityIndexMap.put(subject, indexSet);
		                }
		                count++;
		                count_1++;
	                }
	            }
	            reader.close();
	            System.out.println(flist[i].getAbsolutePath() + " : " + count_1 + "  " + count);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		 System.out.println("******4.1 Finish loading " + count + " triples.******");
         System.out.println("******4.2 Finish setting index for " + entityIndexMap.size() + " entities.******");
	}
	
	public void constructDataSourceByClasses(){	//根据137个class构造一部分数据源
		for(String classKey:dataSourceIdMap.keySet()){	//对某个class
			HashSet<String> entitySet = classEntityMap.get(classKey);
			for(String entity:entitySet){				//这个class对应的某个实体
				//System.out.println(entity);
				if(entityIndexMap.containsKey(entity)){	//如果存在以这个实体作为主语出现的triple
					HashSet<Integer> entityIndexSet = entityIndexMap.get(entity);//这个实体对应所有triple的index
					for(Integer index:entityIndexSet){	//这个实体的某一个triple
						//System.out.println("index " + index + " : " + used[index]);
						if(used[index] == 0){			//判断这个triple是否已经被使用
							dataSourceIdMap.get(classKey).add(index);
							used[index] = 1;
						}
					}//for
				}
			}//for
		}//for
		
		int tempFileCount = 0;		//剩下的
		int tempTripleCount = 0;
		HashSet<Integer> indexSet = new HashSet<Integer>();
		dataSourceIdMap.put("add" + tempFileCount, indexSet);
		for(String entityKey : entityIndexMap.keySet()){
			HashSet<Integer> entityIndexSet = entityIndexMap.get(entityKey);
			if(tempTripleCount + entityIndexSet.size() > 5000000){
				tempFileCount++;
				tempTripleCount = 0;
				HashSet<Integer> tempIndexSet = new HashSet<Integer>();
				dataSourceIdMap.put("add" + tempFileCount, tempIndexSet);
			}
			for(Integer index:entityIndexSet){	//这个实体的某一个triple
				if(used[index] == 0){			//判断这个triple是否已经被使用
					dataSourceIdMap.get("add" + tempFileCount).add(index);
					tempTripleCount++;
				}
			}//for
		}
		System.out.println("******5 Finish partition.******");
	}
	
	public void writeIdToFile() throws IOException{	//把每个数据源的key按照字典序进行排序，然后把相应id写入文件
		System.out.println("******6 Begin to writning to files......******");
		int i = 0;
		for(String sourceKey : dataSourceIdMap.keySet()){
			System.out.println("	" + i + " " + sourceKey + " : " + dataSourceIdMap.get(sourceKey).size() + " triples.");
			//String fileName = "dataSourceId/data-source-id-" + i + ".txt";
			/*try {
	            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); // 打开一个随机访问文件流，按读写方式
	            long fileLength = randomFile.length(); 	// 文件长度，字节数
	            randomFile.seek(fileLength);			//将写文件指针移到文件尾。
	            for(Integer index : dataSourceIdMap.get(sourceKey)){
	            	randomFile.writeBytes(index + "\r\n");
	            }
	            randomFile.close();
	        } 
			catch (IOException e) {
	            e.printStackTrace();
	        }*/
			File filename = new File("dataSourceId/data-source-id-" + i + ".txt");
            filename.createNewFile();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8")));
            for(Integer index : dataSourceIdMap.get(sourceKey))
            	out.write(index + "\r\n");
            out.flush();
            out.close();
			i++;
		}
		System.out.println("******end******");
	}
	
	public static void main(String[] args) throws IOException {
		DataSourceConstructor dataSourceConstructor = new DataSourceConstructor();
		dataSourceConstructor.initialClassEntityMap();
		dataSourceConstructor.initialDataSourceMap();
		dataSourceConstructor.setClassEntityMap();
		dataSourceConstructor.setSPOTriple();
		dataSourceConstructor.constructDataSourceByClasses();
		dataSourceConstructor.writeIdToFile();
	}
}
