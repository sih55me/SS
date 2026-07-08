package cakar.search.filetype

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Project(
    val id: Int,
    val title: String,
    @SerializedName("description") val desc: String,
    val instructions: String,
    val creator: String,
    @SerializedName("image") val thumb: String
): Parcelable{


    var history : Sejarah = Sejarah("","","")

    var remix  = Remix("","")

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
        p0.writeString(desc)
        p0.writeString(instructions)
        p0.writeString(creator)
        p0.writeString(thumb)
        p0.writeMap(uninfo)
    }


    companion object CREATOR: Parcelable.Creator<Project>{
        override fun createFromParcel(p0: Parcel?): Project? {
            if(p0 ==null)return null
            return Project(p0)
        }

        override fun newArray(p0: Int): Array<out Project?> {
            return arrayOfNulls(p0)
        }

    }


    data class Sejarah(
        val created:String,
        val modified:String,
        val shared:String
    )


    data class Remix(
        val parent:String,
        val root: String
    )

    /**
     * 1. created
     * 2. modified
     * 3. shared
     * 4. views
     * 5. loves
     * 6. fav
     * 7. remix
     * 8. is author from scratch team?
     * 8. Root remix (project remix -> project)
     * 9. Parent remix (project remix1 -> project remix -> project)
     */
    val uninfo = mutableMapOf<String, Any>()

}
