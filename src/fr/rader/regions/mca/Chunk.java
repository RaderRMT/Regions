package fr.rader.regions.mca;

import fr.rader.regions.nbt.tags.TagCompound;
import fr.rader.regions.nbt.tags.TagList;
import fr.rader.regions.utils.DataReader;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Chunk {

    private TagCompound data;

    private Section[] sections = new Section[16];

    private int lastUpdate;

    public Chunk(int lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void deserialize(RandomAccessFile raf) throws IOException {
        DataReader reader = new DataReader(DataCompression.decompress(raf));
        data = reader.readNBT();

        TagList<TagCompound> tagSections = data.get("Level").getAsTagCompound().get("Sections").getAsCompoundList();

        for(TagCompound sectionTag : tagSections) {
            int sectionIndex = sectionTag.get("Y").getAsByte();
            if(sectionIndex < 0 || sectionIndex > 15) {
                continue;
            }

            Section section = new Section(sectionTag);
            if(section.isEmpty()) {
                continue;
            }

            sections[sectionIndex] = section;
        }
    }

    public void setBlockStateAt(int x, int y, int z, TagCompound state) {
        int sectionIndex = y >> 4; // y >> 4 is the same as y / 16

        Section section = sections[sectionIndex];

        if(section == null) {
            section = Section.createSection();
            sections[sectionIndex] = section;
        }

        section.setBlockStateAt(x, y, z, state);
    }

    public static Chunk createChunk() {
        Chunk chunk  = new Chunk(0);
        chunk.data = new TagCompound();
        chunk.data.add(new TagCompound("Level"));

        return chunk;
    }

    public void cleanUpBlocksAndPalettes() {
        for(Section section : sections) {
            if(section != null) {
                section.cleanUpBlocksAndPalettes();
            }
        }
    }
}
