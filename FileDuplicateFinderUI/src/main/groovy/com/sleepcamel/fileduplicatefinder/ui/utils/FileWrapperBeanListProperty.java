package com.sleepcamel.fileduplicatefinder.ui.utils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
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
		String sour = source != null ? ((FileWrapper)source).getPath() : "root";
		System.out.println("------------- "+sour+"\n");
		RuntimeException runtimeException = new RuntimeException("");
		StackTraceElement[] stackTrace = runtimeException.getStackTrace();
		List<StackTraceElement> backup = new ArrayList<StackTraceElement>();
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement ste = stackTrace[i];
			if ( ste.getClassName().contains("fileduplicatefinder")){
				backup.add(ste);
			}
		}
		runtimeException.setStackTrace(backup.toArray(new StackTraceElement[]{}));
		System.out.println(Thread.currentThread().getThreadGroup()+Thread.currentThread().getName());
		runtimeException.printStackTrace();
		if ( source != null ){
			((FileWrapper)source).dirs();
		}
		return (List<?>) BeanPropertyHelper.readProperty(source, descriptor);
	}
}
