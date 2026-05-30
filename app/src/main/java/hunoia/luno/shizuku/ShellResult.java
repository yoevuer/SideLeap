package hunoia.luno.shizuku;

import android.os.Parcel;
import android.os.Parcelable;

public class ShellResult implements Parcelable {
    public final int exitCode;
    public final String stdout;
    public final String stderr;
    public final boolean timedOut;
    public final String errorMessage;

    public ShellResult() {
        this(-1, "", "", false, "");
    }

    public ShellResult(int exitCode, String stdout, String stderr, boolean timedOut, String errorMessage) {
        this.exitCode = exitCode;
        this.stdout = stdout != null ? stdout : "";
        this.stderr = stderr != null ? stderr : "";
        this.timedOut = timedOut;
        this.errorMessage = errorMessage != null ? errorMessage : "";
    }

    public boolean isSuccess() {
        return !timedOut && errorMessage.isEmpty() && exitCode == 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(exitCode);
        dest.writeString(stdout);
        dest.writeString(stderr);
        dest.writeByte((byte) (timedOut ? 1 : 0));
        dest.writeString(errorMessage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShellResult> CREATOR = new Creator<ShellResult>() {
        @Override
        public ShellResult createFromParcel(Parcel source) {
            return new ShellResult(
                source.readInt(),
                source.readString(),
                source.readString(),
                source.readByte() != 0,
                source.readString()
            );
        }

        @Override
        public ShellResult[] newArray(int size) {
            return new ShellResult[size];
        }
    };
}
