package com.kraptor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApi;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.Qualities;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ContentXExtractor.kt */
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0016\u0018\u00002\u00020\u0001:\u0001\u0018B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00052\b\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u000f0\u00132\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@¢\u0006\u0002\u0010\u0017R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u0019"}, d2 = {"Lcom/kraptor/TRsTX;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "TrstxVideoData", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nContentXExtractor.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ContentXExtractor.kt\ncom/kraptor/TRsTX\n+ 2 NiceResponse.kt\ncom/lagradost/nicehttp/NiceResponse\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,722:1\n96#2,5:723\n1557#3:728\n1628#3,3:729\n*S KotlinDebug\n*F\n+ 1 ContentXExtractor.kt\ncom/kraptor/TRsTX\n*L\n287#1:723,5\n289#1:728\n289#1:729,3\n*E\n"})
/* loaded from: classes.dex */
public class TRsTX extends ExtractorApi {

    @NotNull
    private final String name = "TRsTX";

    @NotNull
    private final String mainUrl = "https://trstx.org";
    private final boolean requiresReferer = true;

    /* compiled from: ContentXExtractor.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.TRsTX", f = "ContentXExtractor.kt", i = {0, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3}, l = {283, 287, 304, 322}, m = "getUrl$suspendImpl", n = {"$this", "callback", "extRef", "$this", "callback", "extRef", "$this", "callback", "extRef", "vidLinks", "vidMap", "item", "$this", "callback", "extRef"}, s = {"L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$6", "L$0", "L$1", "L$2"})
    /* renamed from: com.kraptor.TRsTX$getUrl$1, reason: invalid class name */
    static final class AnonymousClass1 extends ContinuationImpl {
        Object L$0;
        Object L$1;
        Object L$2;
        Object L$3;
        Object L$4;
        Object L$5;
        Object L$6;
        int label;
        /* synthetic */ Object result;

        AnonymousClass1(Continuation<? super AnonymousClass1> continuation) {
            super(continuation);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return TRsTX.getUrl$suspendImpl(TRsTX.this, null, null, null, null, (Continuation) this);
        }
    }

    @Nullable
    public Object getUrl(@NotNull String str, @Nullable String str2, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Unit> continuation) {
        return getUrl$suspendImpl(this, str, str2, function1, function12, continuation);
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public String getMainUrl() {
        return this.mainUrl;
    }

    public boolean getRequiresReferer() {
        return this.requiresReferer;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.lagradost.cloudstream3.ErrorLoadingException */
    /* JADX WARN: Removed duplicated region for block: B:40:0x01eb  */
    /* JADX WARN: Removed duplicated region for block: B:55:0x029f  */
    /* JADX WARN: Removed duplicated region for block: B:66:0x033b  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x035f  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x0382  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    /* JADX WARN: Removed duplicated region for block: B:87:0x0426  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:63:0x0328 -> B:64:0x032f). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:83:0x0419 -> B:84:0x041c). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    static /* synthetic */ java.lang.Object getUrl$suspendImpl(com.kraptor.TRsTX r54, java.lang.String r55, java.lang.String r56, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r57, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r58, kotlin.coroutines.Continuation<? super kotlin.Unit> r59) throws com.lagradost.cloudstream3.ErrorLoadingException {
        /*
            Method dump skipped, instructions count: 1098
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.TRsTX.getUrl$suspendImpl(com.kraptor.TRsTX, java.lang.String, java.lang.String, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* compiled from: ContentXExtractor.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.TRsTX$getUrl$2", f = "ContentXExtractor.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.kraptor.TRsTX$getUrl$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
        final /* synthetic */ String $extRef;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(String str, Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
            this.$extRef = str;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass2 = new AnonymousClass2(this.$extRef, continuation);
            anonymousClass2.L$0 = obj;
            return anonymousClass2;
        }

        public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
            return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    ExtractorLink $this$newExtractorLink = (ExtractorLink) this.L$0;
                    $this$newExtractorLink.setHeaders(MapsKt.mapOf(TuplesKt.to("Referer", this.$extRef)));
                    $this$newExtractorLink.setQuality(ExtractorApiKt.getQualityFromName(String.valueOf(Qualities.Unknown.getValue())));
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* compiled from: ContentXExtractor.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u000b\u0010\n\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u000b\u001a\u0004\u0018\u00010\u0003HÆ\u0003J!\u0010\f\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003HÆ\u0001J\u0013\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0010\u001a\u00020\u0011HÖ\u0001J\t\u0010\u0012\u001a\u00020\u0003HÖ\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/kraptor/TRsTX$TrstxVideoData;", "", "title", "", "file", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getTitle", "()Ljava/lang/String;", "getFile", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
    public static final /* data */ class TrstxVideoData {

        @Nullable
        private final String file;

        @Nullable
        private final String title;

        /* JADX WARN: Illegal instructions before constructor call */
        public TrstxVideoData() {
            String str = null;
            this(str, str, 3, str);
        }

        public static /* synthetic */ TrstxVideoData copy$default(TrstxVideoData trstxVideoData, String str, String str2, int r3, Object obj) {
            if ((r3 & 1) != 0) {
                str = trstxVideoData.title;
            }
            if ((r3 & 2) != 0) {
                str2 = trstxVideoData.file;
            }
            return trstxVideoData.copy(str, str2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getFile() {
            return this.file;
        }

        @NotNull
        public final TrstxVideoData copy(@JsonProperty("title") @Nullable String title, @JsonProperty("file") @Nullable String file) {
            return new TrstxVideoData(title, file);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TrstxVideoData)) {
                return false;
            }
            TrstxVideoData trstxVideoData = (TrstxVideoData) other;
            return Intrinsics.areEqual(this.title, trstxVideoData.title) && Intrinsics.areEqual(this.file, trstxVideoData.file);
        }

        public int hashCode() {
            return ((this.title == null ? 0 : this.title.hashCode()) * 31) + (this.file != null ? this.file.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "TrstxVideoData(title=" + this.title + ", file=" + this.file + ')';
        }

        public TrstxVideoData(@JsonProperty("title") @Nullable String title, @JsonProperty("file") @Nullable String file) {
            this.title = title;
            this.file = file;
        }

        public /* synthetic */ TrstxVideoData(String str, String str2, int r4, DefaultConstructorMarker defaultConstructorMarker) {
            this((r4 & 1) != 0 ? null : str, (r4 & 2) != 0 ? null : str2);
        }

        @Nullable
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        public final String getFile() {
            return this.file;
        }
    }
}
