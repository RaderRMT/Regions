package fr.rader.regions.utils;

public class BitReader {

    private final byte bitsPerEntry;    // type of data we read
    private final int bitsPerValue;     // step size is in bits. min = 1, max = 64. maximum is 64 bits per read, so one long per read max
    private final byte[] data;          // data to read

    private int currentValue = 0;       // index of the byte we're reading
    private int bitOffset = 0;          // offset in bits of the current data type

    private int valueIndex = 0;         // something

    private boolean skipEnd = true;     // skip the end of a dataType if dataSize

    public BitReader(long[] data, int bitsPerValue) {
        validateStepSize(data.length, Long.SIZE, bitsPerValue);

        this.data = toByteArray(data);
        this.bitsPerValue = bitsPerValue;
        this.bitsPerEntry = Long.SIZE;
    }

    public BitReader(int[] data, int bitsPerValue) {
        validateStepSize(data.length, Integer.SIZE, bitsPerValue);

        this.data = toByteArray(data);
        this.bitsPerValue = bitsPerValue;
        this.bitsPerEntry = Integer.SIZE;
    }

    public BitReader(short[] data, int bitsPerValue) {
        validateStepSize(data.length, Short.SIZE, bitsPerValue);

        this.data = toByteArray(data);
        this.bitsPerValue = bitsPerValue;
        this.bitsPerEntry = Short.SIZE;
    }

    public BitReader(byte[] data, int bitsPerValue) {
        validateStepSize(data.length, Byte.SIZE, bitsPerValue);

        this.data = data;
        this.bitsPerValue = bitsPerValue;
        this.bitsPerEntry = Byte.SIZE;
    }

    private void validateStepSize(int dataLength, int dataSize, int stepSize) {
        if(stepSize < 1 || stepSize > 64) {
            throw new IllegalArgumentException("stepSize must be between 1 and 64. Provided: " + stepSize);
        }

        if(stepSize > (dataLength * dataSize)) {
            throw new IllegalArgumentException("stepSize cannot be bigger than the size of the array in bits. "
                    + "Provided: " + stepSize + ", it must be smaller or equal to " + (dataLength * dataSize));
        }
    }

    public void jumpToValue(int valueIndex) {
        this.valueIndex = valueIndex;

        if(skipEnd) {
            int unusedBits = bitsPerEntry % bitsPerValue;
            int entriesSkipped = Math.round((valueIndex * bitsPerValue) / (float) bitsPerEntry);
            bitOffset = valueIndex * bitsPerValue + entriesSkipped * unusedBits;
            currentValue = (bitOffset / 8);
            bitOffset %= bitsPerEntry;
        } else {
            currentValue = valueIndex * bitsPerValue / 8;
            bitOffset = valueIndex * bitsPerValue % bitsPerEntry;
        }
    }

    /*public void write(int value) {
        if(bitOffset + bitsPerValue > bitsPerEntry) {
            currentValue++;
            bitOffset = 0;
        }

        for(int i = 0; i < bitsPerValue; i++) {
            writeBit((value >>> i) & 1);
        }
    }*/

    public void skip() {
        read();
    }

    public int read() {
        verifyAndSkip();

        int out = 0;
        for(int i = 0; i < bitsPerValue; i++) {
            out |= readBit() << i;
        }

        valueIndex++;

        return out;
    }

    private void verifyAndSkip() {
        // skip end if we're going to read outside out value
        if(bitOffset + bitsPerValue > bitsPerEntry) {
            if(skipEnd) {
                currentValue++;
                bitOffset = 0;
            }

            if(currentValue + 1 > data.length) {
                throw new IndexOutOfBoundsException("done reading");
            }
        }
    }

    public byte readBit() {
        byte bit = (byte) (((data[currentValue] & 0xff) >> (bitOffset % 8)) & 1);

        bitOffset++;
        if(bitOffset % 8 == 0) {
            currentValue++;

            if(bitOffset % bitsPerEntry == 0) {
                bitOffset = 0;
            }
        }

        return bit;
    }

    public void writeBit(int bit) {
        if(bit == 1) {
            data[currentValue] |= ((1 << bitOffset) & 0xff);
        } else {
            data[currentValue] &= ~((1 << bitOffset) & 0xff);
        }

        bitOffset++;
        if(bitOffset >= 8) {
            currentValue++;
            bitOffset = 0;
        }
    }

    public boolean isDone() {
        boolean lastByteOfArray = (currentValue + 1) >= data.length;
        boolean nextReadExceedsData = (bitOffset % 8) + bitsPerValue >= bitsPerEntry;

        return lastByteOfArray && nextReadExceedsData;
    }

    public boolean doneWithValue() {
        return bitOffset + bitsPerValue >= bitsPerEntry;
    }

    public void setSkipEnd(boolean skip) {
        this.skipEnd = skip;
    }

    private byte[] toByteArray(long[] array) {
        byte[] out = new byte[array.length * 8];

        for(int i = 0; i < array.length; i++) {
            long value = array[i];

            out[i * 8]     = (byte) (value & 0xff);
            out[i * 8 + 1] = (byte) ((value >> 8) & 0xff);
            out[i * 8 + 2] = (byte) ((value >> 16) & 0xff);
            out[i * 8 + 3] = (byte) ((value >> 24) & 0xff);
            out[i * 8 + 4] = (byte) ((value >> 32) & 0xff);
            out[i * 8 + 5] = (byte) ((value >> 40) & 0xff);
            out[i * 8 + 6] = (byte) ((value >> 48) & 0xff);
            out[i * 8 + 7] = (byte) ((value >> 56) & 0xff);
        }

        return out;
    }

    private byte[] toByteArray(int[] array) {
        byte[] out = new byte[array.length * 4];

        for(int i = 0; i < array.length; i++) {
            int value = array[i];

            out[i * 4]     = (byte) (value & 0xff);
            out[i * 4 + 1] = (byte) ((value >> 8) & 0xff);
            out[i * 4 + 2] = (byte) ((value >> 16) & 0xff);
            out[i * 4 + 3] = (byte) ((value >> 24) & 0xff);
        }

        return out;
    }

    private byte[] toByteArray(short[] array) {
        byte[] out = new byte[array.length * 2];

        for(int i = 0; i < array.length; i++) {
            short value = array[i];

            out[i * 2]     = (byte) (value & 0xff);
            out[i * 2 + 1] = (byte) ((value >> 8) & 0xff);
        }

        return out;
    }

    public int getValueIndex() {
        return valueIndex;
    }
}
