package com.kraptor;

import com.lagradost.cloudstream3.MainAPIKt;
import kotlin.Metadata;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ContentXExtractor.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u001a\u000e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0001¨\u0006\u0003"}, d2 = {"decodeEE", "", "encoded", "DiziPalOrijinal_debug"}, k = 2, mv = {2, 1, 0}, xi = 48)
/* loaded from: classes.dex */
public final class ContentXExtractorKt {
    @NotNull
    public static final String decodeEE(@NotNull String encoded) {
        String s = StringsKt.replace$default(StringsKt.replace$default(encoded, '-', '+', false, 4, (Object) null), '_', '/', false, 4, (Object) null);
        while (s.length() % 4 != 0) {
            s = s + '=';
        }
        byte[] decodedBytes = MainAPIKt.base64DecodeArray(s);
        String a = new String(decodedBytes, Charsets.UTF_8);
        StringBuilder $this$decodeEE_u24lambda_u240 = new StringBuilder();
        int length = a.length();
        for (int r8 = 0; r8 < length; r8++) {
            char c = a.charAt(r8);
            if ('A' <= c && c < '[') {
                $this$decodeEE_u24lambda_u240.append((char) ((((c - 'A') + 13) % 26) + 65));
            } else if ('a' <= c && c < '{') {
                $this$decodeEE_u24lambda_u240.append((char) ((((c - 'a') + 13) % 26) + 97));
            } else {
                $this$decodeEE_u24lambda_u240.append(c);
            }
        }
        String rot13 = $this$decodeEE_u24lambda_u240.toString();
        return StringsKt.reversed(rot13).toString();
    }
}
