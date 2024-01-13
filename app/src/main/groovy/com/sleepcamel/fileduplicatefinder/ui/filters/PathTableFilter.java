package com.sleepcamel.fileduplicatefinder.ui.filters;

import java.util.Arrays;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper;
import com.sleepcamel.fileduplicatefinder.ui.components.MyCheckboxTableViewer;

public class PathTableFilter extends ViewerFilter {

	String lastPath;
	private static String ALL_FILTER = "All";
	
	PathTableFilter(String initialPath){
		this.lastPath = initialPath;
	}
	
	@Override
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		Object[] out =  super.filter(viewer, parent, elements);
		((MyCheckboxTableViewer)viewer).notifyElementsFiltered(Arrays.asList(out));
		return out;
	}
	
	public boolean filterUsingPath(String newPath){
		if ( newPath.equals(lastPath) ){
			return false;
		}
		lastPath = newPath;
		return true;
	}
	
	@Override
	public boolean select(Viewer paramViewer, Object parent, Object element) {
		if ( ALL_FILTER.equals(lastPath) )
			return true;

		return ((FileWrapper) element).getParentWrapper().getFriendlyPath().equals(lastPath);
	}
	
	public String getLastPath() {
		return lastPath;
	}

}
