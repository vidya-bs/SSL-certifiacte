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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZIPUtil {
	private static final Logger logger = LoggerFactory.getLogger(ZIPUtil.class);
	public ZIPUtil() {
	}

	public static void main(String[] args) {
		ZIPUtil unZip = new ZIPUtil();
		try {
			unZip.unzip("C:/tmp/test/API.zip", "C:/tmp/test/unzip");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}
	}

	public void unzip(String file, String outputFolder) throws FileNotFoundException, IOException, ArchiveException {
		File inputFile = new File(file);

		InputStream is = new FileInputStream(inputFile);
		ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("zip", is);
		ZipEntry entry = null;

		while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {
			try {
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

			} catch (Exception ex) {
				logger.error("Exception occurred", ex);
			}
		}
		is.close();
	}

	public void unzipV2(String zipFilePath, String destinationFolder) throws IOException {
		byte[] buffer = new byte[1024];

		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				String entryName = zipEntry.getName();
				String entryPath = destinationFolder + File.separator + entryName;

				File entryFile = new File(entryPath);
				if (zipEntry.isDirectory()) {
					entryFile.mkdirs();
				} else {
					entryFile.getParentFile().mkdirs();

					try (FileOutputStream outputStream = new FileOutputStream(entryFile)) {
						int length;
						while ((length = zipInputStream.read(buffer)) > 0) {
							outputStream.write(buffer, 0, length);
						}
					}
				}

				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();
			}
		}
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

				logger.info("file unzip : " + newFile.getAbsoluteFile());

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

			logger.info("Done");

		} catch (IOException ex) {
			logger.error("Exception occurred", ex);
		}
	}

	public List<File> getJsonFiles(String folder) {
		List<File> files = new ArrayList<File>();
		getFiles(folder, ".json", files);
		getFiles(folder, ".yaml", files);
		return files;
	}


	public List<File> getGrapgQLFiles(String folder) {
		List<File> files = new ArrayList<File>();
		getFiles(folder, ".graphql", files);
		getFiles(folder, ".gql", files);
		return files;
	}

	public void getFiles(String folder, String extension, List<File> files) {
		File dir = new File(folder);
		for (File file : dir.listFiles())
			if (file.isDirectory())
				getFiles(file.getAbsolutePath(), extension, files);
			else if (getFileExtension(file).equals(extension))
				files.add(file);
	}

	private String getFileExtension(File file) {
		int lastIndexOf = file.getName().lastIndexOf(".");
		if (lastIndexOf == -1)
			return "";
		String ext = file.getName().substring(lastIndexOf);
		return ext;
	}
}
