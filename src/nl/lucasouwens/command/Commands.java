package nl.lucasouwens.command;

import nl.lucasouwens.command.parsing.CommandType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Commands {

    private static Commands instance = null;

    /**
     * (Singleton)
     * Easy access to the commands class so that we don't need to reconstruct it every time
     * @return Commands
     */
    public static Commands getInstance() {
        if(instance == null) {
            instance = new Commands();
        }

        return instance;
    }

    // Below are the commands which i am registering.

    @CommandType(name = "fdelete", args = {"filename"})
    public void deleteFile(String fileName) {
        File toDelete = new File(fileName);
        if(!(toDelete.exists())) {
            System.out.println("[Lucas' Terminal] We cannot delete this file.");
        } else if(toDelete.delete()) {
            System.out.println(String.format("[Lucas' Terminal] The file %s has been deleted", fileName));
        } else {
            System.out.println("[Lucas' Terminal] We are cannot delete this file.");
        }
    }

    @CommandType(name = "fcreate", args = {"filename"}, optional = {"contents"})
    public void createFile(String fileName, String contents) {
        File toCreate = new File(fileName);
        if(toCreate.isDirectory() || toCreate.exists()) {
            System.out.println("[Lucas' Terminal] We cannot create this file.");
            return;
        }

        File _temp = new File(toCreate.getAbsolutePath().replace(toCreate.getName(), ""));
        if(!_temp.exists()) {
            if(!_temp.mkdirs()) {
                System.out.println("[Lucas' Terminal] The creation of the directory has failed.");
                return;
            }
        }

        try {
            if (!(toCreate.createNewFile())) {
                System.out.println("[Lucas' Terminal] The creation of the file has failed.");
            }

            if(contents != null) {
                try(FileWriter fw = new FileWriter(toCreate)) {
                    fw.write(contents);
                }
            }

            System.out.println("[Lucas' Terminal] The file was created.");
        } catch(IOException e) {
            System.out.println("[Lucas' Terminal] An error has occurred.");
            e.printStackTrace();
        }

    }

}
