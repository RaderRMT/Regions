package fr.rader.regions;

import fr.rader.regions.mca.RegionFile;
import fr.rader.regions.nbt.tags.TagCompound;
import fr.rader.regions.nbt.tags.TagString;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {

    private void start() throws IOException {
        //File region = IO.openFilePrompt("Region File", System.getenv("appdata") + "/.minecraft/saves", ".mca");
        //if(region == null) return;

        File region = new File("C:/Users/Rader/AppData/Roaming/.minecraft/saves/test world chunk data/region/r.0.0.mca");

        RegionFile regionFile = new RegionFile(0, 0);
        regionFile.deserialize(region);

        TagCompound stoneBlock = new TagCompound();
        stoneBlock.add(new TagString("Name", "minecraft:stone"));

        regionFile.setBlockStateAt(7, 0, 7, stoneBlock);

        /*RegionFile regionFile = new RegionFile(region);

        Chunk chunk = regionFile.getChunkAt(0, 0);*/

        //chunk.getBlockStateAt(0, 0, 0);

        /*NBTEditor editor = new NBTEditor();
        editor.invokeEditor(chunk.getNBT());*/

        /*File file = IO.openFilePrompt("r", null, ".nbt");
        DataReader reader = new DataReader(file);*/
    }

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (IllegalAccessException | ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException e) {
            e.printStackTrace();
        }

        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
