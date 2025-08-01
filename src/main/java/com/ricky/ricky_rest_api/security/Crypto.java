package com.ricky.ricky_rest_api.security;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

public class Crypto {
	private static final String defaultKey = "2858b4dd4d9e90e713698facaa7a79450a0eb750792759c1c10637d2cad25ecd";

	public static String performEncrypt(String keyText, String plainText) {
		try{
			byte[] key = Hex.decode(keyText.getBytes());
			byte[] ptBytes = plainText.getBytes();
			BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESLightEngine()));
			cipher.init(true, new KeyParameter(key));
			byte[] rv = new byte[cipher.getOutputSize(ptBytes.length)];
			int oLen = cipher.processBytes(ptBytes, 0, ptBytes.length, rv, 0);
			cipher.doFinal(rv, oLen);
			return new String(Hex.encode(rv));
		} catch(Exception e) {
			return "Error";
		}
	}

	public static String performEncrypt(String cryptoText) {
		return performEncrypt(defaultKey, cryptoText);
	}

	public static String performDecrypt(String keyText, String cryptoText) {
		try {
			byte[] key = Hex.decode(keyText.getBytes());
			byte[] cipherText = Hex.decode(cryptoText.getBytes());
			BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESLightEngine()));
			cipher.init(false, new KeyParameter(key));
			byte[] rv = new byte[cipher.getOutputSize(cipherText.length)];
			int oLen = cipher.processBytes(cipherText, 0, cipherText.length, rv, 0);
			cipher.doFinal(rv, oLen);
			return new String(rv).trim();
		} catch(Exception e) {
			return "Error";
		}
	}

	public static String performDecrypt(String cryptoText) {
		return performDecrypt(defaultKey, cryptoText);
	}

	public static void main(String[] args) {
		String strToEncrypt = "Ricky123@";//put text to encrypt in here
		System.out.println("Encryption Result : "+performEncrypt(strToEncrypt));

		String strToDecrypt = "b3fcabb4a0eaca1f79e715d536f255af3eea36c73a7a1e2da35bd1ebeda55aff";//put text to decrypt in here
		System.out.println("Decryption Result : "+performDecrypt(strToDecrypt));
	}
}
