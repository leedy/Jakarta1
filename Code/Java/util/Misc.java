package util;

public class Misc {
	 // ---- helpers ----
    public static String normalizeUnid(String id) {
        if (id == null) return null;
        String s = id.trim().toUpperCase();
        // Domino UNID is 32 hex chars (A-F, 0-9)
        if (s.length() != 32) return null;
        for (int i = 0; i < 32; i++) {
            char c = s.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
            if (!hex) return null;
        }
        return s;
    }
}
