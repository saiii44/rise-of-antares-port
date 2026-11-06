package javax.microedition.media;

public interface Player {
    int UNREALIZED = 100;
    int REALIZED = 200;
    int PREFETCHED = 300;
    int STARTED = 400;
    int CLOSED = 0;

    void realize() throws MediaException;
    void prefetch() throws MediaException;
    void start() throws MediaException;
    void stop() throws MediaException;
    void close();
    void deallocate();
    long setMediaTime(long now) throws MediaException;
    void setLoopCount(int count);
    int getState();
    void addPlayerListener(PlayerListener playerListener);
    void removePlayerListener(PlayerListener playerListener);
    Control getControl(String controlType);
}
