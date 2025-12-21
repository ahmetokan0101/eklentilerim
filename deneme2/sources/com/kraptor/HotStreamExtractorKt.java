package com.kraptor;

import com.lagradost.cloudstream3.MainAPIKt;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

/* compiled from: HotStreamExtractor.kt */
@Metadata(d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0002\u001a(\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0002\u001a\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0001H\u0002¨\u0006\t"}, d2 = {"decryptAES", "", "ct", "password", "ivHex", "saltHex", "hexToBytes", "", "hex", "DiziPalOrijinal_debug"}, k = 2, mv = {2, 1, 0}, xi = 48)
/* loaded from: classes.dex */
public final class HotStreamExtractorKt {
    /* JADX INFO: Access modifiers changed from: private */
    public static final String decryptAES(String ct, String password, String ivHex, String saltHex) throws BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] salt = hexToBytes(saltHex);
        byte[] passwordBytes = password.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(passwordBytes, "getBytes(...)");
        MessageDigest md5_1 = MessageDigest.getInstance("MD5");
        md5_1.update(passwordBytes);
        md5_1.update(salt);
        byte[] d1 = md5_1.digest();
        MessageDigest md5_2 = MessageDigest.getInstance("MD5");
        md5_2.update(d1);
        md5_2.update(passwordBytes);
        md5_2.update(salt);
        byte[] d2 = md5_2.digest();
        byte[] key = ArraysKt.plus(d1, d2);
        byte[] iv = hexToBytes(ivHex);
        byte[] ciphertext = MainAPIKt.base64DecodeArray(ct);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(2, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted, Charsets.UTF_8);
    }

    private static final byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
