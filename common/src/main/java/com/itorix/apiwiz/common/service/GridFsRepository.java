package com.itorix.apiwiz.common.service;

import com.itorix.apiwiz.common.model.GridFsData;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Component;

@Component("gridFsRepository")
public class GridFsRepository {

	@Autowired
	GridFsOperations gridFsOperations;

	public String storeFile(GridFsData gridFsData) {
		DBObject metaData = gridFsData.getMetaData();
		String gridFSFile = null;
		InputStream inputStream = null;
		try {
			if (gridFsData.getInputStream() != null) {
				inputStream = gridFsData.getInputStream();
			} else {
				inputStream = new FileInputStream(gridFsData.getFilePath());
			}
			ObjectId id = gridFsOperations.store(inputStream, gridFsData.getFilename(), gridFsData.getContentType(),
					metaData);
			gridFSFile = id.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return gridFSFile;
	}

	public GridFSFile store(GridFsData gridFsData) {
		DBObject metaData = gridFsData.getMetaData();
		GridFSFile gridFSFile = null;
		InputStream inputStream = null;
		try {
			if (gridFsData.getInputStream() != null) {
				inputStream = gridFsData.getInputStream();
			} else {
				inputStream = new FileInputStream(gridFsData.getFilePath());
			}
			ObjectId id = gridFsOperations.store(inputStream, gridFsData.getFilename(), gridFsData.getContentType(),
					metaData);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return gridFSFile;
	}

	public File findById(GridFsData gridFsData) throws IOException {
		GridFSFile gridFSDBFile = gridFsOperations.findOne(new Query(Criteria.where("_id").is(gridFsData.getId())));
		String filePath = gridFsData.getFilePath();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			fileOutputStream.flush();
			// gridFSDBFile.writeTo(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(filePath);
	}

	public File findLatestByName(GridFsData gridFsData) throws IOException {
		Query query = new Query(Criteria.where("filename").is(gridFsData.getFilename()));
		List<String> fields = new ArrayList<>();
		query.with(Sort.by(Direction.DESC, "uploadDate"));
		List<GridFSDBFile> gridFSDBFiles = null;
		GridFSFindIterable collection = gridFsOperations.find(query);
		if (collection != null) {
			Iterator iterator = collection.iterator();
			gridFSDBFiles = new ArrayList<>();
			while (iterator.hasNext()) {
				gridFSDBFiles.add((GridFSDBFile) iterator.next());
			}
		}
		// List<GridFSDBFile> gridFSDBFiles = gridFsOperations.find(query);
		GridFSDBFile gridFSDBFile = null;
		if (null != gridFSDBFiles && gridFSDBFiles.size() > 0) {
			gridFSDBFile = gridFSDBFiles.get(0);
		}
		String filePath = gridFsData.getFilePath();
		try {
			gridFSDBFile.writeTo(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(filePath);
	}

	public void deleteById(String id) throws IOException {
		gridFsOperations.delete(new Query(Criteria.where("_id").is(id)));
	}

	public void deleteByName(String filename) throws IOException {
		gridFsOperations.delete(new Query(Criteria.where("filename").is(filename)));
	}
}
