package javax.microedition.lcdui;

import android.graphics.Paint;

public class Font {
    public static final int STYLE_PLAIN = 0, STYLE_BOLD = 1, STYLE_ITALIC = 2, STYLE_UNDERLINED = 4;
    public static final int SIZE_SMALL = 8, SIZE_MEDIUM = 0, SIZE_LARGE = 16;
    public static final int FACE_SYSTEM = 0, FACE_MONOSPACE = 32, FACE_PROPORTIONAL = 64;

    private static Font defaultFont = new Font();
    public Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static Font getFont(int face, int style, int size) {
        return defaultFont;
    }

    public static Font getDefaultFont() {
        return defaultFont;
    }

    public int getHeight() { return 14; }
    public int getBaselinePosition() { return 11; }
    public int stringWidth(String str) { return str == null ? 0 : (int) paint.measureText(str); }
    public int charWidth(char ch) { return (int) paint.measureText(new char[]{ch}, 0, 1); }
    public int substringWidth(String str, int offset, int len) {
        return str == null ? 0 : (int) paint.measureText(str, offset, offset + len);
    }
}
