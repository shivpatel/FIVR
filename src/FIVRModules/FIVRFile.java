package FIVRModules;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FIVRFile implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String filename;
	private long size;
	private byte[] data;

	public FIVRFile(String filename) {
		try {
			File tFile = new File(filename);
			if (!tFile.exists()) {
				throw new Exception();
			}
	    	this.filename = filename;
	    	this.size = tFile.length();
	    	Path path = Paths.get(tFile.toURI());
	    	this.data = Files.readAllBytes(path);
		} catch (Exception e) {
			System.out.println("Could not read that file.");
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
