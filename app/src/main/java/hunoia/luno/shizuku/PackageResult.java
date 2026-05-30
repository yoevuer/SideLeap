package hunoia.luno.shizuku;

import android.os.Parcel;
import android.os.Parcelable;

public class PackageResult implements Parcelable {
    public final boolean success;
    public final String packageName;
    public final String errorMessage;

    public PackageResult(boolean success, String packageName, String errorMessage) {
        this.success = success;
        this.packageName = packageName != null ? packageName : "";
        this.errorMessage = errorMessage != null ? errorMessage : "";
    }

    public PackageResult(boolean success, String packageName) {
        this(success, packageName, "");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (success ? 1 : 0));
        dest.writeString(packageName);
        dest.writeString(errorMessage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PackageResult> CREATOR = new Creator<PackageResult>() {
        @Override
        public PackageResult createFromParcel(Parcel source) {
            return new PackageResult(
                source.readByte() != 0,
                source.readString(),
                source.readString()
            );
        }

        @Override
        public PackageResult[] newArray(int size) {
            return new PackageResult[size];
        }
    };
}
