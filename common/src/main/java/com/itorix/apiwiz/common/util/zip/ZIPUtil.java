package com.itorix.apiwiz.common.util.zip;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

public class ZIPUtil {

	public ZIPUtil() {

	}

	public static void main(String[] args) {
		ZIPUtil unZip = new ZIPUtil();
		try {
			unZip.unzip("C:/tmp/test/API.zip", "C:/tmp/test/unzip");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void unzip(String file, String outputFolder) throws FileNotFoundException, IOException, ArchiveException {
		File inputFile = new File(file);
		 
        InputStream is = new FileInputStream(inputFile);
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("zip", is);
        ZipEntry entry = null;
 
        while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {
 
            if (entry.getName().endsWith("/")) {
                File dir = new File(outputFolder + File.separator + entry.getName());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                continue;
            }
 
            File outFile = new File(outputFolder + File.separator + entry.getName());
 
            if (outFile.isDirectory()) {
                continue;
            }
 
            if (outFile.exists()) {
                continue;
            }
 
            FileOutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = ais.read(buffer)) > 0) {
                out.write(buffer, 0, length);
                out.flush();
            }
            out.close();
 
        }
        is.close();
    }
	
	
	public void unZipIt(String zipFile, String outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public  List<File> getJsonFiles(String folder){
		List<File> files = new ArrayList<File>();
		getFiles(folder, ".json", files);
		getFiles(folder, ".yaml", files);
		return files;
	}

	public void getFiles(String folder, String extension, List<File> files){
		File dir = new File(folder);
		for(File file : dir.listFiles())
			if(file.isDirectory())
				getFiles(file.getAbsolutePath(), extension, files);
			else
				if(getFileExtension(file).equals(extension))
					files.add(file);
	}

	private String getFileExtension(File file) {
		int lastIndexOf = file.getName().lastIndexOf(".");
		if (lastIndexOf == -1) return ""; 
		String ext  = file.getName().substring(lastIndexOf);
		return ext;
	}

}