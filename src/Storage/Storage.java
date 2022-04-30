package Storage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Storage{

    public static final String NOT_FOUND=null;
    private static final String FILENAME="save.txt";
    private static Storage instance=null;
    private static Properties properties;

    /**
     * Returns a global instance of Storage
     *
     * @return Storage instance
     */
    public static synchronized Storage getInstance(){
        if (instance==null){
            instance=new Storage();
        }
        return instance;
    }

    private Storage(){
        properties=new Properties();
        try{
            properties.load(new FileReader(FILENAME));
        } catch (FileNotFoundException e){
            System.out.println("No save file exists");
        } catch (IOException e){
            System.out.println("IO Exception: cannot load save file");
        }
    }

    /**
     * Save a key,value pair in long-term storage
     */
    public synchronized void save(String key, String value){
        properties.setProperty(key, value);
        saveProperties();
    }

    /**
     * Get the value for the given key
     *
     * @return corresponding value or Storage.NOT_FOUND
     */
    public synchronized String load(String key){
        if (properties.containsKey(key)){
            return properties.getProperty(key);
        } else{
            return NOT_FOUND;
        }
    }

    /**
     * Check if a value exists for a given key
     *
     * @return whether this key,value pair exists
     */
    public synchronized boolean keyExists(String key){
        return properties.containsKey(key);
    }

    private synchronized void saveProperties(){
        try{
            properties.store(new FileWriter(FILENAME), null);
        } catch (IOException e){
            System.out.println("IO Exception: failed to save data");
        }
    }
}
