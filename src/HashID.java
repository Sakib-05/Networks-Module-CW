import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class HashID {

	// Compute HashID (SHA-256) and return it as a byte array
	public static byte[] computeHashID(String s) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(s.getBytes(StandardCharsets.UTF_8));
		return md.digest();
	}

	// Convert byte array to a 64-character hexadecimal string
	public static String toHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			hexString.append(String.format("%02x", b));  // Convert each byte to a 2-digit hex
		}
		return hexString.toString();
	}

	// Compute the distance between two HashIDs
	public static int computeDistance(byte[] hash1, byte[] hash2) {
		int matchingBits = 0;

		for (int i = 0; i < hash1.length; i++) {
			int byte1 = hash1[i] & 0xFF;
			int byte2 = hash2[i] & 0xFF;

			int xorResult = byte1 ^ byte2;  // XOR to find differing bits

			if (xorResult == 0) {
				matchingBits += 8;  // All 8 bits match
			} else {
				// Count how many leading bits match in this byte
				for (int j = 7; j >= 0; j--) {
					if ((xorResult & (1 << j)) == 0) {
						matchingBits++;
					} else {
						break;  // Stop at first differing bit
					}
				}
				break; // Stop checking after first differing byte
			}
		}

		return 256 - matchingBits;  // Distance = 256 - matching bits
	}

	public static void main(String[] args) {
		try {
			String key1 = "D:message";
			String key2 = "AnotherKey";

			byte[] hash1 = computeHashID(key1);
			byte[] hash2 = computeHashID(key2);

			System.out.println("HashID for \"" + key1 + "\": " + toHex(hash1));
			System.out.println("HashID for \"" + key2 + "\": " + toHex(hash2));

			String hex1 = toHex(hash1);
			String hex2 = toHex(hash2);

			int count=0;
			for(int i=0;i<hex1.length();i++){
				String c = Character.toString(hex1.charAt(i));
				if(hex2.contains(c)){
					count++;
					hex1.replaceFirst(c,"");
					hex2.replaceFirst(c,"");
				}
			}
			System.out.println("count "+count);

			int distance = computeDistance(hash1, hash2);
			System.out.println("Distance between HashIDs: " + (256-count));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
