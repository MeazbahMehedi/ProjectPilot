package app;

import com.google.gson.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import app.Models.*;

/** Single-process lock plus same-directory atomic replacement. Never deserializes Java objects. */
public final class Store implements AutoCloseable {
    final Path path;
    final Gson gson=new GsonBuilder().setPrettyPrinting().create();
    private final FileChannel channel;
    private FileLock lock;
    public Store(Path directory) throws IOException {
        Files.createDirectories(directory);path=directory.resolve("workspace.json");
        channel=FileChannel.open(directory.resolve("workspace.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        try {
            lock=channel.tryLock();
            if(lock==null)throw new IOException("This workspace is already open. Close the other instance first.");
        } catch(IOException|RuntimeException ex) {channel.close();throw ex;}
    }
    Data load() throws IOException {
        if(!Files.exists(path))return null;
        try {
            Data d=gson.fromJson(Files.readString(path),Data.class);
            if(d==null||d.schemaVersion!=1||d.users==null||d.projects==null||d.assignments==null||d.favorites==null||d.waitlist==null||d.activity==null)
                throw new IOException("Unsupported or incomplete workspace. Restore a known-good backup.");
            return d;
        } catch(JsonParseException ex){throw new IOException("Workspace JSON is damaged. Restore a backup; the file has not been overwritten.",ex);}
    }
    void save(Data d) throws IOException {
        Path temp=Files.createTempFile(path.getParent(),"workspace-",".tmp");
        try {
            byte[] bytes=gson.toJson(d).getBytes(StandardCharsets.UTF_8);
            try(FileChannel output=FileChannel.open(temp,StandardOpenOption.WRITE)){java.nio.ByteBuffer buffer=java.nio.ByteBuffer.wrap(bytes);while(buffer.hasRemaining())output.write(buffer);output.force(true);}
            try {Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException ex){throw new IOException("This filesystem cannot safely replace the workspace atomically. Choose a local data directory.",ex);}
        } finally{Files.deleteIfExists(temp);}
    }
    public void close() throws IOException {try{if(lock!=null&&lock.isValid())lock.release();}finally{channel.close();}}
}
