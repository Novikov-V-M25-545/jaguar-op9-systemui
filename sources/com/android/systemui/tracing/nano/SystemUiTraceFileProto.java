package com.android.systemui.tracing.nano;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

/* loaded from: classes.dex */
public final class SystemUiTraceFileProto extends MessageNano {
    public SystemUiTraceEntryProto[] entry;
    public long magicNumber;

    public SystemUiTraceFileProto() {
        clear();
    }

    public SystemUiTraceFileProto clear() {
        this.magicNumber = 0L;
        this.entry = SystemUiTraceEntryProto.emptyArray();
        this.cachedSize = -1;
        return this;
    }

    @Override // com.google.protobuf.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
        long j = this.magicNumber;
        if (j != 0) {
            codedOutputByteBufferNano.writeFixed64(1, j);
        }
        SystemUiTraceEntryProto[] systemUiTraceEntryProtoArr = this.entry;
        if (systemUiTraceEntryProtoArr != null && systemUiTraceEntryProtoArr.length > 0) {
            int i = 0;
            while (true) {
                SystemUiTraceEntryProto[] systemUiTraceEntryProtoArr2 = this.entry;
                if (i >= systemUiTraceEntryProtoArr2.length) {
                    break;
                }
                SystemUiTraceEntryProto systemUiTraceEntryProto = systemUiTraceEntryProtoArr2[i];
                if (systemUiTraceEntryProto != null) {
                    codedOutputByteBufferNano.writeMessage(2, systemUiTraceEntryProto);
                }
                i++;
            }
        }
        super.writeTo(codedOutputByteBufferNano);
    }

    @Override // com.google.protobuf.nano.MessageNano
    protected int computeSerializedSize() {
        int iComputeSerializedSize = super.computeSerializedSize();
        long j = this.magicNumber;
        if (j != 0) {
            iComputeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(1, j);
        }
        SystemUiTraceEntryProto[] systemUiTraceEntryProtoArr = this.entry;
        if (systemUiTraceEntryProtoArr != null && systemUiTraceEntryProtoArr.length > 0) {
            int i = 0;
            while (true) {
                SystemUiTraceEntryProto[] systemUiTraceEntryProtoArr2 = this.entry;
                if (i >= systemUiTraceEntryProtoArr2.length) {
                    break;
                }
                SystemUiTraceEntryProto systemUiTraceEntryProto = systemUiTraceEntryProtoArr2[i];
                if (systemUiTraceEntryProto != null) {
                    iComputeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, systemUiTraceEntryProto);
                }
                i++;
            }
        }
        return iComputeSerializedSize;
    }
}
