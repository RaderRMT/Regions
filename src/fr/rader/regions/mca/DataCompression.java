package fr.rader.regions.mca;

import fr.rader.regions.utils.DataReader;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class DataCompression {

    public static final int VERSION_GZIP = 1;
    public static final int VERSION_DEFLATE = 2;

    public static InputStream decompress(RandomAccessFile raf) throws IOException {
        int length = raf.readInt();
        return decompress(raf.readByte(), new DataReader(raf).readFollowingBytes(length));
    }

    public static InputStream decompress(byte compressionType, byte[] data) throws IOException {
        switch(compressionType) {
            case VERSION_GZIP:
                return new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
            case VERSION_DEFLATE:
                return new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)));
            default:
                throw new IllegalStateException("[DataCompression] -> [#decompress(compressionType, data)] Unknown compression type: " + compressionType);
        }
    }
}
