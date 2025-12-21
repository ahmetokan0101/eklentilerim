package com.kraptor;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: HotStreamExtractor.kt */
@Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003HÆ\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\r\u001a\u00020\u000eHÖ\u0001J\t\u0010\u000f\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u0010"}, d2 = {"Lcom/kraptor/VideoData;", "", "video_location", "", "<init>", "(Ljava/lang/String;)V", "getVideo_location", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "DiziPalOrijinal_debug"}, k = 1, mv = {2, 1, 0}, xi = 48)
/* loaded from: classes.dex */
public final /* data */ class VideoData {

    @NotNull
    private final String video_location;

    public static /* synthetic */ VideoData copy$default(VideoData videoData, String str, int r2, Object obj) {
        if ((r2 & 1) != 0) {
            str = videoData.video_location;
        }
        return videoData.copy(str);
    }

    @NotNull
    /* renamed from: component1, reason: from getter */
    public final String getVideo_location() {
        return this.video_location;
    }

    @NotNull
    public final VideoData copy(@NotNull String video_location) {
        return new VideoData(video_location);
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        return (other instanceof VideoData) && Intrinsics.areEqual(this.video_location, ((VideoData) other).video_location);
    }

    public int hashCode() {
        return this.video_location.hashCode();
    }

    @NotNull
    public String toString() {
        return "VideoData(video_location=" + this.video_location + ')';
    }

    public VideoData(@NotNull String video_location) {
        this.video_location = video_location;
    }

    @NotNull
    public final String getVideo_location() {
        return this.video_location;
    }
}
