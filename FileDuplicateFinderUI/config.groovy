// File: config.groovy
environments  {
	
	allLibs = 'libs'
	eclipseVersion = '3.7.2'
	swtLibs = "${allLibs}/${eclipseVersion}"

	windows32 {
		swtOsJars = "${swtLibs}/win_x86"
	}
	
	windows64 {
		swtOsJars = "${swtLibs}/win_x86_64"
	}
	
	linux32 {			
		swtOsJars = "${swtLibs}/linux_x86"
	}
	
	linux64 {
		swtOsJars = "${swtLibs}/linux_x86_64"
	}
	
	// Unsupported
	mac-carbon {
		swtOsJars = "${swtLibs}/mac_carbon"
	}
	
	maccocoa32 {
		swtOsJars = "${swtLibs}/mac_cocoa"
	}
	
	maccocoa64 {
		swtOsJars = "${swtLibs}/mac_cocoa_x86_64"
	}
}
