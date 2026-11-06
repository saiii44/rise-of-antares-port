package javax.microedition.lcdui;

public abstract class Displayable {
    protected CommandListener listener;
    protected int width = 240;
    protected int height = 320;

    public void setCommandListener(CommandListener l) { this.listener = l; }
    public void addCommand(Command cmd) {}
    public void removeCommand(Command cmd) {}
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isShown() { return true; }
}
