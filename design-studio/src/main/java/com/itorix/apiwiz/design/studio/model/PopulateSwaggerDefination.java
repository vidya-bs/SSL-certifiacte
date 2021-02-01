package com.itorix.apiwiz.design.studio.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.models.properties.PropertyBuilder.PropertyId;
import io.swagger.v3.oas.models.media.Schema;
import net.minidev.json.JSONArray;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.models.properties.RefProperty;

public class PopulateSwaggerDefination {
	private ObjectMapper mapper = new ObjectMapper();
	final static Logger logger = Logger
			.getLogger(PopulateSwaggerDefination.class);


	public Swagger populateDefinitons(List<RowData> rd, Swagger swagger) {
		Map<String, Model> definitions = swagger.getDefinitions();
		if (definitions == null) {
			definitions = new HashMap<String, Model>();
		}
		Map<String, Model> updagtedDefinitions = populateDefinitons(rd, definitions);
		swagger.setDefinitions(updagtedDefinitions);
		return swagger;
	}

	public OpenAPI populateDefinitons(List<RowData> rd, OpenAPI openAPI ) {
		Map<String, Model> definitions = null;
		if (definitions == null) {
			definitions = new HashMap<String, Model>();
		}
		Map<String, Model> updagtedDefinitions = populateDefinitons(rd, definitions);

		Components components = new Components();
		HashMap<String, Schema> schemas = convertDefinition(updagtedDefinitions);
		components.setSchemas(schemas);
		components.setExtensions(null);
		openAPI.setComponents(components);
		return openAPI;
	}

	@SuppressWarnings("rawtypes")
	private HashMap<String, Schema> convertDefinition(Map<String, Model> model){
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			HashMap<String, Schema> schemas3 = new HashMap<String, Schema>(); 
			Set<String> keys = model.keySet();
			for(String key: keys){
				Model model2 = model.get(key);
				String modelStr = mapper.writeValueAsString(model2);
				modelStr = modelStr.replaceAll("/definitions/", "/components/schemas/");
				Schema schema = mapper.readValue(modelStr, Schema.class);
				Map<String, Property> modelProps = model2.getProperties();
				for(String propKey : modelProps.keySet()){
					Property property = modelProps.get(propKey);
					String propStr = mapper.writeValueAsString(property);
					DocumentContext context;
					context = JsonPath.parse(propStr);
					String token = getAttributeValue(context, "$.items.$ref");
					if(token != ""){
						Map<String, Schema> schemaProperties = schema.getProperties();
						schemaProperties.get(propKey).set$ref(token.replaceAll("/definitions/", "/components/schemas/"));
						String modelPropStr=  mapper.writeValueAsString(modelProps).replaceAll("/definitions/", "/components/schemas/");
						//modelProps = mapper.readValue(modelStr, Map<String, Schema>.class);
						schema.setProperties(modelProps);
					}
				}
				schemas3.put(key, schema);
			}
			return schemas3;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, Model> populateDefinitons(List<RowData> rd, Map<String, Model> definitions) {
		for (RowData r : rd) {
			String xpath = r.getXpath();
			//System.out.println(xpath);
			String typ = r.getJsonType();
			String[] xpathArray = xpath.split("/");
			String lastBut = "";
			String root = null;
			String last = xpathArray[xpathArray.length - 1];

			if (xpathArray.length >= 2) {
				lastBut = xpathArray[xpathArray.length - 2];
			} else {
				lastBut = xpathArray[0];
				root = lastBut;
			}
			if (typ.equalsIgnoreCase("object")) {
				Model rootModel = null;
				if (root != null) {
					rootModel = definitions.get(root);
				}
				if (rootModel == null) {
					Model model = definitions.get(lastBut);
					if (model == null) {
						ModelImpl modelImpl = new ModelImpl();
						modelImpl.setType(ModelImpl.OBJECT);
						Model m=definitions.get(last);
						if(m==null){
							definitions.put(last, modelImpl);
							String des = r.getDocumentation();
							if (des != null && des.length() > 0) {
								modelImpl.setDescription(des);
							}
						}
					} else {
						if (model instanceof ModelImpl) {
							Map<String, Property> proMap = model
									.getProperties();
							if (proMap == null) {
								Map<String, Property> propertties = new HashMap<String, Property>();

								Property property = getModelProperty(r, last);
								propertties.put(last, property);
								model.setProperties(propertties);

							} else {

								Property property = getModelProperty(r, last);
								proMap.put(last, property);
								model.setProperties(proMap);
							}
							Model model1 = definitions.get(last);
							if (model1 == null) {
								ModelImpl modelImpl = new ModelImpl();
								modelImpl.setType(ModelImpl.OBJECT);
								definitions.put(last, modelImpl);
							}
						}
					}
				}
			} else {
				Model model = definitions.get(lastBut);
				if (model == null) {

				} else {
					Map<String, Property> proMap = model.getProperties();

					if (proMap == null) {
						Map<String, Property> propertties = new HashMap<String, Property>();
						Property property = getProperty(r,  lastBut, last);
						propertties.put(last, property);
						model.setProperties(propertties);

					} else {
						Property property = getProperty(r, lastBut, last);
						proMap.put(last, property);
						model.setProperties(proMap);
					}
				}
				definitions.put(lastBut, model);
			}
		}
		String strDef;
		try {
			strDef = new ObjectMapper().writeValueAsString(definitions);
			//System.out.println(strDef);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return definitions;
	}


	public static Property getProperty(RowData r, 
			String lastBut, String last) {
		Map<PropertyBuilder.PropertyId, Object> map = new HashMap<PropertyBuilder.PropertyId, Object>();
		/*String mmp = p1.getProperty(lastBut);
		if (mmp != null && mmp.length() > 0) {
			String[] a = mmp.split(Pattern.quote("|"));
			int c = 0;
			for (String s : a) {
				if (c == 0) {
					map.put(PropertyId.MIN_LENGTH, s);
				} else if (c == 1) {
					map.put(PropertyId.MAX_LENGTH, s);
				} else if (c == 2) {
					map.put(PropertyId.PATTERN, s);
				}
				c++;
			}

		}*/
		if(r.getMinLength()!=null && r.getMinLength().length()>0){
			map.put(PropertyId.MIN_LENGTH,Integer.parseInt(r.getMinLength()));
		}
		if(r.getMaxLength()!=null && r.getMaxLength().length()>0){
			map.put(PropertyId.MAX_LENGTH, Integer.parseInt(r.getMaxLength()));
		}
		if(r.getPattern()!=null && r.getPattern().length()>0){
			map.put(PropertyId.PATTERN, r.getPattern());
		}
		List<String> l = r.getEnumcell();
		if (l != null && l.size() > 0) {
			map.put(PropertyId.ENUM, l);
		}
		String des = r.getDocumentation();
		if (des != null && des.length() > 0) {
			map.put(PropertyId.DESCRIPTION, des);
		}
		if (r.getMax().equals("*")) {
			if (r.getMin().equalsIgnoreCase("1")) {
				map.put(PropertyId.MIN_ITEMS, 1);
			}

			map.put(PropertyId.TYPE, "array");
			ArrayProperty ar = (ArrayProperty) PropertyBuilder.build("array",
					null, map);
			Property property = PropertyBuilder.build(r.getJsonType(),
					(r.getJsonFormat() != null && r.getJsonFormat().trim()
					.length() > 0) ? r.getJsonFormat().trim() : null, map);
			ar.setItems(property);
			return ar;
		} else {
			Property property = PropertyBuilder.build(r.getJsonType(),
					(r.getJsonFormat() != null && r.getJsonFormat().trim()
					.length() > 0) ? r.getJsonFormat().trim() : null, map);
			if (r.getMin().equals("1") && r.getMax().equals("1")) {
				//System.out.println(last);
				property.setRequired(true);
			}
			return property;
		}

	}

	public static Property getModelProperty(RowData r, String last) {
		Map<PropertyBuilder.PropertyId, Object> map = new HashMap<PropertyBuilder.PropertyId, Object>();
		if (r.getMax().equals("*")) {

			String des = r.getDocumentation();
			if (des != null && des.length() > 0) {
				map.put(PropertyId.DESCRIPTION, des);
			}
			if (r.getMin().equalsIgnoreCase("1")) {
				map.put(PropertyId.MIN_ITEMS, 1);
				//map.put(PropertyId.MAX_ITEMS, 2147483647);
			}
			ArrayProperty ar = (ArrayProperty) PropertyBuilder.build("array",
					null, map);
			RefProperty refProperty = new RefProperty();
			refProperty.set$ref(last);
			ar.setItems(refProperty);
			return ar;
		} else {
			RefProperty property = (RefProperty) PropertyBuilder.build("ref",
					null, map);
			property.set$ref(last);
			return property;
		}
	}
	
	
	private static String getAttributeValue(DocumentContext context, String path) {
		String value = "";
		try {
			if (context.read(path) instanceof JSONArray) {
				JSONArray array = context.read(path);
				if (array.size() == 1)
					value = array.get(0).toString();
				else
					throw new Exception("Invalid JSON Path specified");
			} else
				value = context.read(path).toString();
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
		return value;
	}

}
