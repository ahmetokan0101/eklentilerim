package com.kerimmkirac;

import android.content.Context;
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin;
import com.lagradost.cloudstream3.plugins.Plugin;
import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: DiziLifePlugin.kt */
@CloudstreamPlugin
@Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016¨\u0006\b"}, d2 = {"Lcom/kerimmkirac/DiziLifePlugin;", "Lcom/lagradost/cloudstream3/plugins/Plugin;", "<init>", "()V", "load", "", "context", "Landroid/content/Context;", "DiziLife_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
/* loaded from: C:\Users\HazDev\Downloads\classes.dex */
public final class DiziLifePlugin extends Plugin {
    public void load(@NotNull Context context) {
        registerMainAPI(new DiziLife());
    }
}
