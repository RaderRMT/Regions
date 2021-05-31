package fr.rader.regions;

import fr.rader.regions.nbt.tags.TagCompound;
import fr.rader.regions.utils.DataWriter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IO {

    public static File openFilePrompt(String description, String path, String... extensions) {
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                for(String extension : extensions) {
                    if(file.getName().endsWith(extension)) return true;
                }

                return file.isDirectory();
            }

            @Override
            public String getDescription() {
                String finalDescription = description + " (";

                for(int i = 0; i < extensions.length; i++) {
                    finalDescription += "*." + extensions[i] + ((i < extensions.length - 1) ? ", " : ")");
                }

                return finalDescription;
            }
        });

        int option = fileChooser.showOpenDialog(null);

        if(option == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    public static void writeNBTFile(File destination, TagCompound compound) {
        DataWriter writer = new DataWriter();
        compound.write(writer);

        writeFile(destination, writer.getInputStream());
    }

    public static void writeFile(File destination, InputStream inputStream) {
        try {
            FileOutputStream outputStream = new FileOutputStream(destination);

            int length;
            byte[] buffer = new byte[1024];
            while((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
