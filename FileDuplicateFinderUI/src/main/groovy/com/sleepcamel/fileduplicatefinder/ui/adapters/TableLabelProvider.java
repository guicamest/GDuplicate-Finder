package com.sleepcamel.fileduplicatefinder.ui.adapters;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper;
import com.sleepcamel.fileduplicatefinder.ui.utils.Utils;

import java.util.Date;

public class TableLabelProvider extends ObservableMapLabelProvider implements ITableColorProvider{

	private IObservableList source;
	private Color lastColor;
	private static final Color ODD_COLOR = new Color(Display.getCurrent(), 255, 255, 255);
	private static final Color EVEN_COLOR = new Color(Display.getCurrent(), 224, 224, 224);

	public TableLabelProvider(IObservableMap[] attributes, IObservableList source) {
		super(attributes);
		this.source = source;
	}

	@Override
	public Color getForeground(Object paramObject, int paramInt) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String columnText = super.getColumnText(element, columnIndex);
		switch (columnIndex) {
			case 2:
				columnText = Utils.formatBytes(Long.parseLong(columnText));
				break;
			case 3:
				columnText = new Date(Long.parseLong(columnText)).toString();
				break;
		}
		return columnText;
	}

	@Override
	public Color getBackground(Object paramObject, int paramInt) {
		if ( paramInt != 0 ){
			return lastColor;
		}
			
		int elementIdx = source.indexOf(paramObject);
		Color color = lastColor;
		if ( elementIdx == 0 ){
			color = EVEN_COLOR;
		}else{
			FileWrapper object = (FileWrapper) paramObject;
			FileWrapper lastObject = (FileWrapper) source.get(elementIdx-1);
			if ( ! object.getMd5().equals(lastObject.getMd5()) ){
				color = ( lastColor == ODD_COLOR ) ? EVEN_COLOR : ODD_COLOR;
			}
		}
		lastColor = color;
		return lastColor;
	}

}
