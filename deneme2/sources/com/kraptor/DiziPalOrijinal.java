package com.kraptor;

import com.lagradost.api.Log;
import com.lagradost.cloudstream3.Actor;
import com.lagradost.cloudstream3.Episode;
import com.lagradost.cloudstream3.LoadResponse;
import com.lagradost.cloudstream3.MainAPI;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MainPageData;
import com.lagradost.cloudstream3.MovieLoadResponse;
import com.lagradost.cloudstream3.MovieSearchResponse;
import com.lagradost.cloudstream3.Score;
import com.lagradost.cloudstream3.SearchResponse;
import com.lagradost.cloudstream3.TvSeriesLoadResponse;
import com.lagradost.cloudstream3.TvSeriesSearchResponse;
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.network.CloudflareKiller;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.sync.Mutex;
import kotlinx.coroutines.sync.MutexKt;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: DiziPalOrijinal.kt */
@Metadata(d1 = {"\u0000\u009a\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 Z2\u00020\u0001:\u0002Z[B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010>\u001a\u00020?H\u0082@¢\u0006\u0002\u0010@J\u001e\u00106\u001a\u00020A2\u0006\u0010B\u001a\u00020C2\u0006\u0010D\u001a\u00020EH\u0096@¢\u0006\u0002\u0010FJ\u000e\u0010G\u001a\u0004\u0018\u00010H*\u00020IH\u0002J\u001c\u0010J\u001a\b\u0012\u0004\u0012\u00020H042\u0006\u0010K\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010LJ\u001c\u0010M\u001a\b\u0012\u0004\u0012\u00020H042\u0006\u0010K\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010LJ\u0018\u0010N\u001a\u0004\u0018\u00010O2\u0006\u0010P\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010LJF\u0010Q\u001a\u00020\u000e2\u0006\u0010R\u001a\u00020\u00052\u0006\u0010S\u001a\u00020\u000e2\u0012\u0010T\u001a\u000e\u0012\u0004\u0012\u00020V\u0012\u0004\u0012\u00020?0U2\u0012\u0010W\u001a\u000e\u0012\u0004\u0012\u00020X\u0012\u0004\u0012\u00020?0UH\u0096@¢\u0006\u0002\u0010YR\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u0014\u0010\r\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0011\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0007\"\u0004\b\u0013\u0010\tR\u0014\u0010\u0014\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0010R\u001a\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00180\u0017X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u001a\u0010\u001b\u001a\u00020\u000eX\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u0010\"\u0004\b\u001d\u0010\u001eR\u001a\u0010\u001f\u001a\u00020 X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\"\"\u0004\b#\u0010$R\u001a\u0010%\u001a\u00020 X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\"\"\u0004\b'\u0010$R\u001b\u0010(\u001a\u00020)8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b,\u0010-\u001a\u0004\b*\u0010+R\u001b\u0010.\u001a\u00020/8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b2\u0010-\u001a\u0004\b0\u00101R\u001a\u00103\u001a\b\u0012\u0004\u0012\u00020504X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b6\u00107R\u001c\u00108\u001a\u0010\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0005\u0018\u000109X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010:\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010;\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010<\u001a\u00020=X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\\"}, d2 = {"Lcom/kraptor/DiziPalOrijinal;", "Lcom/lagradost/cloudstream3/MainAPI;", "<init>", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "setMainUrl", "(Ljava/lang/String;)V", "name", "getName", "setName", "hasMainPage", "", "getHasMainPage", "()Z", "lang", "getLang", "setLang", "hasQuickSearch", "getHasQuickSearch", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "sequentialMainPage", "getSequentialMainPage", "setSequentialMainPage", "(Z)V", "sequentialMainPageDelay", "", "getSequentialMainPageDelay", "()J", "setSequentialMainPageDelay", "(J)V", "sequentialMainPageScrollDelay", "getSequentialMainPageScrollDelay", "setSequentialMainPageScrollDelay", "cloudflareKiller", "Lcom/lagradost/cloudstream3/network/CloudflareKiller;", "getCloudflareKiller", "()Lcom/lagradost/cloudstream3/network/CloudflareKiller;", "cloudflareKiller$delegate", "Lkotlin/Lazy;", "interceptor", "Lcom/kraptor/DiziPalOrijinal$CloudflareInterceptor;", "getInterceptor", "()Lcom/kraptor/DiziPalOrijinal$CloudflareInterceptor;", "interceptor$delegate", "mainPage", "", "Lcom/lagradost/cloudstream3/MainPageData;", "getMainPage", "()Ljava/util/List;", "sessionCookies", "", "cKey", "cValue", "initMutex", "Lkotlinx/coroutines/sync/Mutex;", "initSession", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toMainPageResult", "Lcom/lagradost/cloudstream3/SearchResponse;", "Lorg/jsoup/nodes/Element;", "search", "query", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "quickSearch", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "url", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "CloudflareInterceptor", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nDiziPalOrijinal.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DiziPalOrijinal.kt\ncom/kraptor/DiziPalOrijinal\n+ 2 Mutex.kt\nkotlinx/coroutines/sync/MutexKt\n+ 3 Maps.kt\nkotlin/collections/MapsKt__MapsKt\n+ 4 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 5 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,445:1\n116#2,8:446\n125#2,2:460\n462#3:454\n412#3:455\n1246#4,4:456\n1755#4,3:462\n1611#4,9:465\n1863#4:474\n1864#4:476\n1620#4:477\n1611#4,9:478\n1863#4:487\n1864#4:489\n1620#4:490\n1611#4,9:491\n1863#4:500\n1864#4:503\n1620#4:504\n1557#4:505\n1628#4,3:506\n1557#4:509\n1628#4,3:510\n1611#4,9:513\n1863#4:522\n1864#4:525\n1620#4:526\n1557#4:527\n1628#4,3:528\n1#5:475\n1#5:488\n1#5:501\n1#5:502\n1#5:523\n1#5:524\n*S KotlinDebug\n*F\n+ 1 DiziPalOrijinal.kt\ncom/kraptor/DiziPalOrijinal\n*L\n105#1:446,8\n105#1:460,2\n115#1:454\n115#1:455\n115#1:456,4\n139#1:462,3\n164#1:465,9\n164#1:474\n164#1:476\n164#1:477\n209#1:478,9\n209#1:487\n209#1:489\n209#1:490\n285#1:491,9\n285#1:500\n285#1:503\n285#1:504\n327#1:505\n327#1:506,3\n328#1:509\n328#1:510,3\n341#1:513,9\n341#1:522\n341#1:525\n341#1:526\n352#1:527\n352#1:528,3\n164#1:475\n209#1:488\n285#1:502\n341#1:524\n*E\n"})
/* loaded from: classes.dex */
public final class DiziPalOrijinal extends MainAPI {

    /* renamed from: Companion, reason: from kotlin metadata */
    @NotNull
    public static final Companion INSTANCE = new Companion(null);

    @Nullable
    private String cKey;

    @Nullable
    private String cValue;
    private final boolean hasQuickSearch;

    @Nullable
    private Map<String, String> sessionCookies;

    @NotNull
    private String mainUrl = INSTANCE.getDomain();

    @NotNull
    private String name = "DiziPalOrijinal";
    private final boolean hasMainPage = true;

    @NotNull
    private String lang = "tr";

    @NotNull
    private final Set<TvType> supportedTypes = SetsKt.setOf(TvType.TvSeries);
    private boolean sequentialMainPage = true;
    private long sequentialMainPageDelay = 250;
    private long sequentialMainPageScrollDelay = 250;

    /* renamed from: cloudflareKiller$delegate, reason: from kotlin metadata */
    @NotNull
    private final Lazy cloudflareKiller = LazyKt.lazy(new Function0() { // from class: com.kraptor.DiziPalOrijinal$$ExternalSyntheticLambda0
        public final Object invoke() {
            return DiziPalOrijinal.cloudflareKiller_delegate$lambda$0();
        }
    });

    /* renamed from: interceptor$delegate, reason: from kotlin metadata */
    @NotNull
    private final Lazy interceptor = LazyKt.lazy(new Function0() { // from class: com.kraptor.DiziPalOrijinal$$ExternalSyntheticLambda1
        public final Object invoke() {
            return DiziPalOrijinal.interceptor_delegate$lambda$1(this.f$0);
        }
    });

    @NotNull
    private final List<MainPageData> mainPage = MainAPIKt.mainPageOf(new Pair[]{TuplesKt.to(getMainUrl() + '/', "Yeni Eklenen Bölümler"), TuplesKt.to("", "Yeni Eklenenler"), TuplesKt.to("", "Yüksek Imdb Puanlı Diziler"), TuplesKt.to("", "Yeni Filmler"), TuplesKt.to("1", "Exxen Dizileri"), TuplesKt.to("6", "Disney+ Dizileri"), TuplesKt.to("10", "Netflix Dizileri"), TuplesKt.to("53", "Amazon Dizileri"), TuplesKt.to("54", "Apple TV+ Dizileri"), TuplesKt.to("66", "Max Dizileri"), TuplesKt.to("78", "Hulu Dizileri"), TuplesKt.to("181", "TOD Dizileri"), TuplesKt.to("242", "Tabii Dizileri")});

    @NotNull
    private final Mutex initMutex = MutexKt.Mutex$default(false, 1, (Object) null);

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.DiziPalOrijinal", f = "DiziPalOrijinal.kt", i = {0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5}, l = {126, 140, 150, 162, 169, 181}, m = "getMainPage", n = {"this", "request", "page", "this", "request", "this", "request", "this", "request", "this", "request", "this", "request"}, s = {"L$0", "L$1", "I$0", "L$0", "L$1", "L$0", "L$1", "L$0", "L$1", "L$0", "L$1", "L$0", "L$1"})
    /* renamed from: com.kraptor.DiziPalOrijinal$getMainPage$1, reason: invalid class name */
    static final class AnonymousClass1 extends ContinuationImpl {
        int I$0;
        Object L$0;
        Object L$1;
        int label;
        /* synthetic */ Object result;

        AnonymousClass1(Continuation<? super AnonymousClass1> continuation) {
            super(continuation);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return DiziPalOrijinal.this.getMainPage(0, null, (Continuation) this);
        }
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.DiziPalOrijinal", f = "DiziPalOrijinal.kt", i = {0, 0, 1, 1}, l = {451, 110}, m = "initSession", n = {"this", "$this$withLock_u24default$iv", "this", "$this$withLock_u24default$iv"}, s = {"L$0", "L$1", "L$0", "L$1"})
    /* renamed from: com.kraptor.DiziPalOrijinal$initSession$1, reason: invalid class name and case insensitive filesystem */
    static final class C00001 extends ContinuationImpl {
        Object L$0;
        Object L$1;
        int label;
        /* synthetic */ Object result;

        C00001(Continuation<? super C00001> continuation) {
            super(continuation);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return DiziPalOrijinal.this.initSession((Continuation) this);
        }
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.DiziPalOrijinal", f = "DiziPalOrijinal.kt", i = {0, 0}, l = {312, 372, 383}, m = "load", n = {"this", "url"}, s = {"L$0", "L$1"})
    /* renamed from: com.kraptor.DiziPalOrijinal$load$1, reason: invalid class name and case insensitive filesystem */
    static final class C00011 extends ContinuationImpl {
        Object L$0;
        Object L$1;
        int label;
        /* synthetic */ Object result;

        C00011(Continuation<? super C00011> continuation) {
            super(continuation);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return DiziPalOrijinal.this.load(null, (Continuation) this);
        }
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.DiziPalOrijinal", f = "DiziPalOrijinal.kt", i = {0, 0, 0}, l = {398, 412}, m = "loadLinks", n = {"this", "subtitleCallback", "callback"}, s = {"L$0", "L$1", "L$2"})
    /* renamed from: com.kraptor.DiziPalOrijinal$loadLinks$1, reason: invalid class name and case insensitive filesystem */
    static final class C00021 extends ContinuationImpl {
        Object L$0;
        Object L$1;
        Object L$2;
        int label;
        /* synthetic */ Object result;

        C00021(Continuation<? super C00021> continuation) {
            super(continuation);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return DiziPalOrijinal.this.loadLinks(null, false, null, null, (Continuation) this);
        }
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.DiziPalOrijinal", f = "DiziPalOrijinal.kt", i = {0, 0, 1}, l = {263, 264}, m = "search", n = {"this", "query", "this"}, s = {"L$0", "L$1", "L$0"})
    /* renamed from: com.kraptor.DiziPalOrijinal$search$1, reason: invalid class name and case insensitive filesystem */
    static final class C00031 extends ContinuationImpl {
        Object L$0;
        Object L$1;
        int label;
        /* synthetic */ Object result;

        C00031(Continuation<? super C00031> continuation) {
            super(continuation);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return DiziPalOrijinal.this.search(null, (Continuation) this);
        }
    }

    @NotNull
    public String getMainUrl() {
        return this.mainUrl;
    }

    public void setMainUrl(@NotNull String str) {
        this.mainUrl = str;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(@NotNull String str) {
        this.name = str;
    }

    public boolean getHasMainPage() {
        return this.hasMainPage;
    }

    @NotNull
    public String getLang() {
        return this.lang;
    }

    public void setLang(@NotNull String str) {
        this.lang = str;
    }

    public boolean getHasQuickSearch() {
        return this.hasQuickSearch;
    }

    @NotNull
    public Set<TvType> getSupportedTypes() {
        return this.supportedTypes;
    }

    public boolean getSequentialMainPage() {
        return this.sequentialMainPage;
    }

    public void setSequentialMainPage(boolean z) {
        this.sequentialMainPage = z;
    }

    public long getSequentialMainPageDelay() {
        return this.sequentialMainPageDelay;
    }

    public void setSequentialMainPageDelay(long j) {
        this.sequentialMainPageDelay = j;
    }

    public long getSequentialMainPageScrollDelay() {
        return this.sequentialMainPageScrollDelay;
    }

    public void setSequentialMainPageScrollDelay(long j) {
        this.sequentialMainPageScrollDelay = j;
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0002¨\u0006\u0006"}, d2 = {"Lcom/kraptor/DiziPalOrijinal$Companion;", "", "<init>", "()V", "getDomain", "", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final String getDomain() {
            return (String) BuildersKt.runBlocking$default((CoroutineContext) null, new DiziPalOrijinal$Companion$getDomain$1(null), 1, (Object) null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CloudflareKiller cloudflareKiller_delegate$lambda$0() {
        return new CloudflareKiller();
    }

    private final CloudflareKiller getCloudflareKiller() {
        return (CloudflareKiller) this.cloudflareKiller.getValue();
    }

    private final CloudflareInterceptor getInterceptor() {
        return (CloudflareInterceptor) this.interceptor.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CloudflareInterceptor interceptor_delegate$lambda$1(DiziPalOrijinal this$0) {
        return new CloudflareInterceptor(this$0.getCloudflareKiller());
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\n"}, d2 = {"Lcom/kraptor/DiziPalOrijinal$CloudflareInterceptor;", "Lokhttp3/Interceptor;", "cloudflareKiller", "Lcom/lagradost/cloudstream3/network/CloudflareKiller;", "<init>", "(Lcom/lagradost/cloudstream3/network/CloudflareKiller;)V", "intercept", "Lokhttp3/Response;", "chain", "Lokhttp3/Interceptor$Chain;", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
    public static final class CloudflareInterceptor implements Interceptor {

        @NotNull
        private final CloudflareKiller cloudflareKiller;

        public CloudflareInterceptor(@NotNull CloudflareKiller cloudflareKiller) {
            this.cloudflareKiller = cloudflareKiller;
        }

        @NotNull
        public Response intercept(@NotNull Interceptor.Chain chain) {
            Request request = chain.request();
            Response response = chain.proceed(request);
            Document doc = Jsoup.parse(response.peekBody(1048576L).string());
            if (StringsKt.contains$default(doc.html(), "Just a moment", false, 2, (Object) null)) {
                Log.INSTANCE.d("kraptor_Dizipal", "!!cloudflare geldi!!");
                return this.cloudflareKiller.intercept(chain);
            }
            return response;
        }
    }

    @NotNull
    public List<MainPageData> getMainPage() {
        return this.mainPage;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:41:0x00b9 A[Catch: all -> 0x0205, TRY_ENTER, TRY_LEAVE, TryCatch #0 {all -> 0x0205, blocks: (B:31:0x0095, B:41:0x00b9), top: B:74:0x0095 }] */
    /* JADX WARN: Removed duplicated region for block: B:47:0x0137 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0138  */
    /* JADX WARN: Removed duplicated region for block: B:52:0x016a A[Catch: all -> 0x01ed, LOOP:0: B:50:0x0164->B:52:0x016a, LOOP_END, TryCatch #4 {all -> 0x01ed, blocks: (B:49:0x0142, B:50:0x0164, B:52:0x016a, B:53:0x0190, B:55:0x01a3, B:57:0x01a9, B:59:0x01b3, B:60:0x01b7, B:61:0x01e2), top: B:82:0x0142 }] */
    /* JADX WARN: Removed duplicated region for block: B:55:0x01a3 A[Catch: all -> 0x01ed, TryCatch #4 {all -> 0x01ed, blocks: (B:49:0x0142, B:50:0x0164, B:52:0x016a, B:53:0x0190, B:55:0x01a3, B:57:0x01a9, B:59:0x01b3, B:60:0x01b7, B:61:0x01e2), top: B:82:0x0142 }] */
    /* JADX WARN: Removed duplicated region for block: B:56:0x01a8  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x01b3 A[Catch: all -> 0x01ed, TryCatch #4 {all -> 0x01ed, blocks: (B:49:0x0142, B:50:0x0164, B:52:0x016a, B:53:0x0190, B:55:0x01a3, B:57:0x01a9, B:59:0x01b3, B:60:0x01b7, B:61:0x01e2), top: B:82:0x0142 }] */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x0099 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final java.lang.Object initSession(kotlin.coroutines.Continuation<? super kotlin.Unit> r27) throws java.lang.Throwable {
        /*
            Method dump skipped, instructions count: 544
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.DiziPalOrijinal.initSession(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x010c  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x0116  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0147  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x01fe  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x035e  */
    /* JADX WARN: Removed duplicated region for block: B:78:0x0522  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    /* JADX WARN: Removed duplicated region for block: B:83:0x0558  */
    /* JADX WARN: Removed duplicated region for block: B:99:0x013a A[SYNTHETIC] */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object getMainPage(int r34, @org.jetbrains.annotations.NotNull com.lagradost.cloudstream3.MainPageRequest r35, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.lagradost.cloudstream3.HomePageResponse> r36) throws org.json.JSONException {
        /*
            Method dump skipped, instructions count: 1426
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.DiziPalOrijinal.getMainPage(int, com.lagradost.cloudstream3.MainPageRequest, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final SearchResponse toMainPageResult(Element $this$toMainPageResult) {
        String alt;
        String href;
        String strReplace$default;
        String alt2;
        Element textElement = $this$toMainPageResult.selectFirst("div.text.block div.text-white.text-sm");
        if (textElement == null || StringsKt.isBlank(textElement.text())) {
            Element elementSelectFirst = $this$toMainPageResult.selectFirst("img");
            if (elementSelectFirst == null || (alt = elementSelectFirst.attr("alt")) == null) {
                return null;
            }
        } else {
            Element elementSelectFirst2 = $this$toMainPageResult.selectFirst("img");
            if (elementSelectFirst2 == null || (alt2 = elementSelectFirst2.attr("alt")) == null) {
                alt2 = "";
            }
            alt = alt2 + ' ' + textElement.text();
        }
        String title = alt;
        Element aEl = $this$toMainPageResult.selectFirst("a");
        if (aEl == null) {
            return null;
        }
        String rawHref = aEl.attr("href");
        if (StringsKt.contains$default(rawHref, "/bolum/", false, 2, (Object) null)) {
            String strFixUrlNull = MainAPIKt.fixUrlNull(this, rawHref);
            href = (strFixUrlNull == null || (strReplace$default = StringsKt.replace$default(strFixUrlNull, "/bolum/", "/series/", false, 4, (Object) null)) == null) ? null : new Regex("-[0-9]+x.*$").replace(strReplace$default, "");
        } else {
            href = MainAPIKt.fixUrlNull(this, rawHref);
        }
        if (href == null) {
            return null;
        }
        DiziPalOrijinal diziPalOrijinal = this;
        Element elementSelectFirst3 = $this$toMainPageResult.selectFirst("img");
        final String posterUrl = MainAPIKt.fixUrlNull(diziPalOrijinal, elementSelectFirst3 != null ? elementSelectFirst3.attr("data-src") : null);
        Element elementSelectFirst4 = $this$toMainPageResult.selectFirst("h4");
        String imdbScore = elementSelectFirst4 != null ? elementSelectFirst4.text() : null;
        final String puan = StringsKt.contains$default(String.valueOf(imdbScore), "0.0", false, 2, (Object) null) ? "" : imdbScore;
        return StringsKt.contains$default(href, "/movies/", false, 2, (Object) null) ? MainAPIKt.newMovieSearchResponse$default(this, title, href, TvType.Movie, false, new Function1() { // from class: com.kraptor.DiziPalOrijinal$$ExternalSyntheticLambda5
            public final Object invoke(Object obj) {
                return DiziPalOrijinal.toMainPageResult$lambda$7(posterUrl, puan, (MovieSearchResponse) obj);
            }
        }, 8, (Object) null) : MainAPIKt.newTvSeriesSearchResponse$default(this, title, href, TvType.TvSeries, false, new Function1() { // from class: com.kraptor.DiziPalOrijinal$$ExternalSyntheticLambda6
            public final Object invoke(Object obj) {
                return DiziPalOrijinal.toMainPageResult$lambda$8(posterUrl, puan, (TvSeriesSearchResponse) obj);
            }
        }, 8, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit toMainPageResult$lambda$7(String $posterUrl, String $puan, MovieSearchResponse $this$newMovieSearchResponse) {
        $this$newMovieSearchResponse.setPosterUrl($posterUrl);
        $this$newMovieSearchResponse.setScore(Score.Companion.from10($puan));
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit toMainPageResult$lambda$8(String $posterUrl, String $puan, TvSeriesSearchResponse $this$newTvSeriesSearchResponse) {
        $this$newTvSeriesSearchResponse.setPosterUrl($posterUrl);
        $this$newTvSeriesSearchResponse.setScore(Score.Companion.from10($puan));
        return Unit.INSTANCE;
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x012d A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:20:0x012e  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x014a  */
    /* JADX WARN: Removed duplicated region for block: B:25:0x014f  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object search(@org.jetbrains.annotations.NotNull java.lang.String r32, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.util.List<? extends com.lagradost.cloudstream3.SearchResponse>> r33) throws org.json.JSONException {
        /*
            Method dump skipped, instructions count: 628
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.DiziPalOrijinal.search(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$14$lambda$12(String $posterUrl, MovieSearchResponse $this$newMovieSearchResponse) {
        $this$newMovieSearchResponse.setPosterUrl($posterUrl);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$14$lambda$13(String $posterUrl, TvSeriesSearchResponse $this$newTvSeriesSearchResponse) {
        $this$newTvSeriesSearchResponse.setPosterUrl($posterUrl);
        return Unit.INSTANCE;
    }

    @Nullable
    public Object quickSearch(@NotNull String query, @NotNull Continuation<? super List<? extends SearchResponse>> continuation) {
        return search(query, continuation);
    }

    /* JADX WARN: Removed duplicated region for block: B:178:0x0417  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object load(@org.jetbrains.annotations.NotNull java.lang.String r41, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.lagradost.cloudstream3.LoadResponse> r42) {
        /*
            Method dump skipped, instructions count: 1238
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.DiziPalOrijinal.load(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$20$lambda$19(String $bolumName, Integer $bolumSeason, Integer $bolumEpisode, String $poster, Episode $this$newEpisode) {
        $this$newEpisode.setName($bolumName);
        $this$newEpisode.setSeason($bolumSeason);
        $this$newEpisode.setEpisode($bolumEpisode);
        $this$newEpisode.setPosterUrl($poster);
        return Unit.INSTANCE;
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/MovieLoadResponse;"}, k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.DiziPalOrijinal$load$2", f = "DiziPalOrijinal.kt", i = {}, l = {380}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.kraptor.DiziPalOrijinal$load$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<MovieLoadResponse, Continuation<? super Unit>, Object> {
        final /* synthetic */ List<Pair<Actor, String>> $actors;
        final /* synthetic */ String $movieDesc;
        final /* synthetic */ Integer $movieDuration;
        final /* synthetic */ String $moviePoster;
        final /* synthetic */ List<String> $movieTags;
        final /* synthetic */ String $puanlar;
        final /* synthetic */ String $trailer;
        final /* synthetic */ Integer $year;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(String str, String str2, Integer num, List<String> list, String str3, Integer num2, List<Pair<Actor, String>> list2, String str4, Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
            this.$moviePoster = str;
            this.$movieDesc = str2;
            this.$year = num;
            this.$movieTags = list;
            this.$puanlar = str3;
            this.$movieDuration = num2;
            this.$actors = list2;
            this.$trailer = str4;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass2 = new AnonymousClass2(this.$moviePoster, this.$movieDesc, this.$year, this.$movieTags, this.$puanlar, this.$movieDuration, this.$actors, this.$trailer, continuation);
            anonymousClass2.L$0 = obj;
            return anonymousClass2;
        }

        public final Object invoke(MovieLoadResponse movieLoadResponse, Continuation<? super Unit> continuation) {
            return create(movieLoadResponse, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object $result) {
            Object obj = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    LoadResponse loadResponse = (MovieLoadResponse) this.L$0;
                    loadResponse.setPosterUrl(this.$moviePoster);
                    loadResponse.setPlot(this.$movieDesc);
                    loadResponse.setYear(this.$year);
                    loadResponse.setTags(this.$movieTags);
                    loadResponse.setScore(Score.Companion.from10(this.$puanlar));
                    loadResponse.setDuration(this.$movieDuration);
                    LoadResponse.Companion.addActors(loadResponse, this.$actors);
                    this.label = 1;
                    if (LoadResponse.Companion.addTrailer$default(LoadResponse.Companion, loadResponse, this.$trailer, (String) null, false, (Continuation) this, 6, (Object) null) == obj) {
                        return obj;
                    }
                    break;
                case 1:
                    ResultKt.throwOnFailure($result);
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            return Unit.INSTANCE;
        }
    }

    /* compiled from: DiziPalOrijinal.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/TvSeriesLoadResponse;"}, k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kraptor.DiziPalOrijinal$load$3", f = "DiziPalOrijinal.kt", i = {}, l = {391}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.kraptor.DiziPalOrijinal$load$3, reason: invalid class name */
    static final class AnonymousClass3 extends SuspendLambda implements Function2<TvSeriesLoadResponse, Continuation<? super Unit>, Object> {
        final /* synthetic */ List<Pair<Actor, String>> $actors;
        final /* synthetic */ String $description;
        final /* synthetic */ Integer $duration;
        final /* synthetic */ String $poster;
        final /* synthetic */ String $puanlar;
        final /* synthetic */ List<String> $tags;
        final /* synthetic */ String $trailer;
        final /* synthetic */ Integer $year;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass3(String str, String str2, Integer num, List<String> list, String str3, Integer num2, List<Pair<Actor, String>> list2, String str4, Continuation<? super AnonymousClass3> continuation) {
            super(2, continuation);
            this.$poster = str;
            this.$description = str2;
            this.$year = num;
            this.$tags = list;
            this.$puanlar = str3;
            this.$duration = num2;
            this.$actors = list2;
            this.$trailer = str4;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass3 = new AnonymousClass3(this.$poster, this.$description, this.$year, this.$tags, this.$puanlar, this.$duration, this.$actors, this.$trailer, continuation);
            anonymousClass3.L$0 = obj;
            return anonymousClass3;
        }

        public final Object invoke(TvSeriesLoadResponse tvSeriesLoadResponse, Continuation<? super Unit> continuation) {
            return create(tvSeriesLoadResponse, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object $result) {
            Object obj = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    LoadResponse loadResponse = (TvSeriesLoadResponse) this.L$0;
                    loadResponse.setPosterUrl(this.$poster);
                    loadResponse.setPlot(this.$description);
                    loadResponse.setYear(this.$year);
                    loadResponse.setTags(this.$tags);
                    loadResponse.setScore(Score.Companion.from10(this.$puanlar));
                    loadResponse.setDuration(this.$duration);
                    LoadResponse.Companion.addActors(loadResponse, this.$actors);
                    this.label = 1;
                    if (LoadResponse.Companion.addTrailer$default(LoadResponse.Companion, loadResponse, this.$trailer, (String) null, false, (Continuation) this, 6, (Object) null) == obj) {
                        return obj;
                    }
                    break;
                case 1:
                    ResultKt.throwOnFailure($result);
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            return Unit.INSTANCE;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:24:0x013e A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:25:0x013f  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object loadLinks(@org.jetbrains.annotations.NotNull java.lang.String r26, boolean r27, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r28, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r29, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.Boolean> r30) throws org.json.JSONException {
        /*
            Method dump skipped, instructions count: 348
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.DiziPalOrijinal.loadLinks(java.lang.String, boolean, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }
}
