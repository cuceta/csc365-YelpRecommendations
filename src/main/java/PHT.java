import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

class Bucket {
    static final int SLEN = 32; // max string length for keys and vals
    static final int BLOCKSIZE = 4096;
    static final int ENTRYWIDTH = SLEN + SLEN;
    static final int MAX_COUNT = 63;
    static final int LONG_WIDTH = 8;
    static final int INT_WIDTH = 4;
    long pos;
    int mask;
    int count;
    String[] keys = new String[MAX_COUNT];
    String[] vals = new String[MAX_COUNT];
    static final int POS_INDEX = 0;
    static final int MASK_INDEX = POS_INDEX + LONG_WIDTH;
    static final int COUNT_INDEX = MASK_INDEX + INT_WIDTH;
    static final int FIRST_ENTRY_INDEX = COUNT_INDEX + INT_WIDTH;
    static int keyIndex(int i) { return FIRST_ENTRY_INDEX + i * ENTRYWIDTH; }
    static int valIndex(int i) { return keyIndex(i) + SLEN; }

    void read(ByteBuffer b) throws UnsupportedEncodingException {
        pos = b.getLong();
        mask = b.getInt();
        count = b.getInt();
        for (int i = 0; i < MAX_COUNT; ++i) {
            byte[] kb = new byte[SLEN], vb = new byte[SLEN];
            b.get(kb, 0, SLEN);
            keys[i] = new String(kb, "UTF-8");
            b.get(vb, 0, SLEN);
            vals[i] = new String(vb, "UTF-8");
        }
    }

    String get(String key) {
        for (int j = 0; j < count; ++j) {
            if (key.equals(keys[j]))
                return vals[j];
        }
        return null;
    }
    String getKey(String value) {
        for (int j = 0; j < count; ++j) {
            if (value.equals(vals[j]))
                return keys[j];
        }
        return null;
    }
}

class IndexArray implements Serializable {
    long[] index;
    int size;

    long getBucketPosition(String key) {
        return index[(key.hashCode() & (size - 1))];
    }
}

class PHT {
    static final String bucketFile = "BUCKETS";
    IndexArray indexArray;

    PHT(boolean created) throws IOException, ClassNotFoundException {
        if (created)
            indexArray = (IndexArray) new ObjectInputStream(new FileInputStream("INDEX")).readObject();
        else {
            indexArray = new IndexArray();
            indexArray.index = new long[200000000]; // Initial size
        }
    }
    void put(String key, String value) throws IOException, ClassNotFoundException {
        long bucketPosition = indexArray.getBucketPosition(key);
        ByteBuffer buffer = ByteBuffer.allocate(Bucket.BLOCKSIZE);
        try (RandomAccessFile raf = new RandomAccessFile(bucketFile, "rw")) {
            raf.seek(bucketPosition);
            raf.getChannel().read(buffer);
            buffer.flip();
            Bucket bucket = new Bucket();
            bucket.read(buffer);

            // Check if the key already exists in the bucket
            for (int i = 0; i < bucket.count; i++) {
                if (bucket.keys[i].equals(key)) {
                    bucket.vals[i] = value;
                    writeBucket(raf, bucketPosition, bucket);
                    return;
                }
            }

            // If the bucket is full, handle overflow using chaining
            if (bucket.count >= Bucket.MAX_COUNT) {
                // Create a new bucket for chaining
                Bucket newBucket = new Bucket();
                newBucket.keys[0] = key;
                newBucket.vals[0] = value;
                newBucket.count = 1;

                // Find an empty slot in the file for the new bucket
                long newPosition = findEmptyBucketPosition(raf);

                // Write the new bucket to the empty slot
                writeBucket(raf, newPosition, newBucket);

                // Update the current bucket's mask to point to the new bucket
                bucket.mask |= 1 << (Integer.bitCount(bucket.mask));

                writeBucket(raf, bucketPosition, bucket);
                return;
            }

            // If the key doesn't exist and there's space in the bucket, add it
            bucket.keys[bucket.count] = key;
            bucket.vals[bucket.count] = value;
            bucket.count++;

            writeBucket(raf, bucketPosition, bucket);
        }
    }

    String getValue(String key) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(bucketFile, "r")) {
            long bucketPosition = indexArray.getBucketPosition(key);
            ByteBuffer buffer = ByteBuffer.allocate(Bucket.BLOCKSIZE);
            raf.seek(bucketPosition);
            raf.getChannel().read(buffer);
            buffer.flip();
            Bucket bucket = new Bucket();
            bucket.read(buffer);
            return bucket.get(key);
        }
    }

    String getKey(String value) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(bucketFile, "r")) {
            long bucketPosition = indexArray.getBucketPosition(value);
            ByteBuffer buffer = ByteBuffer.allocate(Bucket.BLOCKSIZE);
            raf.seek(bucketPosition);
            raf.getChannel().read(buffer);
            buffer.flip();
            Bucket bucket = new Bucket();
            bucket.read(buffer);
            return bucket.getKey(value);
        }
    }

    Collection<String> values() throws IOException {
        Collection<String> allValues = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(bucketFile, "r")) {
            for (long position : indexArray.index) {
                ByteBuffer buffer = ByteBuffer.allocate(Bucket.BLOCKSIZE);
                raf.seek(position);
                raf.getChannel().read(buffer);
                buffer.flip();
                Bucket bucket = new Bucket();
                bucket.read(buffer);
                for (int i = 0; i < bucket.count; i++) {
                    allValues.add(bucket.vals[i]);
                }
            }
        }
        return allValues;
    }

    private void writeBucket(RandomAccessFile raf, long position, Bucket bucket) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Bucket.BLOCKSIZE);
        buffer.putLong(bucket.pos);
        buffer.putInt(bucket.mask);
        buffer.putInt(bucket.count);
        for (int j = 0; j < Bucket.MAX_COUNT; j++) {
            buffer.put(bucket.keys[j].getBytes("UTF-8"));
            buffer.put(bucket.vals[j].getBytes("UTF-8"));
        }
        buffer.flip();
        raf.seek(position);
        raf.getChannel().write(buffer);
    }

    private long findEmptyBucketPosition(RandomAccessFile raf) throws IOException {
        long currentPosition = raf.getFilePointer();
        long position = currentPosition;
        ByteBuffer buffer = ByteBuffer.allocate(Bucket.BLOCKSIZE);
        while (raf.read(buffer.array()) != -1) {
            buffer.flip();
            Bucket bucket = new Bucket();
            bucket.read(buffer);
            if (bucket.count < Bucket.MAX_COUNT) {
                raf.seek(position);
                return position;
            }
            position = raf.getFilePointer();
            buffer.clear();
        }
        return currentPosition;
    }
}