package com.sleepcamel.fileduplicatefinder.core.domain.smb

import jcifs.UniAddress
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbSession

import java.beans.PropertyChangeListener
import java.io.Serializable

//@Bindable
data class SmbAuth(val address:String, val folder:String,
                   val username:String = "", val password:String = "") : Serializable {

    val checkPathExists by lazy {
        SmbSession.logon(domain(), auth())
        folderFile().exists()
    }

    fun domain(): UniAddress {
        return UniAddress.getByName(address)
    }

    fun auth(): NtlmPasswordAuthentication {
        return NtlmPasswordAuthentication(address, username, password)
    }

    fun folderPath(): String {
        return "smb://$address/$folder/"
    }

    fun folderFile(): SmbFile {
        return SmbFile(folderPath(), auth())
    }

    override fun toString(): String {
        return username + " @ " + folderPath()
    }

    fun addPropertyChangeListener(listener: PropertyChangeListener) {//todo
    }

    fun addPropertyChangeListener(name: String, listener: PropertyChangeListener) {//todo
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener) {//todo
    }

    fun removePropertyChangeListener(name: String, listener: PropertyChangeListener) {//todo
    }

    fun firePropertyChange(name: String, oldValue: Any, newValue: Any) {//todo
    }

    //todo
    /*val propertyChangeListeners: Array<PropertyChangeListener>
        get() {}

    fun getPropertyChangeListeners(name: String): Array<PropertyChangeListener> {//todo
    }*/

}
