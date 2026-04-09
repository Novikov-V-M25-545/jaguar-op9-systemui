package kotlin.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: IOStreams.kt */
/* loaded from: classes.dex */
public final class ByteStreamsKt {
    public static final long copyTo(@NotNull InputStream copyTo, @NotNull OutputStream out, int i) throws IOException {
        Intrinsics.checkParameterIsNotNull(copyTo, "$this$copyTo");
        Intrinsics.checkParameterIsNotNull(out, "out");
        byte[] bArr = new byte[i];
        int i2 = copyTo.read(bArr);
        long j = 0;
        while (i2 >= 0) {
            out.write(bArr, 0, i2);
            j += i2;
            i2 = copyTo.read(bArr);
        }
        return j;
    }
}
