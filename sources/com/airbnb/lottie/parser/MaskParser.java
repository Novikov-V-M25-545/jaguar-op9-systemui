package com.airbnb.lottie.parser;

/* loaded from: classes.dex */
class MaskParser {
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0061  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x001f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    static com.airbnb.lottie.model.content.Mask parse(com.airbnb.lottie.parser.moshi.JsonReader r10, com.airbnb.lottie.LottieComposition r11) throws java.io.IOException {
        /*
            r10.beginObject()
            r0 = 0
            r1 = 0
            r4 = r0
            r2 = r1
            r3 = r2
        L8:
            boolean r5 = r10.hasNext()
            if (r5 == 0) goto Lc4
            java.lang.String r5 = r10.nextName()
            r5.hashCode()
            int r6 = r5.hashCode()
            r7 = 2
            r8 = 1
            r9 = -1
            switch(r6) {
                case 111: goto L42;
                case 3588: goto L37;
                case 104433: goto L2c;
                case 3357091: goto L21;
                default: goto L1f;
            }
        L1f:
            r6 = r9
            goto L4c
        L21:
            java.lang.String r6 = "mode"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L2a
            goto L1f
        L2a:
            r6 = 3
            goto L4c
        L2c:
            java.lang.String r6 = "inv"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L35
            goto L1f
        L35:
            r6 = r7
            goto L4c
        L37:
            java.lang.String r6 = "pt"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L40
            goto L1f
        L40:
            r6 = r8
            goto L4c
        L42:
            java.lang.String r6 = "o"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L4b
            goto L1f
        L4b:
            r6 = r0
        L4c:
            switch(r6) {
                case 0: goto Lbe;
                case 1: goto Lb8;
                case 2: goto Lb2;
                case 3: goto L53;
                default: goto L4f;
            }
        L4f:
            r10.skipValue()
            goto L8
        L53:
            java.lang.String r1 = r10.nextString()
            r1.hashCode()
            int r6 = r1.hashCode()
            switch(r6) {
                case 97: goto L77;
                case 105: goto L6c;
                case 115: goto L63;
                default: goto L61;
            }
        L61:
            r7 = r9
            goto L81
        L63:
            java.lang.String r6 = "s"
            boolean r1 = r1.equals(r6)
            if (r1 != 0) goto L81
            goto L61
        L6c:
            java.lang.String r6 = "i"
            boolean r1 = r1.equals(r6)
            if (r1 != 0) goto L75
            goto L61
        L75:
            r7 = r8
            goto L81
        L77:
            java.lang.String r6 = "a"
            boolean r1 = r1.equals(r6)
            if (r1 != 0) goto L80
            goto L61
        L80:
            r7 = r0
        L81:
            switch(r7) {
                case 0: goto Lae;
                case 1: goto La5;
                case 2: goto La1;
                default: goto L84;
            }
        L84:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r6 = "Unknown mask mode "
            r1.append(r6)
            r1.append(r5)
            java.lang.String r5 = ". Defaulting to Add."
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            com.airbnb.lottie.utils.Logger.warning(r1)
            com.airbnb.lottie.model.content.Mask$MaskMode r1 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_ADD
            goto L8
        La1:
            com.airbnb.lottie.model.content.Mask$MaskMode r1 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_SUBTRACT
            goto L8
        La5:
            java.lang.String r1 = "Animation contains intersect masks. They are not supported but will be treated like add masks."
            r11.addWarning(r1)
            com.airbnb.lottie.model.content.Mask$MaskMode r1 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_INTERSECT
            goto L8
        Lae:
            com.airbnb.lottie.model.content.Mask$MaskMode r1 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_ADD
            goto L8
        Lb2:
            boolean r4 = r10.nextBoolean()
            goto L8
        Lb8:
            com.airbnb.lottie.model.animatable.AnimatableShapeValue r2 = com.airbnb.lottie.parser.AnimatableValueParser.parseShapeData(r10, r11)
            goto L8
        Lbe:
            com.airbnb.lottie.model.animatable.AnimatableIntegerValue r3 = com.airbnb.lottie.parser.AnimatableValueParser.parseInteger(r10, r11)
            goto L8
        Lc4:
            r10.endObject()
            com.airbnb.lottie.model.content.Mask r10 = new com.airbnb.lottie.model.content.Mask
            r10.<init>(r1, r2, r3, r4)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.parser.MaskParser.parse(com.airbnb.lottie.parser.moshi.JsonReader, com.airbnb.lottie.LottieComposition):com.airbnb.lottie.model.content.Mask");
    }
}
