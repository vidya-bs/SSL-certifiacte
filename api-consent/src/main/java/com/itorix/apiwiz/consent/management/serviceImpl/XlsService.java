package com.itorix.apiwiz.consent.management.serviceImpl;

import com.amazonaws.regions.Regions;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.consent.management.model.Consent;
import com.itorix.apiwiz.consent.management.model.ConsentAuditExportResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class XlsService {

    @Autowired
    JfrogUtilImpl jfrogUtilImpl;

    @Autowired
    private S3Connection s3Connection;

    @Autowired
    private S3Utils s3Utils;

    @Autowired
    ApplicationProperties applicationProperties;

    @SneakyThrows
    public ConsentAuditExportResponse createConsentAuditXsl(String sheetName, List<Consent> data, List<String> columnNames) throws IOException {
        long timeStamp = System.currentTimeMillis();
        String xlsFileBackUpLocation = applicationProperties.getBackupDir() + timeStamp;
        if (!new File(xlsFileBackUpLocation).exists()) {
            new File(xlsFileBackUpLocation).mkdirs();
        }

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);

        Row headerRow = sheet.createRow(0);

        Cell cell = headerRow.createCell(0);
        cell.setCellValue("CONSENT ID");

        for (int i= 0; i < columnNames.size(); i++) {
            Cell headerRowCell = headerRow.createCell(headerRow.getLastCellNum());
            headerRowCell.setCellValue(columnNames.get(i).toUpperCase(Locale.ROOT));
        }

        Cell scopes = headerRow.createCell(headerRow.getLastCellNum());
        scopes.setCellValue("SCOPES");

        Cell cell1 = headerRow.createCell(headerRow.getLastCellNum());
        cell1.setCellValue("CREATED");

        Cell cell2 = headerRow.createCell(headerRow.getLastCellNum());
        cell2.setCellValue("MODIFIED");

        Cell cell3 = headerRow.createCell(headerRow.getLastCellNum());
        cell3.setCellValue("EXPIRED");


        int rowNum = 1;
        for (Consent consent: data ) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(consent.getId());
            for (int i= 0; i < columnNames.size(); i++) {
                row.createCell(row.getLastCellNum()).setCellValue(consent.getConsent().getOrDefault(columnNames.get(i), ""));
            }

            row.createCell(row.getLastCellNum()).setCellValue(String.join(",", consent.getScopes()));
            row.createCell(row.getLastCellNum()).setCellValue(Instant.ofEpochMilli(consent.getCts()).toString());
            row.createCell(row.getLastCellNum()).setCellValue(Instant.ofEpochMilli(consent.getMts()).toString());
            row.createCell(row.getLastCellNum()).setCellValue(Instant.ofEpochMilli(consent.getExpiry()).toString());
        }

        File file = new File(xlsFileBackUpLocation + "/consent-audit.xls");
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();

        ConsentAuditExportResponse response = new ConsentAuditExportResponse();

        try {
            S3Integration s3Integration = s3Connection.getS3Integration();
            String downloadURI = null;
            if (null != s3Integration) {
                downloadURI = s3Utils.uplaodFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
                        Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(),
                        "temp/" + timeStamp + "/consent-audit.xls", xlsFileBackUpLocation + "/consent-audit.xls");
                response.setFileName("consent-audit.xls");
                response.setDownloadURI(downloadURI);
            } else {
                JSONObject jsonObject = jfrogUtilImpl.uploadFiles(file.getAbsolutePath(), applicationProperties.getSwaggerXpath(),
                        applicationProperties.getJfrogHost() + ":" + applicationProperties.getJfrogPort()
                                + "/artifactory/",
                        timeStamp + "", applicationProperties.getJfrogUserName(), applicationProperties.getJfrogPassword());
                response.setFileName(jsonObject.getString("filename"));
                response.setDownloadURI(jsonObject.getString("downloadURI"));
                response.setMd5(jsonObject.getString("md5"));
                response.setSha1(jsonObject.getString("sha1"));
            }
        } catch (Exception e) {
            log.error("Error while uploading consent data ", e.getMessage());
            throw new ItorixException(ErrorCodes.errorMessage.get("General-1000"), "General-1000");
        }
        return response;
    }



}
