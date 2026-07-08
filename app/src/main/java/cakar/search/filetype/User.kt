package cakar.search.filetype

import android.os.Parcel
import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import org.json.JSONObject

data class User(
    val id: Int,
    val title: String,
    val bio: String,
    val status: String,
    val thumb: String,
    val from: String
): Parcelable{

    constructor(p:Parcel) : this(
        p.readInt(),
        p.readString().toString(),
        p.readString().toString(),
        p.readString().toString(),
        p.readString().toString(),
        p.readString().toString()
    ) {

        p.readMap(uninfo, MutableMap::class.java.classLoader)


    }
    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(id)
        p0.writeString(title)
        p0.writeString(bio)
        p0.writeString(status)
        p0.writeString(thumb)
        p0.writeString(from)
        p0.writeMap(uninfo)
    }


    companion object CREATOR: Parcelable.Creator<User>{
        override fun createFromParcel(p0: Parcel?): User? {
            if(p0 ==null)return null
            return User(p0)
        }

        override fun newArray(p0: Int): Array<out User?> {
            return arrayOfNulls(p0)
        }

    }

    /**
     * 1. created
     * 2. is author from scratch team?
     */
    val uninfo = mutableMapOf<String,String>()


    data class AsActive(
        var id: Int,
        @SerializedName("username") var title: String,
        var token: String,
        var thumb: String,
    ){

        constructor(j: JSONObject):this(0,"","",""){
            try{
                id = j.getInt("id")
                title = j.getString("username")
                token = j.getString("token")
            }catch (_: Exception){

            }
        }
    }

}
