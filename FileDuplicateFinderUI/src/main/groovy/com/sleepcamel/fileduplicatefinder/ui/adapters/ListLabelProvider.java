package com.sleepcamel.fileduplicatefinder.ui.adapters;

import java.util.Map;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public class ListLabelProvider extends BaseLabelProvider implements ILabelProvider{

	public Image getImage(Object paramObject) {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public String getText(Object paramObject) {
		Map map = (Map) paramObject;
		return map.get("filePath") + " ( " + map.get("count") + " )";
	}
	
}
