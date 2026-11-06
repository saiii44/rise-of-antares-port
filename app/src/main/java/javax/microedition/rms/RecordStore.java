package javax.microedition.rms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordStore {
    public static File rootDir = new File("/data/data/com.herocraft.antares.port/files/rms");
    private File storeDir;
    private String name;
    private boolean open = true;

    private RecordStore(String name, File storeDir) {
        this.name = name;
        this.storeDir = storeDir;
        if (!storeDir.exists()) storeDir.mkdirs();
    }

    public static RecordStore openRecordStore(String name, boolean createIfNecessary) throws RecordStoreException {
        if (!rootDir.exists()) rootDir.mkdirs();
        File dir = new File(rootDir, name);
        if (!dir.exists() && !createIfNecessary) {
            throw new RecordStoreNotFoundException("Store not found: " + name);
        }
        return new RecordStore(name, dir);
    }

    public void closeRecordStore() {
        open = false;
    }

    public static void deleteRecordStore(String name) {
        File dir = new File(rootDir, name);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) f.delete();
            dir.delete();
        }
    }

    public int getNumRecords() {
        File[] files = storeDir.listFiles((d, f) -> f.endsWith(".rec"));
        return files == null ? 0 : files.length;
    }

    public byte[] getRecord(int recordId) throws RecordStoreException {
        File f = new File(storeDir, recordId + ".rec");
        if (!f.exists()) throw new RecordStoreException("Record not found: " + recordId);
        try (FileInputStream fis = new FileInputStream(f)) {
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            return data;
        } catch (Exception e) {
            throw new RecordStoreException(e.getMessage());
        }
    }

    public int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreException {
        int nextId = getNumRecords() + 1;
        setRecord(nextId, data, offset, numBytes);
        return nextId;
    }

    public void setRecord(int recordId, byte[] data, int offset, int numBytes) throws RecordStoreException {
        File f = new File(storeDir, recordId + ".rec");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            if (data != null && numBytes > 0) {
                fos.write(data, offset, numBytes);
            }
        } catch (Exception e) {
            throw new RecordStoreException(e.getMessage());
        }
    }

    public int getRecordSize(int recordId) {
        File f = new File(storeDir, recordId + ".rec");
        return f.exists() ? (int) f.length() : 0;
    }

    public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated) {
        List<byte[]> records = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        int count = getNumRecords();
        for (int i = 1; i <= count; i++) {
            try {
                byte[] data = getRecord(i);
                if (filter == null || filter.matches(data)) {
                    records.add(data);
                    ids.add(i);
                }
            } catch (Exception ignored) {}
        }

        if (comparator != null && records.size() > 1) {
            // Sort records with ids
            for (int i = 0; i < records.size() - 1; i++) {
                for (int j = i + 1; j < records.size(); j++) {
                    if (comparator.compare(records.get(i), records.get(j)) == RecordComparator.FOLLOWS) {
                        byte[] tr = records.get(i);
                        records.set(i, records.get(j));
                        records.set(j, tr);

                        int tid = ids.get(i);
                        ids.set(i, ids.get(j));
                        ids.set(j, tid);
                    }
                }
            }
        }

        return new SimpleRecordEnumeration(records, ids);
    }

    private static class SimpleRecordEnumeration implements RecordEnumeration {
        private final List<byte[]> records;
        private final List<Integer> ids;
        private int index = 0;

        public SimpleRecordEnumeration(List<byte[]> records, List<Integer> ids) {
            this.records = records;
            this.ids = ids;
        }

        public int numRecords() { return records.size(); }
        public boolean hasNextElement() { return index < records.size(); }
        public boolean hasPreviousElement() { return index > 0; }

        public byte[] nextRecord() throws RecordStoreException {
            if (!hasNextElement()) throw new RecordStoreException("No next record");
            return records.get(index++);
        }

        public int nextRecordId() throws RecordStoreException {
            if (index >= ids.size()) throw new RecordStoreException("No next record");
            return ids.get(index);
        }

        public void reset() { index = 0; }
        public void destroy() { records.clear(); ids.clear(); }
    }
}
