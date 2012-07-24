package com.sleepcamel.fileduplicatefinder.ui.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class I18nControl extends ResourceBundle.Control {

	public List<String> getFormats(String baseName) {
        if (baseName == null) {
            throw new NullPointerException();
        }
        return Arrays.asList(new String[]{"java.properties"});
    }
	
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale,
			String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		
		String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");
        Reader r = null;
        try{
        	r = new InputStreamReader(ClassLoader.getSystemResourceAsStream(resourceName), Charset.forName("utf-8"));
        }catch(Exception e){System.out.println(e);}
		return new PropertyResourceBundle(r);
	}
}
