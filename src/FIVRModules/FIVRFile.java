package FIVRModules;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class FIVRFile implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int MAX_FILENAME_BYTE_SIZE = 100;
	
	private String filename;
	private long size;
	private byte[] data;

	public FIVRFile(String filename) {
		
		try {
			if (filename.getBytes().length > MAX_FILENAME_BYTE_SIZE) {
				throw new Exception();
			}
			File tFile = new File(filename);
			if (!tFile.exists()) {
				throw new Exception();
			}
	    	this.filename = filename;
	    	this.size = tFile.length();
	    	Path path = Paths.get(tFile.toURI());
	    	
	    	byte[] fileContent = Files.readAllBytes(path);
	    	byte[] fileNameContent = filename.getBytes();
	    	
	    	int finalSize = MAX_FILENAME_BYTE_SIZE + fileContent.length;
	    	byte[] fileTotal = new byte[finalSize];
	    	
	    	for (int i=0; i < finalSize; i++) {
	    		if (i < MAX_FILENAME_BYTE_SIZE) {
	    			try {
	    				fileTotal[i] = fileNameContent[i];
	    			} catch (Exception e) {
	    				
	    			}
	    		} else {
	    			fileTotal[i] = fileContent[i-MAX_FILENAME_BYTE_SIZE];
	    		}
	    	}
	    	
	    	this.data = fileTotal;
	    	
		} catch (Exception e) {
			System.out.println("Could not read that file.");
		}
    }
	
	public FIVRFile(byte[] data) {
		try {
			byte[] fileName = Arrays.copyOfRange(data, 0, MAX_FILENAME_BYTE_SIZE);
			byte[] fileContent = Arrays.copyOfRange(data, MAX_FILENAME_BYTE_SIZE, data.length);
			this.filename = new String(fileName.toString());
			this.data = data;
			this.size = fileContent.length;
			Files.write(Paths.get("server-" + this.filename), fileContent);
		} catch (Exception e) {
			System.out.println("Error when saving file: " + e.getMessage());
		}
	}

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
