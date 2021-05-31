package fr.rader.regions.mca;

import fr.rader.regions.nbt.tags.TagBase;
import fr.rader.regions.nbt.tags.TagCompound;
import fr.rader.regions.nbt.tags.TagList;
import fr.rader.regions.nbt.tags.TagString;
import fr.rader.regions.utils.BitReader;

public class Section {

    private TagCompound data;
    public TagList<TagCompound> palette;

    private long[] blockStates;
    private byte[] blockLight;
    private byte[] skyLight;

    // tells bob if the chunk has to be rebuilt because the palette is 1 bit bigger
    // when we move from 15 to 16 blocks in the chunk (so palette.size() == 15 to 16),
    // the amount of bits used to count every block changes, it goes from 4 to 5:
    // 0b00001111 -> 15 (4 bits)
    // 0b00010000 -> 16 (5 bits)
    // every blocks will then have to be rebuilt because they cannot index the new block
    private boolean needsRebuilding = false;

    public Section(TagCompound dataRoot) {
        this.data = dataRoot;

        if(dataRoot.get("Palette") == null) {
            return;
        }

        palette = dataRoot.get("Palette").getAsCompoundList();

        TagBase tag = dataRoot.get("BlockStates");
        blockStates = (tag != null) ? tag.getAsLongArray() : null;

        tag = dataRoot.get("BlockLight");
        blockLight = (tag != null) ? tag.getAsByteArray() : null;

        tag = dataRoot.get("SkyLight");
        skyLight = (tag != null) ? tag.getAsByteArray() : null;
    }

    private Section() {
    }

    public static Section createSection() {
        Section section = new Section();
        section.blockStates = new long[256];
        section.palette = new TagList<>(TagCompound.class);

        TagCompound airBlock = new TagCompound();
        airBlock.add(new TagString("Name", "minecraft:air"));

        section.palette.add(airBlock);
        section.data = new TagCompound();

        return section;
    }

    public void setBlockStateAt(int x, int y, int z, TagCompound state) {
        int bitsPerBlock = getBitsUsed(palette.size());
        //int paletteIndex = addToPalette(state);

        for(int i = 0; i < 20; i++) {
            System.out.println(blockStates[i]);
        }

        for(int i = 0; i < palette.size(); i++) {
            System.out.println("ID: " + i + ", " + palette.get(i).getAsTagCompound().get("Name").getAsString());
        }

        needsRebuilding = (getBitsUsed(palette.size()) - bitsPerBlock) != 0;

        BitReader reader = new BitReader(blockStates, getBitsUsed(palette.size()));

        reader.jumpToValue(getBlockIndex(x, y, z));
        int blockState = reader.read();
        System.out.println(palette.get(blockState).getAsTagCompound().get("Name").getAsString());

        //reader.write(paletteIndex);
        // todo:
        //  reader.write(paletteIndex);
    }

    private int addToPalette(TagCompound data) {
        if(!palette.getTags().contains(data)) {
            palette.add(data);
        }

        return palette.getTags().indexOf(data);
    }

    private int getBlockIndex(int x, int y, int z) {
        return ((y & 0x0f) << 8) | ((z & 0x0f) << 4) | (x & 0x0f);
    }

    private int getBitsUsed(int value) {
        // if the number of bits value takes is < 4, we return 4 (aka the smallest number of bits per block)
        // if it's greater than 4, we return the number of bits that value takes
        return Math.max(Integer.SIZE - Integer.numberOfLeadingZeros(value), 4);
    }

    public void cleanUpBlocksAndPalettes() {
        // todo:
    }

    public boolean needsRebuilding() {
        return needsRebuilding;
    }

    public boolean isEmpty() {
        return this.data == null;
    }
}
