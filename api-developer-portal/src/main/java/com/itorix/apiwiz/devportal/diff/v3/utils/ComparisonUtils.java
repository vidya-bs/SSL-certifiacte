package com.itorix.apiwiz.devportal.diff.v3.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ComparisonUtils {

	public static Map<String, Collection<String>> findDiff(Collection<String> oldList, Collection<String> newList) {

		if (isNull(oldList, newList)) {
			return new LinkedHashMap<String, Collection<String>>();
		}

		Collection<String> removed = oldList.stream().filter(aObject -> {
			return !newList.contains(aObject);
		}).collect(Collectors.toList());

		Collection<String> added = newList.stream().filter(aObject -> !oldList.contains(aObject))
				.collect(Collectors.toList());

		HashMap<String, Collection<String>> tagDiff = new HashMap<>();
		tagDiff.put("removed", removed);
		tagDiff.put("added", added);

		return tagDiff;
	}

	public static Boolean isDiff(String s1, String s2) {
		if (s1 == s2)
			return false;

		if (Objects.nonNull(s1) && Objects.isNull(s2))
			return true;
		if (Objects.nonNull(s2) && Objects.isNull(s1))
			return true;

		return !s1.equals(s2);
	}

	public static Boolean isDiff(Object o1, Object o2) {

		if (isNull(o1, o2)) {
			return false;
		}

		o1 = o1.toString();
		o2 = o2.toString();

		if (o1 instanceof Boolean && o2 instanceof Boolean) {
			o1 = (Boolean) o1;
			o2 = (Boolean) o2;
		} else if (o1 instanceof BigDecimal && o2 instanceof BigDecimal) {
			o1 = (BigDecimal) o1;
			o2 = (BigDecimal) o2;
		} else if (o1 instanceof Integer && o2 instanceof Integer) {
			o1 = (Integer) o1;
			o2 = (Integer) o2;
		}

		if (o1 == o2)
			return false;

		if (Objects.nonNull(o1) && Objects.isNull(o2))
			return true;
		if (Objects.nonNull(o2) && Objects.isNull(o1))
			return true;

		return !(o1 == o2);
	}

	public static Boolean isNull(Object o1, Object o2) {
		return Objects.isNull(o1) && Objects.isNull(o2);
	}

}
