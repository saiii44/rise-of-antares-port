package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import java.io.InputStream;

public class Image {
    public Bitmap bitmap;
    private Graphics graphics;

    public Image(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public static Image createImage(int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return new Image(b);
    }

    public static Image createImage(String name) throws java.io.IOException {
        String cleanName = name.startsWith("/") ? name.substring(1) : name;
        InputStream is = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            is = cl.getResourceAsStream(cleanName);
            if (is == null) is = cl.getResourceAsStream("/" + cleanName);
        }
        if (is == null) {
            is = Image.class.getResourceAsStream("/" + cleanName);
            if (is == null) is = Image.class.getResourceAsStream(cleanName);
        }
        if (is == null) {
            throw new java.io.IOException("Resource not found: " + name);
        }
        return createImage(is);
    }

    public static Image createImage(InputStream is) throws java.io.IOException {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inScaled = false;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap b = BitmapFactory.decodeStream(is, null, opts);
            if (b == null) throw new java.io.IOException("Failed to decode image stream");
            return new Image(b);
        } finally {
            try { is.close(); } catch (Exception ignored) {}
        }
    }

    public static Image createImage(byte[] imageData, int imageOffset, int imageLength) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap b = BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength, opts);
        return new Image(b);
    }

    public static Image createImage(Image image, int x, int y, int width, int height, int transform) {
        if (image == null || image.bitmap == null) return null;
        if (transform == Graphics.TRANS_NONE) {
            Bitmap sub = Bitmap.createBitmap(image.bitmap, x, y, width, height);
            return new Image(sub);
        }

        Matrix m = new Matrix();
        switch (transform) {
            case Graphics.TRANS_ROT90: m.postRotate(90); break;
            case Graphics.TRANS_ROT180: m.postRotate(180); break;
            case Graphics.TRANS_ROT270: m.postRotate(270); break;
            case Graphics.TRANS_MIRROR: m.postScale(-1, 1); break;
            case Graphics.TRANS_MIRROR_ROT180: m.postScale(1, -1); break;
            case Graphics.TRANS_MIRROR_ROT90: m.postScale(-1, 1); m.postRotate(90); break;
            case Graphics.TRANS_MIRROR_ROT270: m.postScale(-1, 1); m.postRotate(270); break;
        }
        Bitmap sub = Bitmap.createBitmap(image.bitmap, x, y, width, height, m, false);
        return new Image(sub);
    }

    public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        Bitmap b = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
        return new Image(b);
    }

    public Graphics getGraphics() {
        if (graphics == null) {
            if (!bitmap.isMutable()) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
            graphics = new Graphics(bitmap);
        }
        return graphics;
    }

    public int getWidth() { return bitmap.getWidth(); }
    public int getHeight() { return bitmap.getHeight(); }
    public boolean isMutable() { return bitmap.isMutable(); }

    public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
        bitmap.getPixels(rgbData, offset, scanlength, x, y, width, height);
    }
}
