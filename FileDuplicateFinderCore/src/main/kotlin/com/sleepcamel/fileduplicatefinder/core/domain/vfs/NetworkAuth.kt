package com.sleepcamel.fileduplicatefinder.core.domain.vfs

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.sleepcamel.fileduplicatefinder.core.domain.fileadapter.FileObjectAdapter
import com.sleepcamel.fileduplicatefinder.core.domain.s3.S3ObjectWrapper
import org.apache.commons.vfs.FileObject

import java.beans.PropertyChangeListener
import java.io.Serializable

//@Bindable
data class NetworkAuth(val hostname:String = "", val username:String = "", val password:String = "",
                  val folder:String = "", val protocol:String = "SMB", val port:Int = 445) : Serializable {

    fun sharePath(): String {
        val sb = StringBuilder()
        var folderStr = folder

        val lowerProtocol = protocol.toLowerCase()
        sb.append("$lowerProtocol://")
        if (lowerProtocol != "file") {
            if ("" != username.trim { it <= ' ' }) {
                sb.append(username)
                if (password.isNotBlank()) {
                    sb.append(":" + password)
                }


                sb.append("@")
            }

            sb.append(this.hostname)
            if (port != -1) {
                sb.append(":" + port.toString())
            }

            if (!folderStr.startsWith("/")) {
                folderStr = "/$folderStr"
            }

        }

        return sb.append(folderStr).toString()
    }

    fun folderFile(): Pair<S3ObjectWrapper?, FileObject?> {
        if (protocol == "S3") {
            val s3 = AmazonS3Client(BasicAWSCredentials(username, password))
            var path = folder.trim { it <= ' ' }
            if (!path.isEmpty() && !path.endsWith("/")) {
                path = "$path/"
            }

            val wrapper = S3ObjectWrapper()

            wrapper.summary = S3ObjectSummary().apply{
                bucketName = hostname
                key = path
            }
            wrapper.s3 = s3
            return wrapper to null
        }
        return null to FileObjectAdapter.resolveFileObject(sharePath())
    }

    override fun toString(): String {
        return "$username @ $hostname - $folder"
    }

    fun checkPathExists(): Boolean {
        val folderFile = folderFile()
        return folderFile.component1()?.exists() ?: (folderFile.component2()?.exists() ?: false)
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
