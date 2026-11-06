package javax.microedition.media;
public interface PlayerListener {
    String STARTED = "started", STOPPED = "stopped", END_OF_MEDIA = "endOfMedia", CLOSED = "closed";
    void playerUpdate(Player player, String event, Object eventData);
}
