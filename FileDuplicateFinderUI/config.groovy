// File: config.groovy
environments  {
	
	windows32 {
		swtLibs = 'libs'
		swtOsJars = "${swtLibs}/win_x86"
	}
	
	windows64 {
		swtLibs = 'libs'
		swtOsJars = "${swtLibs}/win_x86_64"
	}
	
	linux32 {			
		swtLibs = 'libs'
		swtOsJars = "${swtLibs}/linux_x86"
	}
	
	linux64 {
		swtLibs = 'libs'
		swtOsJars = "${swtLibs}/linux_x86_64"
	}
	
	// Oldest one
	mac-carbon {
		swtLibs = 'libs'
		swtOsJars = "${swtLibs}/mac_carbon"
	}
	
	// Newest one
	mac-cocoa {
		swtLibs = 'libs'
		swtOsJars = "${swtLibs}/mac_cocoa"	
	}
}