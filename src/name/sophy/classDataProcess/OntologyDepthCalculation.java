package name.sophy.classDataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.BufferUnderflowException;

/*
 * 读取ontology.txt文件，计算所有。。。
 * 注意：未用到这个类
 */
public class OntologyDepthCalculation {
	/**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static String[][] readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        String[][] childParentClass = new String[755][755];
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempLine = null;
            int count = 0;
            while ((tempLine = reader.readLine()) != null) {
                String [] tempArray = tempLine.split("	");
                childParentClass[count][0] = tempArray[0];
                childParentClass[count][1] = tempArray[1];
                count++;
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
		return childParentClass;
    }
    
	public static void main(String[] args) {
		String[][] childParentArray = OntologyDepthCalculation.readFileByLines("src/data/ontology/ontology.txt");
		
	}
}
