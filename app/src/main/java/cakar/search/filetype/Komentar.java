package cakar.search.filetype;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import kotlin.Pair;

public class Komentar implements Parcelable {

    Object id;
     String usr;

    String usrThumb;
    String komentar;

    /**
     * first : created
     * second : edited
     */
    Pair<String,String> dates;


    public Object getId() {
        return id;
    }

    public String getUsr() {
        return usr;
    }

    public String getKomentar() {
        return komentar;
    }

    public Pair<String, String> getDates() {
        return dates;
    }


    public Komentar(Object id, String usr, String usrThumb, String komentar, Pair<String, String> dates) {
        this.id = id;
        this.usr = usr;
        this.usrThumb = usrThumb;
        this.komentar = komentar;
        this.dates = dates;
    }

    protected Komentar(Parcel in) {
        usr = in.readString();
        komentar = in.readString();
        dates = new Pair<>(in.readString(), in.readString());
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
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(usr);
        dest.writeString(komentar);
        dest.writeString(dates.getFirst());
        dest.writeString(dates.getSecond());
    }
}
