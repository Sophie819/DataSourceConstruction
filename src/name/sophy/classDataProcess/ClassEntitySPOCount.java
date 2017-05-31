package name.sophy.classDataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
/*
 * 统计总的库dbpedia_data中每个class对应的实体作为subject出现的三元组个数
 * 注意：由于这个方法每次只返回10000条结果，所以我放弃了这个方法，直接对文件进行读取处理
 */
public class ClassEntitySPOCount {
	public static void main(String[] args) throws IOException {
		ArrayList<String> classArray = new ArrayList<String>();			//755个class
		ArrayList<Integer> classEntityNum = new ArrayList<Integer>();
        ArrayList<Integer> classEntitySPONum = new ArrayList<Integer>();//每个class定义的所有实体对应的三元组个数
        for(int i = 0; i < 755; i++)	//初始化
        	classEntitySPONum.add(0);
        
        //存储所有的class，不包含左右尖括号
		File file = new File("ontology/ontology.txt");	
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
    	
        for(int i = 0; i < classArray.size(); i++){	//对每一个class，在instance_type中查找定义的所有entity
        	ArrayList<String> entityArray = new ArrayList<String>();
            ParameterizedSparqlString sparqlstr = new ParameterizedSparqlString("select ?s "
            		+ "where{ ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + classArray.get(i) + "> }");
            URL queryURL = new URL("http://localhost:8890/sparql?default-graph-uri=" + URLEncoder.encode("http://localhost:8890/instance_types", "UTF-8") 
            + "&query=" + URLEncoder.encode(sparqlstr.toString(), "UTF-8") + "&format=xml%2Fhtml&timeout=0&debug=on");
            System.out.println(queryURL.toString());

            URLConnection connAPI = queryURL.openConnection();
            connAPI.setConnectTimeout(20000);
            connAPI.connect();

            ResultSet rs = ResultSetFactory.fromXML(connAPI.getInputStream());
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                entityArray.add(qs.get("s").toString());
                //System.out.println(qs.get("s").toString());
            }
            System.out.println((i + 1) + "	" + classArray.get(i) + "	" + entityArray.size());
            classEntityNum.add(entityArray.size());
            
            /*
            for(int j = 0; j < entityArray.size(); j++){	//对每一个entity，在dbpedia_data中查找spo个数
            	ParameterizedSparqlString sparqlstr_1 = new ParameterizedSparqlString("select count(*) as ?count where{ <" + entityArray.get(j) + "> ?p ?o }");
            	URL queryURL_1 = new URL("http://localhost:8890/sparql?default-graph-uri=" + URLEncoder.encode("http://localhost:8890/dbpedia_data", "UTF-8") 
                + "&query=" + URLEncoder.encode(sparqlstr_1.toString(), "UTF-8") + "&format=xml%2Fhtml&timeout=0&debug=on");
                //System.out.println(queryURL.toString());

            	URLConnection connAPI_1 = queryURL_1.openConnection();
                connAPI_1.setConnectTimeout(20000);
                connAPI_1.connect();

                ResultSet rs_1 = ResultSetFactory.fromXML(connAPI_1.getInputStream());
                while (rs_1.hasNext()) {
                    QuerySolution qs = rs_1.next();
                    int num = Integer.parseInt(qs.get("count").toString().split("\\^\\^")[0]);
                    classEntitySPONum.set(i, classEntitySPONum.get(i) + num);
                    //System.out.println(j + " : " + num + " " + classEntitySPONum.get(i));
                }
            }
            System.out.print("	" + classEntitySPONum.get(i));
            System.out.println();
            */
        }
	}
}

