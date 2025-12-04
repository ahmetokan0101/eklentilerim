package com.kraptor;

import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;
import okhttp3.Interceptor;

/* compiled from: DiziPalOrijinal.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u000e\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 1, 0}, xi = 48)
@DebugMetadata(c = "com.kraptor.DiziPalOrijinal$Companion$getDomain$1", f = "DiziPalOrijinal.kt", i = {}, l = {49}, m = "invokeSuspend", n = {}, s = {})
@SourceDebugExtension({"SMAP\nDiziPalOrijinal.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DiziPalOrijinal.kt\ncom/kraptor/DiziPalOrijinal$Companion$getDomain$1\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,445:1\n230#2,2:446\n*S KotlinDebug\n*F\n+ 1 DiziPalOrijinal.kt\ncom/kraptor/DiziPalOrijinal$Companion$getDomain$1\n*L\n52#1:446,2\n*E\n"})
/* loaded from: classes.dex */
final class DiziPalOrijinal$Companion$getDomain$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super String>, Object> {
    int label;

    DiziPalOrijinal$Companion$getDomain$1(Continuation<? super DiziPalOrijinal$Companion$getDomain$1> continuation) {
        super(2, continuation);
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new DiziPalOrijinal$Companion$getDomain$1(continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super String> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0, types: [int] */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v2 */
    /* JADX WARN: Type inference failed for: r2v6 */
    /* JADX WARN: Type inference failed for: r2v8 */
    public final Object invokeSuspend(Object $result) {
        Object $result2;
        Object $result3;
        Object obj = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        ?? r2 = this.label;
        try {
            switch (r2) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    this.label = 1;
                    Object obj2 = Requests.get$default(MainActivityKt.getApp(), "https://raw.githubusercontent.com/Kraptor123/domainListesi/refs/heads/main/eklenti_domainleri.txt", (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, (Continuation) this, 4094, (Object) null);
                    if (obj2 == obj) {
                        return obj;
                    }
                    $result2 = $result;
                    $result3 = obj2;
                    break;
                case 1:
                    $result3 = $result;
                    ResultKt.throwOnFailure($result3);
                    $result2 = $result3;
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        } catch (Exception e) {
        }
        try {
            String domainListesi = ((NiceResponse) $result3).getText();
            Iterable $this$first$iv = StringsKt.split$default(domainListesi, new String[]{"|"}, false, 0, 6, (Object) null);
            r2 = 0;
            for (Object element$iv : $this$first$iv) {
                String it = (String) element$iv;
                if (StringsKt.startsWith$default(StringsKt.trim(it).toString(), "DiziPalOrijinal", false, 2, (Object) null)) {
                    return StringsKt.trim(StringsKt.substringAfter$default((String) element$iv, ":", (String) null, 2, (Object) null)).toString();
                }
            }
            throw new NoSuchElementException("Collection contains no element matching the predicate.");
        } catch (Exception e2) {
            r2 = $result2;
            return "https://dizipal1512.com";
        }
    }
}
