package com.github.is.jingxuan1990.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Encrypt and decrypt messages using AES 256 bit encryption that are compatible with AESCrypt-ObjC
 * and AESCrypt Ruby.
 *
 * @author hzliwenhao
 */
public final class AesCrypt {

  /**
   * AESCrypt-ObjC uses CBC and PKCS7Padding
   */
  private static final String AES_MODE = "AES/CBC/PKCS5Padding";
  private static final String CHARSET = "UTF-8";

  /**
   * AESCrypt-ObjC uses SHA-256 (and so a 256-bit key)
   */
  private static final String HASH_ALGORITHM = "MD5";

  /**
   * AESCrypt-ObjC uses blank IV (not the best security, but the aim here is compatibility)
   */
  private static final byte[] IV_BYTES = {
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00};

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * Generates SHA256 hash of the password which is used as key
   *
   * @param password used to generated key
   * @return SHA256 of the password
   */
  private static SecretKeySpec generateKey(final String password)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
    byte[] bytes = password.getBytes("UTF-8");
    digest.update(bytes, 0, bytes.length);
    byte[] key = digest.digest();

    return new SecretKeySpec(key, "AES");
  }

  /**
   * Encrypt and encode message using 256-bit AES with key generated from password.
   *
   * @param password used to generated key
   * @param message the thing you want to encrypt assumed String UTF-8
   * @return Base64 encoded CipherText
   * @throws GeneralSecurityException if problems occur during encryption
   */
  public static String encrypt(final String password, String message)
      throws GeneralSecurityException {

    try {
      final SecretKeySpec key = generateKey(password);

      byte[] cipherText = encrypt(key, IV_BYTES, message.getBytes(CHARSET));

      // NO_WRAP is important as was getting \n at the end
      return Base64.encodeBase64String(cipherText);
    } catch (UnsupportedEncodingException e) {
      throw new GeneralSecurityException(e);
    }
  }

  /**
   * More flexible AES encrypt that doesn't encode
   *
   * @param key AES key typically 128, 192 or 256 bit
   * @param iv Initiation Vector
   * @param message in bytes (assumed it's already been decoded)
   * @return Encrypted cipher text (not encoded)
   * @throws GeneralSecurityException if something goes wrong during encryption
   */
  public static byte[] encrypt(final SecretKeySpec key, final byte[] iv, final byte[] message)
      throws GeneralSecurityException {
    final Cipher cipher = Cipher.getInstance(AES_MODE);
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
    return cipher.doFinal(message);
  }

  /**
   * Decrypt and decode ciphertext using 128-bit AES with key generated from password
   *
   * @param password used to generated key
   * @param base64EncodedCipherText the encrpyted message encoded with base64
   * @return message in Plain text (String UTF-8)
   * @throws GeneralSecurityException if there's an issue decrypting
   */
  public static String decrypt(final String password, String base64EncodedCipherText)
      throws GeneralSecurityException {

    try {
      final SecretKeySpec key = generateKey(password);

      byte[] decodedCipherText = Base64.decodeBase64(base64EncodedCipherText);

      byte[] decryptedBytes = decrypt(key, IV_BYTES, decodedCipherText);

      return new String(decryptedBytes, CHARSET);
    } catch (UnsupportedEncodingException e) {
      throw new GeneralSecurityException(e);
    }
  }

  /**
   * More flexible AES decrypt that doesn't encode
   *
   * @param key AES key typically 128, 192 or 256 bit
   * @param iv Initiation Vector
   * @param decodedCipherText in bytes (assumed it's already been decoded)
   * @return Decrypted message cipher text (not encoded)
   * @throws GeneralSecurityException if something goes wrong during encryption
   */
  public static byte[] decrypt(final SecretKeySpec key, final byte[] iv,
      final byte[] decodedCipherText)
      throws GeneralSecurityException {
    final Cipher cipher = Cipher.getInstance(AES_MODE);
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
    return cipher.doFinal(decodedCipherText);
  }
}