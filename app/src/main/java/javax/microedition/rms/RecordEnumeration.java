package javax.microedition.rms;

public interface RecordEnumeration {
    int numRecords();
    byte[] nextRecord() throws RecordStoreException;
    int nextRecordId() throws RecordStoreException;
    boolean hasNextElement();
    boolean hasPreviousElement();
    void reset();
    void destroy();
}
