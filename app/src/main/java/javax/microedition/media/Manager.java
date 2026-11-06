package javax.microedition.media;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.microedition.media.control.VolumeControl;

public class Manager {
    private static final String TAG = "AntaresMidiManager";

    public static Player createPlayer(InputStream stream, String type) throws java.io.IOException, MediaException {
        if (stream == null) {
            throw new IllegalArgumentException("Stream cannot be null");
        }

        try {
            File tempMidi = File.createTempFile("antares_track_", ".mid");
            tempMidi.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempMidi)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            Log.i(TAG, "Extracted MIDI stream to: " + tempMidi.getAbsolutePath() + " (" + tempMidi.length() + " bytes)");

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            );
            mediaPlayer.setDataSource(tempMidi.getAbsolutePath());
            return new AndroidMidiPlayer(mediaPlayer, tempMidi);
        } catch (Exception e) {
            Log.e(TAG, "Error creating MIDI player", e);
            throw new MediaException("MIDI playback creation failed: " + e.getMessage());
        }
    }

    private static class AndroidMidiPlayer implements Player, VolumeControl {
        private final MediaPlayer mediaPlayer;
        private final File tempFile;
        private int state = UNREALIZED;
        private PlayerListener listener;
        private float currentVolume = 0.85f;

        public AndroidMidiPlayer(MediaPlayer mp, File file) {
            this.mediaPlayer = mp;
            this.tempFile = file;
        }

        public void realize() {
            state = REALIZED;
        }

        public void prefetch() throws MediaException {
            try {
                mediaPlayer.prepare();
                state = PREFETCHED;
                Log.i(TAG, "MIDI Player prepared and prefetched successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error prefetching MIDI", e);
                throw new MediaException(e.getMessage());
            }
        }

        public void start() throws MediaException {
            try {
                if (state < PREFETCHED) {
                    prefetch();
                }
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(currentVolume, currentVolume);
                mediaPlayer.start();
                state = STARTED;
                Log.i(TAG, "MIDI Player started playing track");
                if (listener != null) {
                    listener.playerUpdate(this, PlayerListener.STARTED, null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error starting MIDI playback", e);
                throw new MediaException(e.getMessage());
            }
        }

        public void stop() {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                state = PREFETCHED;
                if (listener != null) {
                    listener.playerUpdate(this, PlayerListener.STOPPED, null);
                }
            } catch (Exception ignored) {}
        }

        public void deallocate() {
            stop();
            state = UNREALIZED;
        }

        public long setMediaTime(long now) {
            try {
                int ms = (int) (now / 1000);
                mediaPlayer.seekTo(ms);
                return now;
            } catch (Exception e) {
                return 0;
            }
        }

        public void close() {
            try {
                mediaPlayer.release();
                tempFile.delete();
                state = CLOSED;
                if (listener != null) {
                    listener.playerUpdate(this, PlayerListener.CLOSED, null);
                }
            } catch (Exception ignored) {}
        }

        public void setLoopCount(int count) {
            mediaPlayer.setLooping(count != 1);
        }

        public int getState() {
            return state;
        }

        public void addPlayerListener(PlayerListener pl) {
            this.listener = pl;
        }

        public void removePlayerListener(PlayerListener pl) {
            if (this.listener == pl) {
                this.listener = null;
            }
        }

        public Control getControl(String controlType) {
            if ("VolumeControl".equals(controlType)) {
                return this;
            }
            return null;
        }

        public int setLevel(int level) {
            currentVolume = Math.max(0f, Math.min(1f, level / 100f));
            try {
                mediaPlayer.setVolume(currentVolume, currentVolume);
            } catch (Exception ignored) {}
            return level;
        }

        public int getLevel() {
            return (int) (currentVolume * 100);
        }

        public void setMute(boolean mute) {
            try {
                float v = mute ? 0f : currentVolume;
                mediaPlayer.setVolume(v, v);
            } catch (Exception ignored) {}
        }

        public boolean isMuted() {
            return currentVolume == 0f;
        }
    }
}
