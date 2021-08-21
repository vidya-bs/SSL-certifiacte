package com.itorix.apiwiz.devportal.business.diff.compare;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapKeyDiff<K, V> {

	private Map<K, V> increased;
	private Map<K, V> missing;
	private List<K> sharedKey;

	private MapKeyDiff() {
		this.sharedKey = new ArrayList<K>();
	}

	public static <K, V> MapKeyDiff<K, V> diff(Map<K, V> mapLeft, Map<K, V> mapRight) {
		MapKeyDiff<K, V> instance = new MapKeyDiff<K, V>();
		if (null == mapLeft && null == mapRight)
			return instance;
		if (null == mapLeft) {
			instance.increased = mapRight;
			return instance;
		}
		if (null == mapRight) {
			instance.missing = mapLeft;
			return instance;
		}
		instance.increased = new LinkedHashMap<K, V>(mapRight);
		instance.missing = new LinkedHashMap<K, V>();
		for (Entry<K, V> entry : mapLeft.entrySet()) {
			K leftKey = entry.getKey();
			V leftValue = entry.getValue();
			if (mapRight.containsKey(leftKey)) {
				instance.increased.remove(leftKey);
				instance.sharedKey.add(leftKey);

			} else {
				instance.missing.put(leftKey, leftValue);
			}
		}
		return instance;
	}

	public Map<K, V> getIncreased() {
		return increased;
	}

	public Map<K, V> getMissing() {
		return missing;
	}

	public List<K> getSharedKey() {
		return sharedKey;
	}
}
