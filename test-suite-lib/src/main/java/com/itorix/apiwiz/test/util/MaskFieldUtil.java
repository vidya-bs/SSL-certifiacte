package com.itorix.apiwiz.test.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MaskFieldUtil {

	private final static Logger logger = LoggerFactory.getLogger(MaskFieldUtil.class);

	private final static String  regexPlaceHolder = "#FIELD#";

	private final static Map<String, String> templateMap = new HashMap<>();
	private final static List<String> jsonTemplateList = new ArrayList<>();


	static {
		templateMap.put("attribute", "<.*#FIELD#=\".*\".*>");
		templateMap.put("element", "<([a-zA-Z0-9]*:|)#FIELD#.*?>.*?<\\/([a-zA-Z0-9]*:|)#FIELD#>");
		jsonTemplateList.add("\"#FIELD#\":([).?(])");
		jsonTemplateList.add("\"#FIELD#\":[\"\\d].*?(\"|\\d)[,\\}]");

	}

	public static  String getMaskedValue(final String value) {
			char[] mask = new char[value.length()];
			Arrays.fill(mask, '*');
			return new String(mask);
		}

	public static String getMaskedResponseForJson(List<String> maskingFields, String payload) {

		if (maskingFields == null || maskingFields.isEmpty()) {
			return payload;
		}

		ObjectMapper mapper = new ObjectMapper();

		String maskedPayload = new String(payload);
		Map readValue = null;
		for (String maskingField : maskingFields) {
			for(String regexJsonTemplate : jsonTemplateList){
			String regexPattern = regexJsonTemplate.replaceAll(regexPlaceHolder, maskingField);

			final Matcher matcher = Pattern.compile(regexPattern).matcher(payload);
			final List<String> matches = new ArrayList<>();
			while (matcher.find()) {
				matches.add(matcher.group(0));
				String matchedValue = payload.substring(matcher.start(), matcher.end());
				String[] splitKeyValue = matchedValue.split(":");

				if (splitKeyValue[1].startsWith("{")) {
					continue;
				}
				if (splitKeyValue[1].startsWith("[")) {
					try {
						StringBuilder maskedValueBuilder = new StringBuilder(splitKeyValue[0] + ":" + "[");
						if (splitKeyValue[1].endsWith(",")) {
							String valueToMask = matchedValue.substring(0, matchedValue.length() - 1);
							readValue = mapper.readValue("{" + valueToMask + "}", Map.class);

							List<String> valueList = (List<String>) readValue
									.get(splitKeyValue[0].replaceAll("\"", ""));
							valueList.stream().forEach(s -> {
								maskedValueBuilder.append("\"" + getMaskedValue(s) + "\",");
							});
							maskedValueBuilder.deleteCharAt(maskedValueBuilder.length() - 1).append("]").append(",");
							maskedPayload = maskedPayload.replace(matchedValue, maskedValueBuilder.toString());

						} else {

							readValue = mapper.readValue("{" + matchedValue + "}", Map.class);
							List<String> v = (List<String>) readValue.get(splitKeyValue[0].replaceAll("\"", ""));
							v.stream().forEach(s -> {
								maskedValueBuilder.append("\"" + getMaskedValue(s) + "\",");
							});
							maskedValueBuilder.deleteCharAt(maskedValueBuilder.length() - 1).append("]");
							maskedPayload = maskedPayload.replace(matchedValue, maskedValueBuilder.toString());
						}

					} catch (IOException e) {
						logger.error("Error during masking json",e);
					}

				} else {
					if (splitKeyValue[1].startsWith("\"")) {
						StringBuilder maskedValueBuilder = new StringBuilder(splitKeyValue[0] + ":" + "\"");
						String valueToMask = null;

						if (splitKeyValue[1].endsWith(",")) {
							valueToMask = splitKeyValue[1].substring(1, splitKeyValue[1].length() - 2);
							maskedValueBuilder.append(getMaskedValue(valueToMask)).append("\"").append(",");
						} else {
							valueToMask = splitKeyValue[1].substring(1, splitKeyValue[1].length() - 1);
							maskedValueBuilder.append(getMaskedValue(valueToMask)).append("\"");
						}
						maskedPayload = maskedPayload.replace(matchedValue, maskedValueBuilder.toString());
					} else {
						StringBuilder maskedValueBuilder = new StringBuilder(splitKeyValue[0] + ":" + "\"");
						if (splitKeyValue[1].endsWith(",")) {
							String valueToMask = splitKeyValue[1].substring(0, splitKeyValue[1].length() - 1);
							maskedValueBuilder.append(getMaskedValue(valueToMask)).append("\"").append(",");
						} else {
							String valueToMask = splitKeyValue[1].substring(0, splitKeyValue[1].length());
							maskedValueBuilder.append(getMaskedValue(valueToMask)).append("\"");
						}
						maskedPayload = maskedPayload.replace(matchedValue, maskedValueBuilder.toString());
					}

				}
			}

		}
	}
		return maskedPayload.toString();

	}

	public static String getMaskedResponseForXml(List<String> maskingFields, String payload) {

		String maskedPayload = new String(payload);
		for (Entry<String, String> template : templateMap.entrySet()) {
			for (String maskingField : maskingFields) {
				String regexPattern = template.getValue().replace("#FIELD#", maskingField);
				final Matcher matcher = Pattern.compile(regexPattern).matcher(payload);
				final List<String> matches = new ArrayList<>();
				while (matcher.find()) {
					matches.add(matcher.group(0));
					String matchedValue = payload.substring(matcher.start(), matcher.end());
					if (template.getKey().equals("attribute")) {
						String[] splitElement = matchedValue.split(maskingField);
						Pattern p = Pattern.compile("\"([^\"]*)\"");
						Matcher m = p.matcher(splitElement[1]);
						while (m.find()) {
							String attributeValue = m.group(1);
							maskedPayload = maskedPayload.replace(matchedValue, splitElement[0] + maskingField
									+ splitElement[1].replaceFirst(attributeValue, getMaskedValue(attributeValue)));
						}

					} else {
						int start = matchedValue.indexOf(">") + 1;
						int end = matchedValue.indexOf("</");
						String elementValue = matchedValue.substring(start, end);
						StringBuilder maskedValue = new StringBuilder(matchedValue.substring(0, start));
						maskedValue.append(getMaskedValue(elementValue))
								.append(matchedValue.subSequence(end, matchedValue.length()));
						maskedPayload = maskedPayload.replace(matchedValue, maskedValue.toString());
					}
				}
			}
		}
		return maskedPayload.toString();
	}
}
