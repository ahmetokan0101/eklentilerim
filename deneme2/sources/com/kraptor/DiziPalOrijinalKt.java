package com.kraptor;

import com.lagradost.cloudstream3.MainAPIKt;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.CharsKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: DiziPalOrijinal.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001¨\u0006\u0006"}, d2 = {"decrypt", "", "passphrase", "saltHex", "ivHex", "ciphertextBase64", "DiziPalOrijinal_debug"}, k = 2, mv = {2, 1, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nDiziPalOrijinal.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DiziPalOrijinal.kt\ncom/kraptor/DiziPalOrijinalKt\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,445:1\n1557#2:446\n1628#2,3:447\n1557#2:450\n1628#2,3:451\n*S KotlinDebug\n*F\n+ 1 DiziPalOrijinal.kt\ncom/kraptor/DiziPalOrijinalKt\n*L\n430#1:446\n430#1:447,3\n431#1:450\n431#1:451,3\n*E\n"})
/* loaded from: classes.dex */
public final class DiziPalOrijinalKt {
    @NotNull
    public static final String decrypt(@NotNull String passphrase, @NotNull String saltHex, @NotNull String ivHex, @NotNull String ciphertextBase64) throws BadPaddingException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        Iterable $this$map$iv = StringsKt.chunked(saltHex, 2);
        Collection destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
        for (Object item$iv$iv : $this$map$iv) {
            String it = (String) item$iv$iv;
            destination$iv$iv.add(Byte.valueOf((byte) Integer.parseInt(it, CharsKt.checkRadix(16))));
        }
        byte[] salt = CollectionsKt.toByteArray((List) destination$iv$iv);
        Iterable $this$map$iv2 = StringsKt.chunked(ivHex, 2);
        Collection destination$iv$iv2 = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv2, 10));
        for (Object item$iv$iv2 : $this$map$iv2) {
            String it2 = (String) item$iv$iv2;
            destination$iv$iv2.add(Byte.valueOf((byte) Integer.parseInt(it2, CharsKt.checkRadix(16))));
        }
        byte[] iv = CollectionsKt.toByteArray((List) destination$iv$iv2);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        char[] charArray = passphrase.toCharArray();
        Intrinsics.checkNotNullExpressionValue(charArray, "toCharArray(...)");
        PBEKeySpec spec = new PBEKeySpec(charArray, salt, 999, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(2, secret, new IvParameterSpec(iv));
        byte[] decoded = MainAPIKt.base64DecodeArray(ciphertextBase64);
        byte[] plaintextBytes = cipher.doFinal(decoded);
        return new String(plaintextBytes, Charsets.UTF_8);
    }
}
