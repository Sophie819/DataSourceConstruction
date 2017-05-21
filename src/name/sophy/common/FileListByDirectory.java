package name.sophy.common;

import java.io.File;
import java.util.ArrayList;

public class FileListByDirectory {
	 private static ArrayList<File> getFileListByDirectory(File file) {
		  ArrayList<File> fileList = new  ArrayList<File>();
		  File flist[] = file.listFiles();
		  for (File f : flist) 
		      fileList.add(f);
		  return fileList;
		}
	 
	 public static void main(String[] args) {
		 
		 FileListByDirectory.getFileListByDirectory(new File("data"));
	}
}
