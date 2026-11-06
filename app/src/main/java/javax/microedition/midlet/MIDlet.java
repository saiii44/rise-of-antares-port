package javax.microedition.midlet;

public abstract class MIDlet {
    public static MIDlet instance;

    protected MIDlet() {
        instance = this;
    }

    public abstract void startApp() throws MIDletStateChangeException;
    public abstract void pauseApp();
    public abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

    public final void notifyDestroyed() {}
    public final void notifyPaused() {}
    public final void resumeRequest() {}

    public final String getAppProperty(String key) {
        if ("MIDlet-Name".equals(key)) return "Rise of Antares";
        if ("MIDlet-Vendor".equals(key)) return "HeroCraft";
        if ("MIDlet-Version".equals(key)) return "1.1.1";
        return null;
    }
}
