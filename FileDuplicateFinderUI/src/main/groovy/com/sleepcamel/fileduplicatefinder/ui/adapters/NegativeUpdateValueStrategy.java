package com.sleepcamel.fileduplicatefinder.ui.adapters;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;

public class NegativeUpdateValueStrategy extends UpdateValueStrategy {
	public NegativeUpdateValueStrategy(){
		setConverter(new IConverter() {
			public Object convert(Object paramObject) {
				return !((Boolean)paramObject);
			}

			public Object getFromType() {return Boolean.class;}

			public Object getToType() {return Boolean.class;}
		});
	}
}
