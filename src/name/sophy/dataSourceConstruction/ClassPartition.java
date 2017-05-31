package name.sophy.dataSourceConstruction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*
 * 按照每个class定义的实体对应的spo数目，给出按照class划分的规则（就是获得哪些class直接构成数据源）
 * 广度优先算法
 */
public class ClassPartition {
	private String[][] subClassOfRelArray = new String[755][2];//class A is subclassof class B
	private String[] classArray = new String[755];	//class
	private int[] spoCountArray = new int[755]; 	//每个class对应spo数量
	
	public void setSubClassOfRel(){
		File file = new File("ontology/ontology.txt");	//存储所有的class，不包含左右尖括号
        BufferedReader reader = null;
        try {
        	int index = 0;
            reader = new BufferedReader(new FileReader(file));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
                String [] tempArray = tempLine.split("	");
                subClassOfRelArray[index][0] = "<" + tempArray[0] + ">";
                subClassOfRelArray[index][1] = "<" + tempArray[1] + ">";
                index++;
            }
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void setClassAndSpoCount(){
		File file = new File("ontology/class.txt");	//存储所有的class，不包含左右尖括号
        BufferedReader reader = null;
        try {
        	int index = 0;
            reader = new BufferedReader(new FileReader(file));
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
                String [] tempArray = tempLine.split("	");
                classArray[index] = tempArray[0];
                spoCountArray[index] = Integer.parseInt(tempArray[2]);
                index++;
            }
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public ArrayList<String> getChildsOfParentClass(String parentClass){	//subClassOfRelArray
		ArrayList<String> temp = new ArrayList<String>();
		for(int i = 0; i < 755; i++){
			if(subClassOfRelArray[i][1].equals(parentClass))
				temp.add(subClassOfRelArray[i][0]);
		}
		return temp;
	}
	
	public int getSpoCountOfClass(String classStr){	//class spoCount
		for(int i = 0; i < 755; i++){
			if(classArray[i].equals(classStr))
				return spoCountArray[i];
		}
		return -1;
	}
	
	public void judge(String classStr, int depth){	//进行广度优先搜索,判断哪些class可以直接被分成一个数据源
		ArrayList<String> childs = getChildsOfParentClass(classStr);
		//System.out.println(classStr + " : " + childs.size());
		for(int i = 0; i < childs.size(); i++){
			String tempClass = childs.get(i);
			int tempCount = getSpoCountOfClass(tempClass);
			//System.out.println(tempClass + " :: " + tempCount);
			if( tempCount > 5000000){
				System.err.println("******" + tempClass + "	" + tempCount + ", depth = " + depth + "******");
				judge(tempClass, depth + 1);
			}else if(tempCount < 1000){
				//舍弃
			}else{
				System.out.println(tempClass + "	" + tempCount + ", depth = " + depth);
			}
		}
	}
	
	public static void main(String[] args) {
		ClassPartition classPartition = new ClassPartition();
		classPartition.setSubClassOfRel();
		classPartition.setClassAndSpoCount();
		classPartition.judge("<http://www.w3.org/2002/07/owl#Thing>", 1);
	}
}
