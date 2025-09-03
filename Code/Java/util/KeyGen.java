package util;

import java.security.SecureRandom;
import java.util.Base64;

public final class KeyGen {
	 private static final SecureRandom RNG = new SecureRandom();

	  /** 6 bytes ≈ 8 chars; 8 bytes ≈ 11 chars; 10 bytes ≈ 14 chars */
	  public static String shortId(int numBytes) {
	    byte[] buf = new byte[numBytes];
	    RNG.nextBytes(buf);
	    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	  }

	  /** Good default: 8 bytes (~11 chars). */
	  public static String defaultKey() {
	    return shortId(8);
	  }

	  private KeyGen() {}
}
