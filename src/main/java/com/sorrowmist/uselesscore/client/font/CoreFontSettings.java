package com.sorrowmist.uselesscore.client.font;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Client-local preferences. Font files stay on the user's machine and are never sent to a server. */
public final class CoreFontSettings {
    public enum Choice {
        DEFAULT("default", new String[0]),
        YAHEI("yahei", new String[]{"yahei.ttf","yahei.ttc","msyh.ttc","msyh.ttf"}),
        HUAWEI("huawei", new String[]{"huawei.ttf","huawei.ttc","HarmonyOS_Sans_SC_Regular.ttf","HarmonyOS_Sans_SC.ttf","HarmonyOS_Sans.ttf"}),
        APPLE("apple", new String[]{"apple.ttf","apple.ttc","PingFang.ttc","PingFang SC.ttf","PingFang.ttf"}),
        SCRIPT("script", new String[]{"script.ttf","script.ttc","STXINGKA.TTF","simkai.ttf","MTCORSVA.TTF","Gabriola.ttf"}),
        CUSTOM("custom",new String[0]);
        public final String id;
        final String[] names;
        Choice(String id,String[] names){this.id=id;this.names=names;}
        public String key(){return "gui.useless_core.font."+id;}
    }
    private record Preference(String font,String customFile,int size,boolean bold,boolean italic,Integer color,boolean shadow) {}
    public record Options(Choice choice,String customFile,int size,boolean bold,boolean italic,int color,boolean shadow) {
        public Options {
            if(choice==null)choice=Choice.DEFAULT;
            if(customFile==null)customFile="custom.ttf";
            size=size==0?9:Math.max(6,Math.min(16,size));
            color&=0xffffff;
        }
        public static Options defaults(){return new Options(Choice.DEFAULT,"custom.ttf",9,false,false,0,false);}
    }
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private static volatile Choice selected;
    private static volatile Options options;
    private static final ThreadLocal<Boolean> PREVIEW=ThreadLocal.withInitial(()->false);
    private static final ThreadLocal<Boolean> SHADOW_PREVIEW=new ThreadLocal<>();
    private static volatile String error;
    private CoreFontSettings() {}
    public static Path directory(){return FMLPaths.CONFIGDIR.get().resolve("useless_core/fonts");}
    public static void prepareDirectory() throws java.io.IOException {
        Files.createDirectories(directory());
        Path readme=directory().resolve("README.txt");
        if(!Files.exists(readme))Files.writeString(readme,
                "Useless Core local fonts / 本地字体\n"
                +"微软雅黑: yahei.ttf / yahei.ttc / msyh.ttc\n"
                +"华为: huawei.ttf / huawei.ttc / HarmonyOS_Sans_SC_Regular.ttf\n"
                +"苹果: apple.ttf / apple.ttc / PingFang.ttc\n"
                +"花体/书法: script.ttf / script.ttc; 自定义: 任意 TTF / TTC 文件名\n"
                +"Place your font here, then rescan in Font Settings. / 放入自己的字体后在字体设置中重新扫描。\n"
                +"Fonts are not redistributed or uploaded to servers. / 字体不会上传服务器或随模组分发。\n");
    }
    private static Path config(){return FMLPaths.CONFIGDIR.get().resolve("useless_core/font.json");}
    public static synchronized Choice selected() {
        if(selected!=null)return selected;
        selected=Choice.DEFAULT;
        if(Files.isRegularFile(config()))try(var reader=Files.newBufferedReader(config())){
            var data=JSON.fromJson(reader,Preference.class);
            if(data!=null) {
                for(var choice:Choice.values())if(choice.id.equals(data.font))selected=choice;
                options=new Options(selected,data.customFile,data.size,data.bold,data.italic,data.color==null?0:data.color,data.shadow);
            }
        }catch(Exception exception){LogUtils.getLogger().warn("Cannot read Core font settings; using Minecraft font",exception);}
        return selected;
    }
    public static synchronized Options options(){selected();if(options==null)options=Options.defaults();return options;}
    public static synchronized void select(Choice choice) throws java.io.IOException {
        var old=options();save(new Options(choice,old.customFile,old.size,old.bold,old.italic,old.color,old.shadow));
    }
    public static synchronized void save(Options value) throws java.io.IOException {
        Files.createDirectories(directory());
        Path temporary=config().resolveSibling("font.json.tmp");
        Files.writeString(temporary,JSON.toJson(new Preference(value.choice.id,value.customFile,value.size,value.bold,value.italic,value.color,value.shadow)));
        Files.move(temporary,config(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        selected=value.choice;options=value;error=null;
    }
    public static float scale(){return options().size/9f;}
    public static boolean shadow(boolean requested){var preview=SHADOW_PREVIEW.get();return requested && (preview!=null?preview:options().shadow);}
    public static net.minecraft.network.chat.Style style(net.minecraft.network.chat.Style style) {
        return style(style,true);
    }
    public static net.minecraft.network.chat.Style style(net.minecraft.network.chat.Style style,boolean recolor) {
        if(PREVIEW.get() || (!net.minecraft.client.Minecraft.DEFAULT_FONT.equals(style.getFont())
                && !net.minecraft.client.Minecraft.UNIFORM_FONT.equals(style.getFont())))return style;
        var current=options();
        if(current.bold && !style.isBold())style=style.withBold(true);
        if(current.italic && !style.isItalic())style=style.withItalic(true);
        if(recolor && style.getColor()==null)style=style.withColor(current.color);
        return style;
    }
    public static void preview(Runnable action){boolean previous=PREVIEW.get();PREVIEW.set(true);try{action.run();}finally{PREVIEW.set(previous);}}
    public static void preview(boolean shadow,Runnable action){
        Boolean previous=SHADOW_PREVIEW.get();SHADOW_PREVIEW.set(shadow);
        try{preview(action);}finally{if(previous==null)SHADOW_PREVIEW.remove();else SHADOW_PREVIEW.set(previous);}
    }
    public static String error(){return error;}
    public static Path find(Choice choice) {
        return find(choice,options().customFile);
    }
    public static Path find(Choice choice,String customFile) {
        if(choice==Choice.DEFAULT)return null;
        if(choice==Choice.CUSTOM) {
            try {
                if(customFile==null || customFile.contains("/") || customFile.contains("\\"))return null;
                String lower=customFile.toLowerCase(Locale.ROOT);
                if(!lower.endsWith(".ttf") && !lower.endsWith(".ttc"))return null;
                Path root=directory().toAbsolutePath().normalize(),file=root.resolve(customFile).normalize();
                return file.startsWith(root) && Files.isRegularFile(file)?file:null;
            }catch(java.nio.file.InvalidPathException invalid){return null;}
        }
        var roots=new ArrayList<Path>();roots.add(directory());
        String windows=System.getenv("WINDIR"),local=System.getenv("LOCALAPPDATA");
        if(windows!=null)roots.add(Path.of(windows,"Fonts"));
        if(local!=null)roots.add(Path.of(local,"Microsoft/Windows/Fonts"));
        roots.add(Path.of(System.getProperty("user.home"),"Library/Fonts"));
        roots.add(Path.of("/System/Library/Fonts"));roots.add(Path.of("/System/Library/Fonts/Supplemental"));
        roots.add(Path.of("/Library/Fonts"));roots.add(Path.of(System.getProperty("user.home"),".local/share/fonts"));
        roots.add(Path.of("/usr/share/fonts"));
        for(Path root:roots) {
            if(!Files.isDirectory(root))continue;
            try(var files=Files.list(root)) {
                List<Path> candidates=files.filter(Files::isRegularFile).toList();
                for(String name:choice.names)for(Path candidate:candidates)
                    if(candidate.getFileName().toString().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)))return candidate;
            }catch(java.io.IOException ignored) { }
        }
        return null;
    }
    /** Use Minecraft's native glyph provider, including the first face of TrueType collections. */
    public static GlyphProvider createProvider() {
        Choice choice=selected();if(choice==Choice.DEFAULT)return null;
        try{return createProvider(options());}
        catch(Exception exception){
            error=choice.id;LogUtils.getLogger().warn("Cannot load Core font {}; keeping Minecraft fallback",choice.id,exception);
            return null;
        }
    }
    /** Load pending preview settings without changing saved preferences or the global error state. */
    public static GlyphProvider createProvider(Options requested) throws java.io.IOException {
        Choice choice=requested.choice();if(choice==Choice.DEFAULT)return null;
        ByteBuffer memory=null;STBTTFontinfo info=null;
        try {
            Path file=find(choice,requested.customFile());
            if(file==null)throw new java.io.IOException("Font file is missing: "+choice.id);
            long length=Files.size(file);
            if(length<12 || length>64*1024*1024)throw new java.io.IOException("Invalid font file size");
            byte[] bytes=Files.readAllBytes(file);
            memory=MemoryUtil.memAlloc(bytes.length);memory.put(bytes).flip();
            info=STBTTFontinfo.malloc();
            int offset=STBTruetype.stbtt_GetFontOffsetForIndex(memory,0);
            if(offset<0 || !STBTruetype.stbtt_InitFont(info,memory,offset))throw new java.io.IOException("Unsupported or damaged font");
            return new SmoothTrueTypeGlyphProvider(memory,info,requested.size(),requested.bold());
        }catch(Exception exception) {
            if(info!=null)info.free();if(memory!=null)MemoryUtil.memFree(memory);
            throw new java.io.IOException("Cannot load font: "+choice.id,exception);
        }
    }
}
