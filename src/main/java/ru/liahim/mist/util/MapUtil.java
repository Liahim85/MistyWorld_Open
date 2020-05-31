package ru.liahim.mist.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtil {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean reverse) {
	    return map.entrySet()
	              .stream()
	              .sorted(reverse ? Map.Entry.comparingByValue(Collections.reverseOrder()) : Map.Entry.comparingByValue())
	              .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (e1, e2) -> e1, 
	                LinkedHashMap::new
	              ));
	}
}