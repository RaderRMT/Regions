package fr.rader.regions.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DataWriter {

    private ByteArrayInOutStream stream;

    private byte[] buffer = new byte[16384];
    private int index = 0;

    public DataWriter() {
        stream = new ByteArrayInOutStream(1);
    }

    public int getIndex() {
        return index;
    }

    public void writeByte(int value) {
        if(index == buffer.length) {
            stream.write(buffer, 0, index);
            index = 0;
        }

        buffer[index] = (byte) (value & 0xff);
        index++;
    }

    public void writeShort(int value) {
        writeByte(value >>> 8);
        writeByte(value & 0xff);
    }

    public void writeInt(int value) {
        writeShort(value >>> 16);
        writeShort(value & 0xffff);
    }

    public void writeLong(long value) {
        writeInt((int) (value >>> 32));
        writeInt((int) value);
    }

    public void writeByteArray(byte[] values) {
        for(byte value : values) {
            writeByte(value);
        }
    }

    public void writeIntArray(int[] values) {
        for(int value : values) {
            writeInt(value);
        }
    }

    public void writeLongArray(long[] values) {
        for(long value : values) {
            writeLong(value);
        }
    }

    public void writeFloat(float value) {
        writeByteArray(ByteBuffer.allocate(4).putFloat(value).array());
    }

    public void writeDouble(double value) {
        writeByteArray(ByteBuffer.allocate(8).putDouble(value).array());
    }

    public void writeBoolean(boolean value) {
        writeByte(value ? 0x01 : 0x00);
    }

    public void writeVarInt(int value) {
        do {
            byte temp = (byte) (value & 0x7f);
            value >>>= 7;

            if(value != 0) {
                temp |= 0x80;
            }

            writeByte(temp);
        } while (value != 0);
    }

    public void writeVarLong(long value) {
        do {
            byte temp = (byte) (value & 0x7f);
            value >>>= 7;

            if(value != 0) {
                temp |= 0x80;
            }

            writeByte(temp);
        } while (value != 0);
    }

    public void writeString(String value) {
        writeByteArray(value.getBytes(StandardCharsets.UTF_8));
    }

    public void writeAsciiChar(char value) {
        writeByte(value);
    }

    public void writeUUID(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public byte[] getData() {
        flush();

        return stream.getBufferData();
    }

    public InputStream getInputStream() {
        flush();

        return stream.getInputStream();
    }

    public OutputStream getStream() {
        flush();

        return stream;
    }

    public void closeStream() {
        flush();

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flush() {
        try {
            stream.write(buffer, 0, index);
            stream.flush();
            index = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
