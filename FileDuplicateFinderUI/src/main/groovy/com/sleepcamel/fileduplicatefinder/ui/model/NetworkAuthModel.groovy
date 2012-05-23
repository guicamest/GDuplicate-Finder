package com.sleepcamel.fileduplicatefinder.ui.model

import com.sleepcamel.fileduplicatefinder.core.domain.vfs.NetworkAuth

public class NetworkAuthModel implements Serializable{

	static final long serialVersionUID = -6618469841127325815L;

	NetworkAuth auth
	
	boolean isMounted = false

	public int hashCode() {
		final int prime = 31
		int result = 1
		prime * result + ((auth == null) ? 0 : auth.hashCode())
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false
		if (getClass() != obj.getClass())
			return false
		NetworkAuthModel other = (NetworkAuthModel) obj
		if (auth == null) {
			if (other.auth != null)
				return false
		} else if (!auth.equals(other.auth))
			return false
		true
	}

	public String toString() {
		auth?.toString()
	}
}
