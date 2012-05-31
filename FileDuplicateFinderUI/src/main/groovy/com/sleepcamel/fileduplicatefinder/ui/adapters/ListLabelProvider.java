package com.sleepcamel.fileduplicatefinder.ui.adapters;

import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;

public class ListLabelProvider extends LabelProvider {

	private static String FOLDER_DUPLICATE_COUNT_FORMAT = "%s ( %d )";
	private static String FOLDER_NAME_PROPERTY = "filePath";
	private static String FOLDER_DUPLICATE_COUNT_PROPERTY = "count";
	
	@SuppressWarnings("rawtypes")
	public String getText(Object paramObject) {
		Map map = (Map) paramObject;
		return String.format(FOLDER_DUPLICATE_COUNT_FORMAT, map.get(FOLDER_NAME_PROPERTY), map.get(FOLDER_DUPLICATE_COUNT_PROPERTY));
	}
	
}
