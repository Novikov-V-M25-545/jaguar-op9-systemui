package com.android.systemui.tracing.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

/* loaded from: classes.dex */
public final class SystemUiTraceEntryProto extends MessageNano {
    private static volatile SystemUiTraceEntryProto[] _emptyArray;
    public long elapsedRealtimeNanos;
    public SystemUiTraceProto systemUi;

    public static SystemUiTraceEntryProto[] emptyArray() {
        if (_emptyArray == null) {
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null) {
                    _emptyArray = new SystemUiTraceEntryProto[0];
                }
            }
        }
        return _emptyArray;
    }

    public SystemUiTraceEntryProto() {
        clear();
    }

    public SystemUiTraceEntryProto clear() {
        this.elapsedRealtimeNanos = 0L;
        this.systemUi = null;
        this.cachedSize = -1;
        return this;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        long j = this.elapsedRealtimeNanos;
        if (j != 0) {
            codedOutputByteBufferNano.writeFixed64(1, j);
        }
        SystemUiTraceProto systemUiTraceProto = this.systemUi;
        if (systemUiTraceProto != null) {
            codedOutputByteBufferNano.writeMessage(3, systemUiTraceProto);
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    @Override // com.google.protobuf.nano.MessageNano
    protected int computeSerializedSize() {
        int iComputeSerializedSize = super.computeSerializedSize();
        long j = this.elapsedRealtimeNanos;
        if (j != 0) {
            iComputeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(1, j);
        }
        SystemUiTraceProto systemUiTraceProto = this.systemUi;
        return systemUiTraceProto != null ? iComputeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(3, systemUiTraceProto) : iComputeSerializedSize;
    }
}
