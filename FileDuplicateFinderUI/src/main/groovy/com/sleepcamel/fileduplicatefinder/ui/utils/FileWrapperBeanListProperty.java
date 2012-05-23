package com.sleepcamel.fileduplicatefinder.ui.utils;

import java.beans.PropertyDescriptor;
import java.util.List;

import org.eclipse.core.internal.databinding.beans.BeanListProperty;
import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper;

import com.sleepcamel.fileduplicatefinder.core.domain.FileWrapper;

public class FileWrapperBeanListProperty extends BeanListProperty {

	private PropertyDescriptor descriptor;

	@SuppressWarnings("rawtypes")
	public FileWrapperBeanListProperty(PropertyDescriptor propertyDescriptor, Class elementType) {
		super(propertyDescriptor, elementType);
		this.descriptor = propertyDescriptor;
	}

	@Override
	protected List<?> doGetList(Object source) {
		if ( source != null ){
			((FileWrapper)source).dirs();
		}
		return (List<?>) BeanPropertyHelper.readProperty(source, descriptor);
	}
}
