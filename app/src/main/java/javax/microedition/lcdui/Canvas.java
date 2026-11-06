package javax.microedition.lcdui;

public abstract class Canvas extends Displayable {
    public static final int UP = 1, DOWN = 6, LEFT = 2, RIGHT = 5, FIRE = 8;
    public static final int GAME_A = 9, GAME_B = 10, GAME_C = 11, GAME_D = 12;

    public static final int KEY_NUM0 = 48, KEY_NUM1 = 49, KEY_NUM2 = 50, KEY_NUM3 = 51;
    public static final int KEY_NUM4 = 52, KEY_NUM5 = 53, KEY_NUM6 = 54, KEY_NUM7 = 55;
    public static final int KEY_NUM8 = 56, KEY_NUM9 = 57, KEY_STAR = 42, KEY_POUND = 35;

    public static Canvas activeCanvas;

    public static int screenWidth = 240;
    public static int screenHeight = 320;

    protected Canvas() {
        activeCanvas = this;
        this.width = screenWidth;
        this.height = screenHeight;
    }

    public int getWidth() { return screenWidth; }
    public int getHeight() { return screenHeight; }

    public int getGameAction(int keyCode) {
        switch (keyCode) {
            case 50: case 1: case -1: return UP;     // '2' or UP
            case 56: case 6: case -2: return DOWN;   // '8' or DOWN
            case 52: case 2: case -3: return LEFT;   // '4' or LEFT
            case 54: case 5: case -4: return RIGHT;  // '6' or RIGHT
            case 53: case 8: case -5: return FIRE;   // '5' or FIRE
            default: return 0;
        }
    }

    public int getKeyCode(int gameAction) {
        switch (gameAction) {
            case UP: return 50;
            case DOWN: return 56;
            case LEFT: return 52;
            case RIGHT: return 54;
            case FIRE: return 53;
            default: return 0;
        }
    }

    public String getKeyName(int keyCode) {
        return "KEY_" + keyCode;
    }

    public void repaint() {}
    public void repaint(int x, int y, int width, int height) {}
    public void serviceRepaints() {}

    public abstract void paint(Graphics g);

    public void keyPressed(int keyCode) {}
    public void keyReleased(int keyCode) {}
    public void keyRepeated(int keyCode) {}

    public void showNotify() {}
    public void hideNotify() {}

    public void pointerPressed(int x, int y) {}
    public void pointerReleased(int x, int y) {}
    public void pointerDragged(int x, int y) {}

    public boolean hasPointerEvents() { return true; }
    public boolean isDoubleBuffered() { return true; }
    public void setFullScreenMode(boolean mode) {}
}
