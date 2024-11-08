package com.smart.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class FileDeleteHelper {

	public final String UPLOAD_DIR = new ClassPathResource("static/img/").getFile().getAbsolutePath();

	public FileDeleteHelper() throws IOException{
		
	}
	
	public boolean deleteFile(String fileName) {
		
		boolean b = false;
		
		try {
			
			Files.delete(Paths.get(UPLOAD_DIR + File.separator + fileName ));
			
			//Files.delete(Path.of(UPLOAD_DIR + File.separator + fileName));
			
			b = true;
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return b;
	}
	
}
