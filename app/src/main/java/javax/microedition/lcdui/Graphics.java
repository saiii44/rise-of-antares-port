package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class Graphics {
    public static final int HCENTER = 1, VCENTER = 2, LEFT = 4, RIGHT = 8, TOP = 16, BOTTOM = 32, BASELINE = 64;
    public static final int SOLID = 0, DOTTED = 1;

    public static final int TRANS_NONE = 0;
    public static final int TRANS_MIRROR_ROT180 = 1;
    public static final int TRANS_MIRROR = 2;
    public static final int TRANS_ROT180 = 3;
    public static final int TRANS_MIRROR_ROT270 = 4;
    public static final int TRANS_ROT90 = 5;
    public static final int TRANS_ROT270 = 6;
    public static final int TRANS_MIRROR_ROT90 = 7;

    public Canvas canvas;
    private final Paint paint = new Paint();
    private int currentColor = 0xFF000000;
    private int transX = 0, transY = 0;
    private Font currentFont = Font.getDefaultFont();
    private final Rect clipRect = new Rect(0, 0, 240, 320);
    private int strokeStyle = SOLID;
    private final Matrix transformMatrix = new Matrix();
    private int baseSaveCount = 1;

    public Graphics(Bitmap bitmap) {
        this.canvas = new Canvas(bitmap);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        this.baseSaveCount = canvas.save();
    }

    public Graphics(Canvas canvas) {
        this.canvas = canvas;
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        this.baseSaveCount = canvas.save();
    }

    public void beginFrame() {
        if (canvas.getSaveCount() > baseSaveCount) {
            canvas.restoreToCount(baseSaveCount);
        }
        transX = 0;
        transY = 0;
        clipRect.set(0, 0, 240, 320);
    }

    public void setColor(int RGB) {
        this.currentColor = 0xFF000000 | (RGB & 0x00FFFFFF);
        paint.setColor(currentColor);
    }

    public void setColor(int red, int green, int blue) {
        setColor((red << 16) | (green << 8) | blue);
    }

    public int getColor() { return currentColor & 0x00FFFFFF; }

    public void setStrokeStyle(int style) {
        this.strokeStyle = style;
    }

    public int getStrokeStyle() {
        return strokeStyle;
    }

    public void setClip(int x, int y, int width, int height) {
        clipRect.set(x + transX, y + transY, x + transX + width, y + transY + height);
        if (canvas.getSaveCount() > baseSaveCount) {
            canvas.restoreToCount(baseSaveCount);
        }
        canvas.save();
        if (transX != 0 || transY != 0) {
            canvas.translate(transX, transY);
        }
        canvas.clipRect(x, y, x + width, y + height);
    }

    public void clipRect(int x, int y, int width, int height) {
        clipRect.intersect(x + transX, y + transY, x + transX + width, y + transY + height);
        canvas.clipRect(x, y, x + width, y + height);
    }

    public int getClipX() { return clipRect.left - transX; }
    public int getClipY() { return clipRect.top - transY; }
    public int getClipWidth() { return Math.max(0, clipRect.width()); }
    public int getClipHeight() { return Math.max(0, clipRect.height()); }

    public void translate(int x, int y) {
        transX += x;
        transY += y;
        canvas.translate(x, y);
    }

    public int getTranslateX() { return transX; }
    public int getTranslateY() { return transY; }

    public void fillRect(int x, int y, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        canvas.drawRect(x, y, x + width, y + height, paint);
    }

    public void drawRect(int x, int y, int width, int height) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentColor);
        canvas.drawRect(x, y, x + width, y + height, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentColor);
        canvas.drawRoundRect(new RectF(x, y, x + width, y + height), arcWidth, arcHeight, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        paint.setColor(currentColor);
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    public void drawImage(Image img, int x, int y, int anchor) {
        if (img == null || img.bitmap == null) return;
        int dx = x;
        int dy = y;
        if ((anchor & RIGHT) != 0) dx -= img.getWidth();
        else if ((anchor & HCENTER) != 0) dx -= img.getWidth() / 2;
        if ((anchor & BOTTOM) != 0) dy -= img.getHeight();
        else if ((anchor & VCENTER) != 0) dy -= img.getHeight() / 2;
        canvas.drawBitmap(img.bitmap, dx, dy, paint);
    }

    public void drawRegion(Image src, int x_src, int y_src, int width, int height,
                           int transform, int x_dest, int y_dest, int anchor) {
        if (src == null || src.bitmap == null || width <= 0 || height <= 0) return;

        int destW = width;
        int destH = height;
        if (transform == TRANS_ROT90 || transform == TRANS_ROT270 ||
            transform == TRANS_MIRROR_ROT90 || transform == TRANS_MIRROR_ROT270) {
            destW = height;
            destH = width;
        }

        int dx = x_dest;
        int dy = y_dest;
        if ((anchor & RIGHT) != 0) dx -= destW;
        else if ((anchor & HCENTER) != 0) dx -= destW / 2;
        if ((anchor & BOTTOM) != 0) dy -= destH;
        else if ((anchor & VCENTER) != 0) dy -= destH / 2;

        if (transform == TRANS_NONE) {
            Rect srcRect = new Rect(x_src, y_src, x_src + width, y_src + height);
            Rect destRect = new Rect(dx, dy, dx + width, dy + height);
            canvas.drawBitmap(src.bitmap, srcRect, destRect, paint);
            return;
        }

        transformMatrix.reset();
        switch (transform) {
            case TRANS_ROT90:
                transformMatrix.postRotate(90);
                transformMatrix.postTranslate(destW, 0);
                break;
            case TRANS_ROT180:
                transformMatrix.postRotate(180);
                transformMatrix.postTranslate(destW, destH);
                break;
            case TRANS_ROT270:
                transformMatrix.postRotate(270);
                transformMatrix.postTranslate(0, destH);
                break;
            case TRANS_MIRROR:
                transformMatrix.postScale(-1, 1);
                transformMatrix.postTranslate(destW, 0);
                break;
            case TRANS_MIRROR_ROT180:
                transformMatrix.postScale(1, -1);
                transformMatrix.postTranslate(0, destH);
                break;
            case TRANS_MIRROR_ROT90:
                transformMatrix.postScale(-1, 1);
                transformMatrix.postRotate(90);
                transformMatrix.postTranslate(destW, 0);
                break;
            case TRANS_MIRROR_ROT270:
                transformMatrix.postScale(-1, 1);
                transformMatrix.postRotate(270);
                transformMatrix.postTranslate(0, destH);
                break;
        }
        transformMatrix.postTranslate(dx, dy);

        canvas.save();
        canvas.clipRect(dx, dy, dx + destW, dy + destH);
        canvas.concat(transformMatrix);
        Rect srcRect = new Rect(x_src, y_src, x_src + width, y_src + height);
        Rect localDest = new Rect(0, 0, width, height);
        canvas.drawBitmap(src.bitmap, srcRect, localDest, paint);
        canvas.restore();
    }

    public void drawString(String str, int x, int y, int anchor) {
        if (str == null) return;
        currentFont.paint.setColor(currentColor);
        int dx = x;
        int dy = y;
        if ((anchor & RIGHT) != 0) dx -= currentFont.stringWidth(str);
        else if ((anchor & HCENTER) != 0) dx -= currentFont.stringWidth(str) / 2;
        if ((anchor & TOP) != 0) dy += currentFont.getBaselinePosition();
        else if ((anchor & BOTTOM) != 0) dy -= (currentFont.getHeight() - currentFont.getBaselinePosition());
        canvas.drawText(str, dx, dy, currentFont.paint);
    }

    public void setFont(Font font) {
        if (font != null) this.currentFont = font;
    }

    public Font getFont() { return currentFont; }
}
