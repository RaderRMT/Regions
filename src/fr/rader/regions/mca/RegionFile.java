package fr.rader.regions.mca;

import fr.rader.regions.nbt.tags.TagCompound;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RegionFile {

    private Chunk[] chunks;

    private int regionX;
    private int regionZ;

    public RegionFile(int regionX, int regionZ) {
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public void deserialize(File file) throws IOException {
        deserialize(new RandomAccessFile(file, "r"));
    }

    public void deserialize(RandomAccessFile raf) throws IOException {
        this.chunks = new Chunk[1024];

        for(int i = 0; i < 1024; i++) {
            raf.seek(i * 4);

            int offset = raf.read() << 16;
            offset |= (raf.read() & 0xff) << 8;
            offset |= raf.read() & 0xff;

            if(raf.readByte() == 0) {
                continue;
            }

            raf.seek(4096 + i * 4);
            int timestamp = raf.readInt();
            raf.seek(4096L * offset);
            Chunk chunk = new Chunk(timestamp);
            chunk.deserialize(raf);

            chunks[i] = chunk;
        }
    }

    public void setBlockStateAt(int x, int y, int z, TagCompound state) {
        createChunkIfMissing(x, z).setBlockStateAt(x, y, z, state);
    }

    private Chunk createChunkIfMissing(int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        Chunk chunk = getChunk(chunkX, chunkZ);

        if(chunk == null) {
            chunk = Chunk.createChunk();
            setChunk(getChunkIndex(chunkX, chunkZ), chunk);
        }

        return chunk;
    }

    public void setChunk(int index, Chunk chunk) {
        checkIndex(index);

        if(chunks == null) {
            chunks = new Chunk[1024];
        }

        chunks[index] = chunk;
    }

    public Chunk getChunk(int index) {
        checkIndex(index);

        if(chunks == null) {
            return null;
        }

        return chunks[index];
    }

    public Chunk getChunk(int x, int z) {
        return getChunk(getChunkIndex(x, z));
    }

    public int getChunkIndex(int x, int z) {
        return (x & 0x1f) + (z & 0x1f) << 0x1f;
    }

    public void checkIndex(int index) {
        if(index < 0 || index > 1023) {
            throw new IndexOutOfBoundsException();
        }
    }

    public void cleanUpBlocksAndPalettes() {
        for(Chunk chunk : chunks) {
            if(chunk != null) {
                chunk.cleanUpBlocksAndPalettes();
            }
        }
    }
}

