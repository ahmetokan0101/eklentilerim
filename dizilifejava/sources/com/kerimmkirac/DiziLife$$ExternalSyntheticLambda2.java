package com.kerimmkirac;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.lagradost.cloudstream3.Episode;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 18, versionHash = "7de78e4c4546435adeebf26ef4b0cc5c4f405847ffd476305b2b34259a231191")
/* loaded from: C:\Users\HazDev\Downloads\classes.dex */
public final /* synthetic */ class DiziLife$$ExternalSyntheticLambda2 implements Function1 {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ int f$3;

    public /* synthetic */ DiziLife$$ExternalSyntheticLambda2(String str, String str2, int r3, int r4) {
        this.f$0 = str;
        this.f$1 = str2;
        this.f$2 = r3;
        this.f$3 = r4;
    }

    public final Object invoke(Object obj) {
        return DiziLife.load$lambda$12$lambda$11(this.f$0, this.f$1, this.f$2, this.f$3, (Episode) obj);
    }
}
