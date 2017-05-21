package name.sophy.datasource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;


/*
 * 将11个文件合并成1个文件
 */
public class FileCombination {
	String[] allSPO = new String[103295215 - 22 - 56989265];	//7个文件：56989265//103295215 - 22
	
	public void loadTriple() {
		File flist[] = new File("data").listFiles();
		int count = 0;
		for(int i = 7; i < flist.length; i++){	
	        BufferedReader reader = null;
	        int count_1 = 0;
	        try {
	            reader = new BufferedReader(new FileReader(flist[i]));
	            String tempLine = null;
	            while ((tempLine = reader.readLine()) != null) {
	               if(tempLine.startsWith("<")){
	            	   allSPO[count] = tempLine;
		               count++;
		               count_1++;
	               }
	            }
	            reader.close();
	            System.out.println(flist[i].getAbsolutePath() + " : " + count_1 + ", " + count);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		 System.out.println("******Finish loading " + count + " triples.******");
	}
	
	public void writeFile(){
		try {
            RandomAccessFile randomFile = new RandomAccessFile("all_data.ttl", "rw"); // 打开一个随机访问文件流，按读写方式
            long fileLength = randomFile.length(); // 文件长度，字节数
            randomFile.seek(fileLength);//将写文件指针移到文件尾。
            for(int i = 0; i < allSPO.length; i++){
            	randomFile.writeBytes(allSPO[i] + "\r\n");
            }
            randomFile.close();
            System.out.println("******Finish writing to file.******");
        } 
		catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args) {
		FileCombination fileCombination = new FileCombination();
		fileCombination.loadTriple();
		//fileCombination.writeFile();
	}
}
