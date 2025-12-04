package com.kerimmkirac;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.lagradost.cloudstream3.MovieSearchResponse;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 18, versionHash = "7de78e4c4546435adeebf26ef4b0cc5c4f405847ffd476305b2b34259a231191")
/* loaded from: C:\Users\HazDev\Downloads\classes.dex */
public final /* synthetic */ class DiziLife$$ExternalSyntheticLambda1 implements Function1 {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ Double f$1;

    public /* synthetic */ DiziLife$$ExternalSyntheticLambda1(String str, Double d) {
        this.f$0 = str;
        this.f$1 = d;
    }

    public final Object invoke(Object obj) {
        return DiziLife.toMainPageResult$lambda$2(this.f$0, this.f$1, (MovieSearchResponse) obj);
    }
}
