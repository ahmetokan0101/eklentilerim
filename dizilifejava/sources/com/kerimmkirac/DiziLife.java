package com.kerimmkirac;

import android.util.Base64;
import android.util.Log;
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
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.Qualities;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.MapsKt;
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Element;

/* compiled from: DiziLife.kt */
@Metadata(d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u001e\u0010 \u001a\u00020\"2\u0006\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020&H\u0096@¢\u0006\u0002\u0010'J\u000e\u0010(\u001a\u0004\u0018\u00010)*\u00020*H\u0002J\u001e\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\u00052\u0006\u0010#\u001a\u00020$H\u0096@¢\u0006\u0002\u0010.J\u000e\u0010/\u001a\u0004\u0018\u00010)*\u00020*H\u0002J\u001e\u00100\u001a\n\u0012\u0004\u0012\u00020)\u0018\u00010\u001e2\u0006\u0010-\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u00101J\u0018\u00102\u001a\u0004\u0018\u0001032\u0006\u00104\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u00101J\u000e\u00105\u001a\u0004\u0018\u00010)*\u00020*H\u0002JF\u00106\u001a\u00020\u000e2\u0006\u00107\u001a\u00020\u00052\u0006\u00108\u001a\u00020\u000e2\u0012\u00109\u001a\u000e\u0012\u0004\u0012\u00020;\u0012\u0004\u0012\u00020<0:2\u0012\u0010=\u001a\u000e\u0012\u0004\u0012\u00020>\u0012\u0004\u0012\u00020<0:H\u0097@¢\u0006\u0002\u0010?J\u0010\u0010@\u001a\u00020A2\u0006\u0010B\u001a\u00020AH\u0002J4\u0010C\u001a\u000e\u0012\u0004\u0012\u00020A\u0012\u0004\u0012\u00020A0D2\u0006\u0010E\u001a\u00020A2\u0006\u0010F\u001a\u00020A2\u0006\u0010G\u001a\u00020$2\u0006\u0010H\u001a\u00020$H\u0002J\u001a\u0010I\u001a\u0004\u0018\u00010\u00052\u0006\u0010J\u001a\u00020\u00052\u0006\u0010E\u001a\u00020\u0005H\u0002R\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u0014\u0010\r\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0011\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0007\"\u0004\b\u0013\u0010\tR\u0014\u0010\u0014\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0010R\u0014\u0010\u0016\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0010R\u001a\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u001a\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001eX\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b \u0010!¨\u0006K"}, d2 = {"Lcom/kerimmkirac/DiziLife;", "Lcom/lagradost/cloudstream3/MainAPI;", "<init>", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "setMainUrl", "(Ljava/lang/String;)V", "name", "getName", "setName", "hasMainPage", "", "getHasMainPage", "()Z", "lang", "getLang", "setLang", "hasQuickSearch", "getHasQuickSearch", "hasDownloadSupport", "getHasDownloadSupport", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "mainPage", "", "Lcom/lagradost/cloudstream3/MainPageData;", "getMainPage", "()Ljava/util/List;", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toMainPageResult", "Lcom/lagradost/cloudstream3/SearchResponse;", "Lorg/jsoup/nodes/Element;", "search", "Lcom/lagradost/cloudstream3/SearchResponseList;", "query", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toSearchResult", "quickSearch", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "url", "toRecommendationResult", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "md5", "", "bytes", "evpBytesToKey", "Lkotlin/Pair;", "passphrase", "salt", "keyLen", "ivLen", "opensslAesPassphraseDecrypt", "base64Cipher", "DiziLife_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nDiziLife.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DiziLife.kt\ncom/kerimmkirac/DiziLife\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,409:1\n1611#2,9:410\n1863#2:419\n1864#2:421\n1620#2:422\n1611#2,9:423\n1863#2:432\n1864#2:434\n1620#2:435\n1368#2:436\n1454#2,5:437\n1557#2:442\n1628#2,3:443\n774#2:446\n865#2,2:447\n1611#2,9:449\n1863#2:458\n1864#2:460\n1620#2:461\n1611#2,9:463\n1863#2:472\n1864#2:474\n1620#2:475\n1797#2,3:477\n1#3:420\n1#3:433\n1#3:459\n1#3:462\n1#3:473\n1#3:476\n*S KotlinDebug\n*F\n+ 1 DiziLife.kt\ncom/kerimmkirac/DiziLife\n*L\n36#1:410,9\n36#1:419\n36#1:421\n36#1:422\n61#1:423,9\n61#1:432\n61#1:434\n61#1:435\n90#1:436\n90#1:437,5\n91#1:442\n91#1:443,3\n92#1:446\n92#1:447,2\n96#1:449,9\n96#1:458\n96#1:460\n96#1:461\n107#1:463,9\n107#1:472\n107#1:474\n107#1:475\n380#1:477,3\n36#1:420\n61#1:433\n96#1:459\n107#1:473\n*E\n"})
/* loaded from: C:\Users\HazDev\Downloads\classes.dex */
public final class DiziLife extends MainAPI {
    private final boolean hasDownloadSupport;
    private final boolean hasQuickSearch;

    @NotNull
    private String mainUrl = "https://dizi25.life";

    @NotNull
    private String name = "DiziLife";
    private final boolean hasMainPage = true;

    @NotNull
    private String lang = "tr";

    @NotNull
    private final Set<TvType> supportedTypes = SetsKt.setOf(new TvType[]{TvType.Movie, TvType.TvSeries});

    @NotNull
    private final List<MainPageData> mainPage = MainAPIKt.mainPageOf(new Pair[]{TuplesKt.to(getMainUrl() + "/diziler", "Diziler"), TuplesKt.to(getMainUrl() + "/filmler", "Filmler")});

    /* compiled from: DiziLife.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife", f = "DiziLife.kt", i = {0, 0}, l = {35}, m = "getMainPage", n = {"this", "request"}, s = {"L$0", "L$1"})
    /* renamed from: com.kerimmkirac.DiziLife$getMainPage$1, reason: invalid class name */
    static final class AnonymousClass1 extends ContinuationImpl {
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
            return DiziLife.this.getMainPage(0, null, (Continuation) this);
        }
    }

    /* compiled from: DiziLife.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife", f = "DiziLife.kt", i = {0, 0}, l = {82, 128, 140}, m = "load", n = {"this", "url"}, s = {"L$0", "L$1"})
    /* renamed from: com.kerimmkirac.DiziLife$load$1, reason: invalid class name and case insensitive filesystem */
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
            return DiziLife.this.load(null, (Continuation) this);
        }
    }

    /* compiled from: DiziLife.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife", f = "DiziLife.kt", i = {0, 0}, l = {184, 301, 336}, m = "loadLinks", n = {"this", "callback"}, s = {"L$0", "L$1"})
    /* renamed from: com.kerimmkirac.DiziLife$loadLinks$1, reason: invalid class name and case insensitive filesystem */
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
            return DiziLife.this.loadLinks(null, false, null, null, (Continuation) this);
        }
    }

    /* compiled from: DiziLife.kt */
    @Metadata(k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife", f = "DiziLife.kt", i = {0, 1}, l = {56, 58}, m = "search", n = {"this", "this"}, s = {"L$0", "L$0"})
    /* renamed from: com.kerimmkirac.DiziLife$search$1, reason: invalid class name and case insensitive filesystem */
    static final class C00041 extends ContinuationImpl {
        Object L$0;
        int label;
        /* synthetic */ Object result;

        C00041(Continuation<? super C00041> continuation) {
            super(continuation);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return DiziLife.this.search(null, 0, (Continuation) this);
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

    public boolean getHasDownloadSupport() {
        return this.hasDownloadSupport;
    }

    @NotNull
    public Set<TvType> getSupportedTypes() {
        return this.supportedTypes;
    }

    @NotNull
    public List<MainPageData> getMainPage() {
        return this.mainPage;
    }

    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object getMainPage(int r26, @org.jetbrains.annotations.NotNull com.lagradost.cloudstream3.MainPageRequest r27, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.lagradost.cloudstream3.HomePageResponse> r28) {
        /*
            Method dump skipped, instructions count: 246
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kerimmkirac.DiziLife.getMainPage(int, com.lagradost.cloudstream3.MainPageRequest, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final SearchResponse toMainPageResult(Element $this$toMainPageResult) {
        String title;
        String href;
        String strText;
        Element elementSelectFirst = $this$toMainPageResult.selectFirst("h3");
        if (elementSelectFirst == null || (title = elementSelectFirst.text()) == null || (href = MainAPIKt.fixUrlNull(this, $this$toMainPageResult.attr("data-url"))) == null) {
            return null;
        }
        DiziLife diziLife = this;
        Element elementSelectFirst2 = $this$toMainPageResult.selectFirst("img");
        String posterUrl = MainAPIKt.fixUrlNull(diziLife, elementSelectFirst2 != null ? elementSelectFirst2.attr("src") : null);
        Element elementSelectFirst3 = $this$toMainPageResult.selectFirst(".card-rating");
        String ratingText = (elementSelectFirst3 == null || (strText = elementSelectFirst3.text()) == null) ? null : StringsKt.trim(strText).toString();
        Double rating = ratingText != null ? StringsKt.toDoubleOrNull(ratingText) : null;
        return MainAPIKt.newMovieSearchResponse$default(this, title, href, TvType.Movie, false, new DiziLife$$ExternalSyntheticLambda1(posterUrl, rating), 8, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit toMainPageResult$lambda$2(String $posterUrl, Double $rating, MovieSearchResponse $this$newMovieSearchResponse) {
        Score scoreFrom10;
        $this$newMovieSearchResponse.setPosterUrl($posterUrl);
        if ($rating != null) {
            double it = $rating.doubleValue();
            scoreFrom10 = Score.Companion.from10(Double.valueOf(it));
        } else {
            scoreFrom10 = null;
        }
        $this$newMovieSearchResponse.setScore(scoreFrom10);
        return Unit.INSTANCE;
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x0145  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object search(@org.jetbrains.annotations.NotNull java.lang.String r27, int r28, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.lagradost.cloudstream3.SearchResponseList> r29) {
        /*
            Method dump skipped, instructions count: 372
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kerimmkirac.DiziLife.search(java.lang.String, int, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final SearchResponse toSearchResult(Element $this$toSearchResult) {
        String title;
        String href;
        String strText;
        Element elementSelectFirst = $this$toSearchResult.selectFirst("h3");
        if (elementSelectFirst == null || (title = elementSelectFirst.text()) == null || (href = MainAPIKt.fixUrlNull(this, $this$toSearchResult.attr("data-url"))) == null) {
            return null;
        }
        DiziLife diziLife = this;
        Element elementSelectFirst2 = $this$toSearchResult.selectFirst("img");
        String posterUrl = MainAPIKt.fixUrlNull(diziLife, elementSelectFirst2 != null ? elementSelectFirst2.attr("src") : null);
        Element elementSelectFirst3 = $this$toSearchResult.selectFirst(".card-rating");
        String ratingText = (elementSelectFirst3 == null || (strText = elementSelectFirst3.text()) == null) ? null : StringsKt.trim(strText).toString();
        Double rating = ratingText != null ? StringsKt.toDoubleOrNull(ratingText) : null;
        return MainAPIKt.newMovieSearchResponse$default(this, title, href, TvType.Movie, false, new DiziLife$$ExternalSyntheticLambda6(posterUrl, rating), 8, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit toSearchResult$lambda$5(String $posterUrl, Double $rating, MovieSearchResponse $this$newMovieSearchResponse) {
        Score scoreFrom10;
        $this$newMovieSearchResponse.setPosterUrl($posterUrl);
        if ($rating != null) {
            double it = $rating.doubleValue();
            scoreFrom10 = Score.Companion.from10(Double.valueOf(it));
        } else {
            scoreFrom10 = null;
        }
        $this$newMovieSearchResponse.setScore(scoreFrom10);
        return Unit.INSTANCE;
    }

    @Nullable
    public Object quickSearch(@NotNull String query, @NotNull Continuation<? super List<? extends SearchResponse>> continuation) {
        return search(query, continuation);
    }

    /* JADX WARN: Removed duplicated region for block: B:128:0x0400  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    /* JADX WARN: Removed duplicated region for block: B:97:0x02bf  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object load(@org.jetbrains.annotations.NotNull java.lang.String r43, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.lagradost.cloudstream3.LoadResponse> r44) {
        /*
            Method dump skipped, instructions count: 1092
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kerimmkirac.DiziLife.load(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$12$lambda$11(String $episodeTitle, String $poster, int $episodeNumber, int $seasonNumber, Episode $this$newEpisode) {
        $this$newEpisode.setName($episodeTitle);
        $this$newEpisode.setPosterUrl($poster);
        $this$newEpisode.setEpisode(Integer.valueOf($episodeNumber));
        $this$newEpisode.setSeason(Integer.valueOf($seasonNumber));
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Comparable load$lambda$13(Episode it) {
        return it.getSeason();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Comparable load$lambda$14(Episode it) {
        return it.getEpisode();
    }

    /* compiled from: DiziLife.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/TvSeriesLoadResponse;"}, k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife$load$2", f = "DiziLife.kt", i = {}, l = {135}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.kerimmkirac.DiziLife$load$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<TvSeriesLoadResponse, Continuation<? super Unit>, Object> {
        final /* synthetic */ String $description;
        final /* synthetic */ String $poster;
        final /* synthetic */ String $rating;
        final /* synthetic */ List<SearchResponse> $recommendations;
        final /* synthetic */ List<String> $tags;
        final /* synthetic */ String $trailer;
        final /* synthetic */ Integer $year;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(String str, String str2, Integer num, List<String> list, String str3, List<? extends SearchResponse> list2, String str4, Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
            this.$poster = str;
            this.$description = str2;
            this.$year = num;
            this.$tags = list;
            this.$rating = str3;
            this.$recommendations = list2;
            this.$trailer = str4;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass2 = new AnonymousClass2(this.$poster, this.$description, this.$year, this.$tags, this.$rating, this.$recommendations, this.$trailer, continuation);
            anonymousClass2.L$0 = obj;
            return anonymousClass2;
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
                    loadResponse.setScore(Score.Companion.from10(this.$rating));
                    loadResponse.setRecommendations(this.$recommendations);
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

    /* compiled from: DiziLife.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/MovieLoadResponse;"}, k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife$load$3", f = "DiziLife.kt", i = {}, l = {148}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.kerimmkirac.DiziLife$load$3, reason: invalid class name */
    static final class AnonymousClass3 extends SuspendLambda implements Function2<MovieLoadResponse, Continuation<? super Unit>, Object> {
        final /* synthetic */ String $description;
        final /* synthetic */ Integer $duration;
        final /* synthetic */ String $poster;
        final /* synthetic */ String $rating;
        final /* synthetic */ List<SearchResponse> $recommendations;
        final /* synthetic */ List<String> $tags;
        final /* synthetic */ String $trailer;
        final /* synthetic */ Integer $year;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass3(String str, String str2, Integer num, List<String> list, String str3, Integer num2, List<? extends SearchResponse> list2, String str4, Continuation<? super AnonymousClass3> continuation) {
            super(2, continuation);
            this.$poster = str;
            this.$description = str2;
            this.$year = num;
            this.$tags = list;
            this.$rating = str3;
            this.$duration = num2;
            this.$recommendations = list2;
            this.$trailer = str4;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass3 = new AnonymousClass3(this.$poster, this.$description, this.$year, this.$tags, this.$rating, this.$duration, this.$recommendations, this.$trailer, continuation);
            anonymousClass3.L$0 = obj;
            return anonymousClass3;
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
                    loadResponse.setPosterUrl(this.$poster);
                    loadResponse.setPlot(this.$description);
                    loadResponse.setYear(this.$year);
                    loadResponse.setTags(this.$tags);
                    loadResponse.setScore(Score.Companion.from10(this.$rating));
                    loadResponse.setDuration(this.$duration);
                    loadResponse.setRecommendations(this.$recommendations);
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

    private final SearchResponse toRecommendationResult(Element $this$toRecommendationResult) {
        String strText;
        String title;
        String strText2;
        Element elementSelectFirst = $this$toRecommendationResult.selectFirst("h3.card-title");
        if (elementSelectFirst == null || (strText = elementSelectFirst.text()) == null || (title = StringsKt.trim(strText).toString()) == null) {
            return null;
        }
        String it = StringsKt.substringBefore$default(StringsKt.substringAfter$default($this$toRecommendationResult.attr("onclick"), "window.location.href='", (String) null, 2, (Object) null), "'", (String) null, 2, (Object) null);
        String href = MainAPIKt.fixUrlNull(this, it);
        if (href == null) {
            return null;
        }
        DiziLife diziLife = this;
        Element elementSelectFirst2 = $this$toRecommendationResult.selectFirst("img");
        String posterUrl = MainAPIKt.fixUrlNull(diziLife, elementSelectFirst2 != null ? elementSelectFirst2.attr("src") : null);
        Element elementSelectFirst3 = $this$toRecommendationResult.selectFirst(".card-rating");
        String ratingText = (elementSelectFirst3 == null || (strText2 = elementSelectFirst3.text()) == null) ? null : StringsKt.trim(strText2).toString();
        Double rating = ratingText != null ? StringsKt.toDoubleOrNull(ratingText) : null;
        return MainAPIKt.newMovieSearchResponse$default(this, title, href, TvType.Movie, false, new DiziLife$$ExternalSyntheticLambda5(posterUrl, rating), 8, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit toRecommendationResult$lambda$17(String $posterUrl, Double $rating, MovieSearchResponse $this$newMovieSearchResponse) {
        Score scoreFrom10;
        $this$newMovieSearchResponse.setPosterUrl($posterUrl);
        if ($rating != null) {
            double it = $rating.doubleValue();
            scoreFrom10 = Score.Companion.from10(Double.valueOf(it));
        } else {
            scoreFrom10 = null;
        }
        $this$newMovieSearchResponse.setScore(scoreFrom10);
        return Unit.INSTANCE;
    }

    /* JADX WARN: Removed duplicated region for block: B:7:0x0018  */
    @android.annotation.SuppressLint({"SuspiciousIndentation"})
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object loadLinks(@org.jetbrains.annotations.NotNull java.lang.String r27, boolean r28, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r29, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r30, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.Boolean> r31) {
        /*
            Method dump skipped, instructions count: 1332
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kerimmkirac.DiziLife.loadLinks(java.lang.String, boolean, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final String loadLinks$lambda$18(MatchResult it) {
        return StringsKt.replace$default(it.getValue(), "\\", "", false, 4, (Object) null);
    }

    /* compiled from: DiziLife.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife$loadLinks$2", f = "DiziLife.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.kerimmkirac.DiziLife$loadLinks$2, reason: invalid class name and case insensitive filesystem */
    static final class C00022 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
        private /* synthetic */ Object L$0;
        int label;

        C00022(Continuation<? super C00022> continuation) {
            super(2, continuation);
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> c00022 = new C00022(continuation);
            c00022.L$0 = obj;
            return c00022;
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
                    $this$newExtractorLink.setQuality(Qualities.Unknown.getValue());
                    $this$newExtractorLink.setHeaders(MapsKt.mapOf(new Pair[]{TuplesKt.to("Connection", "keep-alive"), TuplesKt.to("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0"), TuplesKt.to("Accept-Language", "en-US,en;q=0.5"), TuplesKt.to("DNT", "1"), TuplesKt.to("Sec-GPC", "1"), TuplesKt.to("Upgrade-Insecure-Requests", "1"), TuplesKt.to("Sec-Fetch-Dest", "document"), TuplesKt.to("Sec-Fetch-Mode", "navigate"), TuplesKt.to("Sec-Fetch-Site", "cross-site"), TuplesKt.to("Sec-Fetch-User", "?1"), TuplesKt.to("Priority", "u=4"), TuplesKt.to("Pragma", "no-cache"), TuplesKt.to("Cache-Control", "no-cache")}));
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* compiled from: DiziLife.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 1, 0}, xi = 48)
    @DebugMetadata(c = "com.kerimmkirac.DiziLife$loadLinks$3", f = "DiziLife.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.kerimmkirac.DiziLife$loadLinks$3, reason: invalid class name and case insensitive filesystem */
    static final class C00033 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
        private /* synthetic */ Object L$0;
        int label;

        C00033(Continuation<? super C00033> continuation) {
            super(2, continuation);
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> c00033 = new C00033(continuation);
            c00033.L$0 = obj;
            return c00033;
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
                    $this$newExtractorLink.setQuality(Qualities.Unknown.getValue());
                    $this$newExtractorLink.setHeaders(MapsKt.mapOf(new Pair[]{TuplesKt.to("Connection", "keep-alive"), TuplesKt.to("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0"), TuplesKt.to("Accept-Language", "en-US,en;q=0.5"), TuplesKt.to("DNT", "1"), TuplesKt.to("Sec-GPC", "1"), TuplesKt.to("Upgrade-Insecure-Requests", "1"), TuplesKt.to("Sec-Fetch-Dest", "document"), TuplesKt.to("Sec-Fetch-Mode", "navigate"), TuplesKt.to("Sec-Fetch-Site", "cross-site"), TuplesKt.to("Sec-Fetch-User", "?1"), TuplesKt.to("Priority", "u=4"), TuplesKt.to("Pragma", "no-cache"), TuplesKt.to("Cache-Control", "no-cache")}));
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    private final byte[] md5(byte[] bytes) {
        return MessageDigest.getInstance("MD5").digest(bytes);
    }

    private final Pair<byte[], byte[]> evpBytesToKey(byte[] passphrase, byte[] salt, int keyLen, int ivLen) {
        int totalLen = keyLen + ivLen;
        ArrayList<byte[]> result = new ArrayList();
        byte[] prev = new byte[0];
        while (true) {
            int length = 0;
            for (byte[] it : result) {
                length += it.length;
            }
            if (length >= totalLen) {
                break;
            }
            byte[] data = ArraysKt.plus(ArraysKt.plus(prev, passphrase), salt);
            prev = md5(data);
            result.add(prev);
        }
        ArrayList $this$fold$iv = result;
        byte[] bArrPlus = new byte[0];
        for (Object element$iv : $this$fold$iv) {
            byte[] b = (byte[]) element$iv;
            byte[] acc = bArrPlus;
            bArrPlus = ArraysKt.plus(acc, b);
        }
        byte[] derived = bArrPlus;
        byte[] key = ArraysKt.copyOfRange(derived, 0, keyLen);
        byte[] iv = ArraysKt.copyOfRange(derived, keyLen, keyLen + ivLen);
        return TuplesKt.to(key, iv);
    }

    private final String opensslAesPassphraseDecrypt(String base64Cipher, String passphrase) {
        try {
            byte[] cipherData = Base64.decode(base64Cipher, 0);
            byte[] salted = "Salted__".getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(salted, "getBytes(...)");
            if (cipherData.length >= 16 && Arrays.equals(ArraysKt.copyOfRange(cipherData, 0, 8), salted)) {
                byte[] salt = ArraysKt.copyOfRange(cipherData, 8, 16);
                byte[] enc = ArraysKt.copyOfRange(cipherData, 16, cipherData.length);
                byte[] bytes = passphrase.getBytes(Charsets.UTF_8);
                Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                Pair<byte[], byte[]> pairEvpBytesToKey = evpBytesToKey(bytes, salt, 32, 16);
                byte[] key = (byte[]) pairEvpBytesToKey.component1();
                byte[] iv = (byte[]) pairEvpBytesToKey.component2();
                SecretKeySpec skey = new SecretKeySpec(key, "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(2, skey, ivSpec);
                byte[] plain = cipher.doFinal(enc);
                return new String(plain, Charsets.UTF_8);
            }
            return null;
        } catch (Throwable e) {
            Log.e("DiziLife", "decrypt error: " + e.getMessage());
            return null;
        }
    }
}
