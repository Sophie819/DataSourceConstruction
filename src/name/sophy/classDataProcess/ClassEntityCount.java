package name.sophy.classDataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

/*
 * 统计每个class对应多少个不同的实体
 */
public class ClassEntityCount {
	public static void main(String[] args) {
        ArrayList<String> classArray = new ArrayList<String>();
        ArrayList<Integer> classEntityNum = new ArrayList<Integer>();
        for(int i = 0; i < 755; i++)	//初始化
        	classEntityNum.add(0);
        
        //存储所有的class，不包含左右尖括号
		File file = new File("data/ontology/ontology.txt");	
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempLine = null;
            int count = 0;
            while ((tempLine = reader.readLine()) != null) {
                String [] tempArray = tempLine.split("	");
                if(!classArray.contains(tempArray[0])){
                	classArray.add(tempArray[0]);
                	count++;
                }
            }
            classArray.add("http://www.w3.org/2002/07/owl#Thing");
            count++;
            System.out.println("Class count : " + count);
            
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
        
        //遍历instance_type，统计每个class对应多少不同的实体
        String inputFileName = "data/instance_types_transitive_en.ttl";
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open(inputFileName);
		if (in == null) 
		{
			throw new IllegalArgumentException("File: " + inputFileName + " not found");
		}
		model.read(in, "","N3");
		StmtIterator iter = model.listStatements();
		int count = 0;
		while (iter.hasNext()) 
		{	
			Statement stmt = iter.nextStatement(); 
			String subject = stmt.getSubject().toString(); 
			String predicate = stmt.getPredicate().toString();
			RDFNode object = stmt.getObject();
			//System.out.println(subject + " -> " + object.toString());
			if(classArray.contains(object.toString())){
				//System.out.println(subject + " -> " + object.toString());
				int indexOfThisClass = classArray.indexOf(object.toString());
				classEntityNum.set(indexOfThisClass, classEntityNum.get(indexOfThisClass) + 1);
			}
			count++;
			if(count % 10000 == 0)
				System.out.println(count);
		}
		System.out.println(count);
		try {
			model.close();
			in.close();
			iter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish instance type!");
		
		for(int i = 0 ; i < 755; i++){
			System.out.println(classArray.get(i) + " : " + classEntityNum.get(i));
		}
		System.out.println("***************************************************");
		for(int i = 0 ; i < 755; i++){
			System.out.println(classEntityNum.get(i));
		}
	}
}
