package fr.rader.regions.utils;

public class BitReader {

    private final byte dataTypeSize;    // type of data we read
    private final int stepSize;         // step size is in bits. min = 1, max = 64. maximum is 64 bits per read, so one long per read max
    private final byte[] data;          // data to read

    private int currentValue = 0;       // index of the byte we're reading
    private int bitOffset = 0;          // offset in bits of the current data type

    private boolean skipEnd = true;     // skip the end of a dataType if dataSize

    public BitReader(long[] data, int stepSize) {
        validateStepSize(data.length, Long.SIZE, stepSize);

        this.data = toByteArray(data);
        this.stepSize = stepSize;
        this.dataTypeSize = Long.SIZE;
    }

    public BitReader(int[] data, int stepSize) {
        validateStepSize(data.length, Integer.SIZE, stepSize);

        this.data = toByteArray(data);
        this.stepSize = stepSize;
        this.dataTypeSize = Integer.SIZE;
    }

    public BitReader(short[] data, int stepSize) {
        validateStepSize(data.length, Short.SIZE, stepSize);

        this.data = toByteArray(data);
        this.stepSize = stepSize;
        this.dataTypeSize = Short.SIZE;
    }

    public BitReader(byte[] data, int stepSize) {
        validateStepSize(data.length, Byte.SIZE, stepSize);

        this.data = data;
        this.stepSize = stepSize;
        this.dataTypeSize = Byte.SIZE;
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
        valueIndex *= stepSize;

        // jump to a value where we don't need to skip to the end
        currentValue = valueIndex / 8;
        bitOffset = valueIndex % dataTypeSize;

        // do we need to skip the end?
        if(skipEnd) {
            // if yes, we get the number of bits left per data type
            int bits_left_per_value = dataTypeSize % stepSize;
            // and the values we skipped
            int values_skipped = valueIndex / dataTypeSize;

            // we then add the numbers of bits we need to get to the correct bit offset
            bitOffset += bits_left_per_value * values_skipped;
            if(bitOffset > dataTypeSize) {
                // if the bit offset exceeds the data type size, we then get the byte index at which we should be
                currentValue += bitOffset / dataTypeSize;
                // and we clean the bit offset
                bitOffset %= dataTypeSize;

                // if it's not zero, we pad it to the next value
                if(bitOffset % dataTypeSize != 0) {
                    bitOffset += bits_left_per_value;
                }
            }
        }
    }

    /*public void write(int value) {
        int tempCurrentValue = currentValue;
        int tempBitOffset = bitOffset;

        if(bitOffset + stepSize > dataTypeSize) {
            currentValue++;
            bitOffset = 0;
        }

        for(int i = 0; i < stepSize; i++) {
            writeBit((value >>> i) & 1);
        }

        this.currentValue = tempCurrentValue;
        this.bitOffset = tempBitOffset;
    }*/

    public void skip() {
        read();
    }

    public int read() {
        verifyAndSkip();

        int out = 0;
        for(int i = 0; i < stepSize; i++) {
            out |= readBit() << i;
        }

        return out;
    }

    private void verifyAndSkip() {
        // skip end if we're going to read outside out value
        if(bitOffset + stepSize > dataTypeSize) {
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

            if(bitOffset % dataTypeSize == 0) {
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
        boolean nextReadExceedsData = (bitOffset % 8) + stepSize >= dataTypeSize;

        return lastByteOfArray && nextReadExceedsData;
    }

    public boolean doneWithValue() {
        return bitOffset + stepSize >= dataTypeSize;
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
}
