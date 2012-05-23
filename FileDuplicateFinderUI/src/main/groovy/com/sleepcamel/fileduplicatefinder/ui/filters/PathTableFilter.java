package com.sleepcamel.fileduplicatefinder.ui.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper;

public class PathTableFilter extends ViewerFilter {

	String lastPath;
	
	PathTableFilter(String initialPath){
		this.lastPath = initialPath;
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
		if ( lastPath.equals("All") )
			return true;

		return ((FileWrapper) element).getParentWrapper().getFriendlyPath().equals(lastPath);
	}

}
