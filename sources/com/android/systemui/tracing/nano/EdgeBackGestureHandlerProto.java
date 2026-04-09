package com.android.systemui.tracing.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

/* loaded from: classes.dex */
public final class EdgeBackGestureHandlerProto extends MessageNano {
    public boolean allowGesture;

    public EdgeBackGestureHandlerProto() {
        clear();
    }

    public EdgeBackGestureHandlerProto clear() {
        this.allowGesture = false;
        this.cachedSize = -1;
        return this;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        boolean z = this.allowGesture;
        if (z) {
            codedOutputByteBufferNano.writeBool(1, z);
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    @Override // com.google.protobuf.nano.MessageNano
    protected int computeSerializedSize() {
        int iComputeSerializedSize = super.computeSerializedSize();
        boolean z = this.allowGesture;
        return z ? iComputeSerializedSize + CodedOutputByteBufferNano.computeBoolSize(1, z) : iComputeSerializedSize;
    }
}
