package com.kraptor;

import com.lagradost.cloudstream3.plugins.BasePlugin;
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin;
import kotlin.Metadata;

/* compiled from: DiziPalOrijinalPlugin.kt */
@CloudstreamPlugin
@Metadata(d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016¨\u0006\u0006"}, d2 = {"Lcom/kraptor/DiziPalOrijinalPlugin;", "Lcom/lagradost/cloudstream3/plugins/BasePlugin;", "<init>", "()V", "load", "", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
/* loaded from: classes.dex */
public final class DiziPalOrijinalPlugin extends BasePlugin {
    public void load() {
        registerMainAPI(new DiziPalOrijinal());
        registerExtractorAPI(new ContentX());
        registerExtractorAPI(new Hotlinger());
        registerExtractorAPI(new RapidVid());
        registerExtractorAPI(new TRsTX());
        registerExtractorAPI(new VidMoxy());
        registerExtractorAPI(new Sobreatsesuyp());
        registerExtractorAPI(new TurboImgz());
        registerExtractorAPI(new TurkeyPlayer());
        registerExtractorAPI(new Hotlinger());
        registerExtractorAPI(new FourCX());
        registerExtractorAPI(new PlayRu());
        registerExtractorAPI(new FourPlayRu());
        registerExtractorAPI(new FourPichive());
        registerExtractorAPI(new FourPichiveOnline());
        registerExtractorAPI(new Pichive());
        registerExtractorAPI(new FourDplayer());
        registerExtractorAPI(new SNDplayer());
        registerExtractorAPI(new ORGDplayer());
        registerExtractorAPI(new Dplayer());
        registerExtractorAPI(new VidMolyExtractor());
        registerExtractorAPI(new VidMolyTo());
        registerExtractorAPI(new DonilasPlay());
        registerExtractorAPI(new HotStreamExtractor());
    }
}
