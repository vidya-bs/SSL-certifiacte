package com.itorix.apiwiz.design.studio.businessimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.design.studio.model.EmptyXlsRows;
import com.itorix.apiwiz.design.studio.model.RowData;
import com.itorix.apiwiz.design.studio.model.XmlSchemaVo;



@Component
public class XlsUtil {
	private static final Logger logger = LoggerFactory.getLogger(XlsUtil.class);
//	public static void main(String[] args) throws EmptyXlsRows {
//		XlsUtil xlsReader = new XlsUtil();
//		xlsReader.readExcel("C://Users/anandigam/Documents/My Received Files/config/searchProductOfferSummary_res.xlsx","searchProductOffer");
//	}
	@Autowired
	JfrogUtilImpl jfrogUtilImpl;
	@Autowired
	ApplicationProperties applicationProperties;

	public List<RowData> readExcel(String filePath,String sheetNname) throws EmptyXlsRows {
		List<RowData> listRowDatas = new ArrayList<RowData>();
		try {
			FileInputStream file = new FileInputStream(new File(filePath));
			String extension = filePath.substring(filePath.lastIndexOf(".") + 1,
					filePath.length());
			Sheet sheet = null;
			if (extension.equalsIgnoreCase("xls")) {
				HSSFWorkbook workbook = new HSSFWorkbook(file);
				sheet = workbook.getSheet(sheetNname);
			} else if (extension.equalsIgnoreCase("xlsx")) {
				XSSFWorkbook workbook = new XSSFWorkbook(file);
				sheet = workbook.getSheet(sheetNname);
			}
			for (Row myrow : sheet) {
				int rowNum = myrow.getRowNum();
				Cell includeCell = myrow.getCell(0);
				if (includeCell != null
						&& includeCell.getCellType() != Cell.CELL_TYPE_BLANK
						&& includeCell.getStringCellValue().equalsIgnoreCase(
								"Y") && rowNum !=0) {
					RowData rd = new RowData();
					String xPath = getCellValue(myrow.getCell(1), rowNum);
					rd.setXpath(xPath.substring(1));
					String minOcc = getCellValue(myrow.getCell(2), rowNum);
					rd.setMin(minOcc);
					String maxOcc = getCellValue(myrow.getCell(3), rowNum);
					rd.setMax(maxOcc);
					String xsdType = getCellValue(myrow.getCell(4), rowNum);
					rd.setXsdType(xsdType);
					List<String> list=getEnumCellValue(myrow.getCell(5));
					rd.setEnumcell(list);
					String jsonType = getCellValue(myrow.getCell(6), rowNum);
					rd.setJsonType(jsonType);
					String jsonFormat = getCellValue(myrow.getCell(7));
					rd.setJsonFormat(jsonFormat);
					 String minLength = getCellValue(myrow.getCell(8));
					 rd.setMinLength(minLength);
				     String maxLength = getCellValue(myrow.getCell(9));
				     rd.setMaxLength(maxLength);
				     String length = getCellValue(myrow.getCell(10));
				     rd.setLength(length);
				     String pattern = getCellValue(myrow.getCell(11));
				     rd.setPattern(pattern);
				     String documentation = getCellValue(myrow.getCell(11));
				     rd.setDocumentation(documentation);
					listRowDatas.add(rd);
				}
			}

			file.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listRowDatas;
	}
	
	

	public JSONObject writeExcel(List<XmlSchemaVo> listRowDatas,String sheetName) throws EmptyXlsRows, IOException {
		long timeStamp=System.currentTimeMillis();
		String xsdFileBackUpLocation = applicationProperties.getBackupDir() + timeStamp;
		if (!new File(xsdFileBackUpLocation).exists()) {
			new File(xsdFileBackUpLocation).mkdirs();
		}
		 Workbook wb = new HSSFWorkbook();
		    Sheet sheet = wb.createSheet(sheetName);
		    int rowNumber=0;
		    for(XmlSchemaVo rowData:listRowDatas){
		    	 Row row = sheet.createRow(rowNumber);
				    row.createCell(0).setCellValue(rowData.getInclude());
				    row.createCell(1).setCellValue(rowData.getXpath());
				    row.createCell(2).setCellValue(rowData.getMinOccurs());
				    row.createCell(3).setCellValue(rowData.getMaxOccurs());
				    row.createCell(4).setCellValue(rowData.getXsdType());
				    row.createCell(5).setCellValue(rowData.getEnums());
				    row.createCell(6).setCellValue(rowData.getJsonType());
				    row.createCell(7).setCellValue(rowData.getJsonFormat());
				    row.createCell(8).setCellValue(rowData.getMinLength());
				    row.createCell(9).setCellValue(rowData.getMaxLength());
				    row.createCell(10).setCellValue(rowData.getLength());
				    row.createCell(11).setCellValue(rowData.getPattern());
				    row.createCell(12).setCellValue(rowData.getDocumentation());
				    rowNumber++;
		    }
		    // Write the output to a file
		    File file=new File(xsdFileBackUpLocation+"/workbook.xls");
		    FileOutputStream fileOut = new FileOutputStream(file);
		    wb.write(fileOut);
		    fileOut.close();
			JSONObject obj=null;
			try {
				obj = jfrogUtilImpl.uploadFiles(file.getAbsolutePath(), applicationProperties.getSwaggerXpath(),
						applicationProperties.getJfrogHost()+":"+applicationProperties.getJfrogPort()+"/artifactory/",
						timeStamp+"", //change the repo name and path 
						applicationProperties.getJfrogUserName(), applicationProperties.getJfrogPassword());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
return obj;
		
	}
	
	public static String getCellValue(Cell cell, int rowNum)
			throws EmptyXlsRows {
		String s = "";
		
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s = cell.getStringCellValue();
		} else {
			throw new EmptyXlsRows(rowNum);
		}
		return s;
	}
	
	public static String getCellValue(Cell cell)
			throws EmptyXlsRows {
		String s = "";
		
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s = cell.getStringCellValue();
		} 
		return s;
	}
	
	public static List<String> getEnumCellValue(Cell cell)
			throws EmptyXlsRows {
		List<String> list=new ArrayList<String>();
		if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
		String s1 = cell.getStringCellValue();
			String ar[]=s1.split(Pattern.quote("|"));
			for(String a:ar){
				if(a.length()>0){
				list.add(a);
				}
			}
		} 
		return list;
	}

}
