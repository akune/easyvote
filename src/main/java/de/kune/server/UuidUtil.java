package de.kune.server;

import java.util.UUID;

public final class UuidUtil {

	private UuidUtil() {
		// Do nothing.
	}

	public static String encodeUuid(final UUID id) {
		final long hi = id.getMostSignificantBits();
		final long lo = id.getLeastSignificantBits();
		return new String(new char[] { (char) ((hi >>> 48) & 0xffff),
				(char) ((hi >>> 32) & 0xffff), (char) ((hi >>> 16) & 0xffff),
				(char) ((hi) & 0xffff), (char) ((lo >>> 48) & 0xffff),
				(char) ((lo >>> 32) & 0xffff), (char) ((lo >>> 16) & 0xffff),
				(char) ((lo) & 0xffff) });
	}

	public static UUID decodeUuid(final String enc) {
		final char[] cs = enc.toCharArray();
		return new UUID((long) cs[0] << 48 | (long) cs[1] << 32
				| (long) cs[2] << 16 | (long) cs[3], (long) cs[4] << 48
				| (long) cs[5] << 32 | (long) cs[6] << 16 | (long) cs[7]);
	}

	// base64url, see: http://tools.ietf.org/html/rfc4648 section 5
	private static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

	/**
	 * Generates a UUID and compresses it into a base 64 character string; this
	 * results in a 22 character string and since each character represents 6
	 * bits of data that means the result can represent up to 132 bits. However,
	 * since a UUID is only 128 bits, 4 additional randomize bits are inserted
	 * into the result (if desired); this means that the number of available
	 * unique IDs is increased by a factor of 16
	 *
	 * @param enhanced
	 *            specifies whether or not to enhance the result with 4
	 *            additional bits of data since a 22 base64 characters can hold
	 *            132 bits of data and a UUID is only 128 bits
	 * @return a 22 character string where each character is from the file and
	 *         url safe base64 character set [A-Za-z0-9-_]
	 */
	public static String getCompressedUuid(boolean enhanced) {
		UUID uuid = UUID.randomUUID();
		return compressLong(uuid.getMostSignificantBits(), enhanced)
				+ compressLong(uuid.getLeastSignificantBits(), enhanced);
	}

	/**
	 * Compress a 64 bit number into 11 6-bit characters
	 * 
	 * @param key
	 * @param enhance
	 * @return
	 */
	private static String compressLong(long key, boolean enhance) {
		// randomize 2 bits as a prefix for the leftmost character which would
		// otherwise only have 4 bits of data in the 6 bits
		long prefix = enhance ? (long) (Math.random() * 4) << 62 : 0;

		// extract the first 6-bit character from the key
		String result = "" + chars.charAt((int) (key & 0x3f));

		// shifting in 2 extra random bits since we have the room
		key = ((key >>> 2) | prefix) >>> 4;

		// iterate thru the next 10 characters
		for (int i = 1; i < 11; i++) {
			// strip off the last 6 bits from the key, look up the matching
			// character
			// and prepend that character to the result
			result = chars.charAt((int) (key & 0x3f)) + result;
			// logical bit shift right so we can isolate the next 6 bits
			key = key >>> 6;
		}

		return result;
	}
}
