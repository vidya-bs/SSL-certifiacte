package com.itorix.apiwiz.design.studio.businessimpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.generator.exception.ApiException;
import io.swagger.generator.util.ZipUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;

public class Swagger3SDK {
	 
	public static String generate(String language, String openapi, String output) throws ApiException, FileNotFoundException, IOException {
		CodegenConfigurator configurator = new CodegenConfigurator();
		configurator.setOutputDir(output);
		configurator.setLang(language);
		configurator.setInputSpecURL(openapi);
		final ClientOptInput clientOptInput = configurator.toClientOptInput();
		List<File>  files = new DefaultGenerator().opts(clientOptInput).generate();
        if (files.size() > 0) {
        	String outputFilename = output + "-bundle.zip";
            List<File> filesToAdd = new ArrayList<File>();
            filesToAdd.add(new File(output));
            ZipUtil zip = new ZipUtil();
            zip.compressFiles(filesToAdd, outputFilename);
            FileUtils.deleteDirectory(new File(output));
            return outputFilename;
        } 
        return null;
	}

	public static OpenAPI loadSwagger(String swagger){
		ParseOptions options = new ParseOptions();
		options.setResolve(true);
		options.setResolveCombinators(false);
		options.setResolveFully(true);
		return new OpenAPIV3Parser().readContents(swagger, null, options).getOpenAPI();
	}
	
}
