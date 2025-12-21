package com.kraptor;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: HotStreamExtractor.kt */
@Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000e\u001a\u00020\u0003HÆ\u0003J'\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0013\u001a\u00020\u0014HÖ\u0001J\t\u0010\u0015\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t¨\u0006\u0016"}, d2 = {"Lcom/kraptor/Sifre;", "", "ct", "", "iv", "s", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getCt", "()Ljava/lang/String;", "getIv", "getS", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
/* loaded from: classes.dex */
public final /* data */ class Sifre {

    @NotNull
    private final String ct;

    @NotNull
    private final String iv;

    @NotNull
    private final String s;

    public static /* synthetic */ Sifre copy$default(Sifre sifre, String str, String str2, String str3, int r4, Object obj) {
        if ((r4 & 1) != 0) {
            str = sifre.ct;
        }
        if ((r4 & 2) != 0) {
            str2 = sifre.iv;
        }
        if ((r4 & 4) != 0) {
            str3 = sifre.s;
        }
        return sifre.copy(str, str2, str3);
    }

    @NotNull
    /* renamed from: component1, reason: from getter */
    public final String getCt() {
        return this.ct;
    }

    @NotNull
    /* renamed from: component2, reason: from getter */
    public final String getIv() {
        return this.iv;
    }

    @NotNull
    /* renamed from: component3, reason: from getter */
    public final String getS() {
        return this.s;
    }

    @NotNull
    public final Sifre copy(@NotNull String ct, @NotNull String iv, @NotNull String s) {
        return new Sifre(ct, iv, s);
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Sifre)) {
            return false;
        }
        Sifre sifre = (Sifre) other;
        return Intrinsics.areEqual(this.ct, sifre.ct) && Intrinsics.areEqual(this.iv, sifre.iv) && Intrinsics.areEqual(this.s, sifre.s);
    }

    public int hashCode() {
        return (((this.ct.hashCode() * 31) + this.iv.hashCode()) * 31) + this.s.hashCode();
    }

    @NotNull
    public String toString() {
        return "Sifre(ct=" + this.ct + ", iv=" + this.iv + ", s=" + this.s + ')';
    }

    public Sifre(@NotNull String ct, @NotNull String iv, @NotNull String s) {
        this.ct = ct;
        this.iv = iv;
        this.s = s;
    }

    @NotNull
    public final String getCt() {
        return this.ct;
    }

    @NotNull
    public final String getIv() {
        return this.iv;
    }

    @NotNull
    public final String getS() {
        return this.s;
    }
}
