package com.itorix.apiwiz.test.executor;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TestRegex {

	public static void main(String args[])
			throws JsonParseException, JsonMappingException, IOException, PatternSyntaxException {
		final String regex = "(capture";
		final String string = "200";
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(regex, Pattern.MULTILINE);
		} catch (PatternSyntaxException ex) {
			System.err.println(ex.getDescription());
			System.exit(1);
		}

		final Matcher matcher = pattern.matcher(string);

		while (matcher.find()) {
			System.out.println("Full match: " + matcher.group(0));
			for (int i = 1; i <= matcher.groupCount(); i++) {
				System.out.println("Group " + i + ": " + matcher.group(i));
			}
		}

		boolean flag = Pattern.matches(regex, string);
		System.out.println(flag);

	}

}
