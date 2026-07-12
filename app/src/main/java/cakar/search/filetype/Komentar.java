package cakar.search.filetype;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import kotlin.Pair;

public class Komentar implements Parcelable {

    int id;
    String usr;

    String usrThumb;
    String komentar;

    /**
     * first : created
     * second : edited
     */
    Pair<String,String> dates;


    public int getId() {
        return id;
    }

    public String getUsr() {
        return usr;
    }

    public String getKomentar() {
        return komentar;
    }
    public String getUsrThumb() {
        return usrThumb;
    }

    public Pair<String, String> getDates() {
        return dates;
    }


    public Komentar(int id, String usr, String usrThumb, String komentar, Pair<String, String> dates) {
        this.id = id;
        this.usr = usr;
        this.usrThumb = usrThumb;
        this.komentar = komentar;
        this.dates = dates;
    }

    protected Komentar(Parcel in) {
        id = in.readInt();
        usr = in.readString();
        komentar = in.readString();
        dates = new Pair<>(in.readString(), in.readString());
        try {
            origin = new JSONObject(in.readString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final Creator<Komentar> CREATOR = new Creator<Komentar>() {
        @Override
        public Komentar createFromParcel(Parcel in) {
            return new Komentar(in);
        }

        @Override
        public Komentar[] newArray(int size) {
            return new Komentar[size];
        }
    };

    @Override
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(usr);
        dest.writeString(komentar);
        dest.writeString(dates.getFirst());
        dest.writeString(dates.getSecond());
        dest.writeString(origin.toString());
    }


    public JSONObject origin = new JSONObject();
}
