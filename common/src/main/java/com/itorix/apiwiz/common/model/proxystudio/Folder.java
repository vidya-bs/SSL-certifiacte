package com.itorix.apiwiz.common.model.proxystudio;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Folder {

	private List<Folder> files;
	private String name;
	private boolean isFolder;

	public Folder(String name, boolean isFolder) {
		this.name = name;
		this.isFolder = isFolder;
	}

	public Folder() {
	}

	public List<Folder> getFiles() {
		return files;
	}

	public void setFiles(List<Folder> files) {
		this.files = files;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public void addFile(Folder file) {
		if (files == null)
			files = new ArrayList<Folder>();
		files.add(file);
	}

	public Folder getFile(String fileName) {
		if (files != null) {
			for (Folder file : files) {
				if (file.getName().equalsIgnoreCase(fileName))
					return file;
			}
		}
		return null;
	}

	public boolean removeFile(String fileName) {
		if (files != null) {
			for (Folder file : files) {
				if (file.getName().equalsIgnoreCase(fileName)) {
					files.remove(file);
					return true;
				}
			}
		}
		return false;
	}
}
