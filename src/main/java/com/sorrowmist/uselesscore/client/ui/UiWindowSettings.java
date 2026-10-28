package com.sorrowmist.uselesscore.client.ui;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/** Client-local sizes, one entry per screen type. No server packets or global GUI changes. */
final class UiWindowSettings {
    private static final Map<String,Double> sizes=new HashMap<>();
    private static boolean loaded;
    private static Path path(){return FMLPaths.CONFIGDIR.get().resolve("useless_core/ui-windows.json");}
    static double get(String key){
        if(!loaded){loaded=true;if(Files.isRegularFile(path()))try(var reader=Files.newBufferedReader(path())){
            var object=JsonParser.parseReader(reader).getAsJsonObject();
            for(var entry:object.entrySet()){double scale=entry.getValue().getAsDouble();if(Double.isFinite(scale))sizes.put(entry.getKey(),clamp(scale));}
        }catch(Exception error){LogUtils.getLogger().warn("Cannot load UI sizes; using defaults",error);}}
        return sizes.getOrDefault(key,1d);
    }
    static double clamp(double value){return Math.max(.35,Math.min(3,value));}
    static void set(String key,double scale){sizes.put(key,clamp(scale));}
    static void save(){try{
        Files.createDirectories(path().getParent());var temp=path().resolveSibling("ui-windows.json.tmp");
        Files.writeString(temp,new GsonBuilder().setPrettyPrinting().create().toJson(sizes));
        Files.move(temp,path(),StandardCopyOption.REPLACE_EXISTING);
    }catch(Exception error){LogUtils.getLogger().warn("Cannot save UI sizes",error);}}
}
