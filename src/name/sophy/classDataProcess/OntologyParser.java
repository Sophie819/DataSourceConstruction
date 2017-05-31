package name.sophy.classDataProcess;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

/*
 * 此类的主要作用是解析Ontology的nt文件，获得DBpedia(2016-04版本)的所有class定义，并获取这些class构成的树结构，树结构存储在文件中。
 * Get all class definitions from dbpedia_2016-04.nt, and store the tree structure of these classes in a file.
 */
public class OntologyParser {
	public static void main(String args[]) throws Exception
	{
		String inputFileName = "data/dbpedia_2016-04.nt";
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open(inputFileName);
		if (in == null) 
		{
			throw new IllegalArgumentException("File: " + inputFileName + " not found");
		}
		model.read(in, "","N3");
		StmtIterator iter = model.listStatements();
		
		//计数
		int subClassOfCount = 0;	//记录有多少subclassof关系 
		ArrayList<String> classArray = new ArrayList<String>();
		ArrayList<String> childClassArray = new ArrayList<String>();
		while (iter.hasNext()) 
		{	
			Statement stmt = iter.nextStatement(); 
			String subject = stmt.getSubject().toString(); 
			String predicate = stmt.getPredicate().toString();
			RDFNode object = stmt.getObject(); 
/*
			//判断谓词关系是否是“subclassof”，插入表中
            if(predicate.equals("http://www.w3.org/2000/01/rdf-schema#subClassOf") 
            		&& subject.startsWith("http://dbpedia.org/ontology/") && 
            		( object.toString().startsWith("http://dbpedia.org/ontology/") || object.toString().equals("http://www.w3.org/2002/07/owl#Thing"))){
            	if(!classArray.contains(subject))
            		classArray.add(subject);
            	if(!childClassArray.contains(subject))
            		childClassArray.add(subject);
            	//else	//重复定义父类节点的子类
            		//System.out.println("******重复定义父类的class******： " + subject + " subclassof " + object.toString());
            	
            	if(!classArray.contains(object.toString()))
            		classArray.add(object.toString());
            	System.out.println(subject + "	" + object.toString());
            	subClassOfCount++;
            }
//            else if(predicate.equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")){
//            	System.out.println("******包含不是DBpedia中定义的class******：" + subject + "	" + object.toString());
//			}
*/
			if(predicate.equals("http://www.w3.org/2000/01/rdf-schema#subClassOf") && object.toString().equals("http://www.w3.org/2002/07/owl#Thing")){
				System.out.println(subject);
			}
        }
		//System.out.println("Node : " + classArray.size() + ", Edge : " + subClassOfCount);
			
	}
}
