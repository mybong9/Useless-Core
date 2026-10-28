package com.sorrowmist.uselesscore.client.font;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Font family / B / I / size toolbar with a separate sample area. */
public final class CoreFontScreen extends Screen {
    private final Screen parent;
    private CoreFontSettings.Options pending=CoreFontSettings.options();
    private Component status=Component.empty();
    private boolean applying;
    private Button apply,family;
    private EditBox file,hex;
    private static final int[] PALETTE={0x000000,0x333333,0x666666,0x999999,0xcccccc,0xffffff,0x8b0000,0xff0000,
            0xff8800,0xffcc00,0x008000,0x00aaaa,0x0088ff,0x0000ff,0x800080,0xff66aa};
    CoreFontScreen(Screen parent){super(text("title"));this.parent=parent;}
    private static Component text(String key,Object... args){return Component.translatable("gui.useless_core.font."+key,args);}
    private static Tooltip tip(String key){return Tooltip.create(text(key).copy().withStyle(s->s.withColor(0xffffff)));}
    private void edit(CoreFontSettings.Choice choice,String filename,int size,boolean bold,boolean italic){
        pending=new CoreFontSettings.Options(choice,filename,size,bold,italic,pending.color(),pending.shadow());
    }
    private Component familyLabel(){
        var label=Component.translatable(pending.choice().key());
        if(pending.choice()!=CoreFontSettings.Choice.DEFAULT && CoreFontSettings.find(pending.choice(),pending.customFile())==null)label.append(text("missing"));
        return label.withStyle(s->s.withColor(0xffffff));
    }
    @Override protected void init() {
        int x=width/2-145,top=28;
        family=addRenderableWidget(Button.builder(familyLabel(),b->{
            var choices=CoreFontSettings.Choice.values();
            edit(choices[(pending.choice().ordinal()+1)%choices.length],pending.customFile(),pending.size(),pending.bold(),pending.italic());rebuildWidgets();
        }).bounds(x,top,290,20).build());
        family.setTooltip(tip("family_hint"));
        file=addRenderableWidget(new EditBox(font,x+3,top+23,284,20,text("custom_file")));
        file.setBordered(false);
        file.setTextColor(0xffffff);file.setTextColorUneditable(0xaaaaaa);
        file.setMaxLength(128);file.setValue(pending.customFile());
        file.setResponder(value->{edit(CoreFontSettings.Choice.CUSTOM,value,pending.size(),pending.bold(),pending.italic());family.setMessage(familyLabel());});
        file.setEditable(pending.choice()==CoreFontSettings.Choice.CUSTOM);
        file.setTooltip(tip("file_hint"));
        addRenderableWidget(Button.builder(Component.literal(pending.bold()?"B ✓":"B").withStyle(s->s.withBold(true)),b->{
            edit(pending.choice(),pending.customFile(),pending.size(),!pending.bold(),pending.italic());rebuildWidgets();
        }).bounds(x,top+46,48,20).tooltip(tip("bold")).build());
        addRenderableWidget(Button.builder(Component.literal(pending.italic()?"I ✓":"I").withStyle(s->s.withItalic(true)),b->{
            edit(pending.choice(),pending.customFile(),pending.size(),pending.bold(),!pending.italic());rebuildWidgets();
        }).bounds(x+52,top+46,48,20).tooltip(tip("italic")).build());
        addRenderableWidget(Button.builder(Component.literal("−"),b->{edit(pending.choice(),pending.customFile(),pending.size()-1,pending.bold(),pending.italic());rebuildWidgets();})
                .bounds(x+108,top+46,24,20).build());
        addRenderableWidget(Button.builder(text("size",pending.size()),b->{edit(pending.choice(),pending.customFile(),9,pending.bold(),pending.italic());rebuildWidgets();})
                .bounds(x+136,top+46,64,20).tooltip(tip("size_hint")).build());
        addRenderableWidget(Button.builder(Component.literal("+"),b->{edit(pending.choice(),pending.customFile(),pending.size()+1,pending.bold(),pending.italic());rebuildWidgets();})
                .bounds(x+204,top+46,24,20).build());
        addRenderableWidget(Button.builder(text("reset"),b->{pending=CoreFontSettings.Options.defaults();status=text("reset_pending");rebuildWidgets();})
                .bounds(x+232,top+46,58,20).build());
        addRenderableWidget(Button.builder(text("folder"),b->{
            try{CoreFontSettings.prepareDirectory();Util.getPlatform().openFile(CoreFontSettings.directory().toFile());}
            catch(Exception exception){status=text("failed");}
        }).bounds(x,top+69,142,20).build());
        addRenderableWidget(Button.builder(text("rescan"),b->rebuildWidgets()).bounds(x+148,top+69,142,20).build());
        addRenderableWidget(Button.builder(text(pending.shadow()?"shadow_on":"shadow_off"),b->{
            pending=new CoreFontSettings.Options(pending.choice(),pending.customFile(),pending.size(),pending.bold(),pending.italic(),pending.color(),!pending.shadow());rebuildWidgets();
        }).bounds(x,143,142,20).tooltip(tip("shadow_hint")).build());
        hex=addRenderableWidget(new EditBox(font,x+179,146,108,18,text("color")));
        hex.setBordered(false);hex.setMaxLength(7);hex.setFilter(value->value.matches("#?[0-9a-fA-F]{0,6}"));
        hex.setTextColor(0xffffff);
        hex.setValue(String.format(java.util.Locale.ROOT,"#%06X",pending.color()));
        hex.setResponder(value->{String rgb=value.startsWith("#")?value.substring(1):value;if(rgb.length()==6)setColor(Integer.parseInt(rgb,16));});
        hex.setTooltip(tip("color_hint"));
        apply=addRenderableWidget(Button.builder(text("apply"),b->apply()).bounds(x,height-28,142,20).build());apply.active=!applying;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(x+148,height-28,142,20).build());
        for(var child:children())if(child instanceof Button button)button.setMessage(button.getMessage().copy().withStyle(s->s.withColor(0xffffff)));
    }
    private void setColor(int color){pending=new CoreFontSettings.Options(pending.choice(),pending.customFile(),pending.size(),pending.bold(),pending.italic(),color,pending.shadow());}
    @Override public boolean mouseClicked(double mx,double my,int button) {
        int left=width/2-145;
        if(button==0 && my>=121 && my<139 && mx>=left && mx<left+288){
            int index=(int)(mx-left)/18;setColor(PALETTE[index]);hex.setValue(String.format(java.util.Locale.ROOT,"#%06X",pending.color()));return true;
        }
        return super.mouseClicked(mx,my,button);
    }
    private void apply() {
        if(applying)return;
        if(hex.getValue().replace("#","").length()!=6){status=text("invalid_color");return;}
        if(pending.choice()!=CoreFontSettings.Choice.DEFAULT && CoreFontSettings.find(pending.choice(),pending.customFile())==null){status=text("failed");return;}
        try{CoreFontSettings.save(pending);}catch(Exception exception){status=text("save_failed");return;}
        applying=true;apply.active=false;status=text("applying");
        minecraft.reloadResourcePacks().whenComplete((ignored,failure)->minecraft.execute(()->{
            applying=false;status=text(failure!=null || CoreFontSettings.error()!=null?"failed":"applied");
            if(CoreFontSettings.error()!=null)try{CoreFontSettings.save(CoreFontSettings.Options.defaults());pending=CoreFontSettings.Options.defaults();}catch(Exception ignoredError) { }
            rebuildWidgets();
        }));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        CoreFontSettings.preview(()->renderContents(g,mx,my,partial));
    }
    private void renderContents(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g);
        int left=width/2-145;
        g.fill(left,51,left+290,72,0xe0202530);g.fill(left+175,143,left+290,163,0xe0202530);
        super.render(g,mx,my,partial);
        g.drawCenteredString(font,title,width/2,9,0xffffffff);
        for(int i=0;i<PALETTE.length;i++){
            int x=left+i*18;g.fill(x,121,x+17,139,pending.color()==PALETTE[i]?0xff2277ee:0xff737985);
            g.fill(x+2,123,x+15,137,0xff000000|PALETTE[i]);
        }
        g.drawString(font,"HEX",left+148,148,0xffffffff,false);
        int color=pending.color(),luma=((color>>16&255)*299+(color>>8&255)*587+(color&255)*114)/1000;
        g.fill(left,166,left+290,190,luma<100?0xe08b9099:0xe0202530);
        CoreFontSettings.preview(pending.shadow(),()->{
            float ratio=(float)pending.size()/CoreFontSettings.options().size();
            String sample=text("preview").getString();
            Component value=Component.literal(sample).withStyle(s->s.withBold(pending.bold()).withItalic(pending.italic()).withColor(pending.color()));
            while(sample.length()>1 && font.width(value)*ratio>270){sample=sample.substring(0,sample.length()-1);value=Component.literal(sample).withStyle(s->s.withBold(pending.bold()).withItalic(pending.italic()).withColor(pending.color()));}
            g.pose().pushPose();g.pose().translate(width/2f,171,0);g.pose().scale(ratio,ratio,1);
            g.drawString(font,value,-font.width(value)/2,0,0xff000000|pending.color(),pending.shadow());g.pose().popPose();
        });
        g.drawCenteredString(font,font.plainSubstrByWidth(status.getString(),width-20),width/2,height-44,0xffffffff);
        if(my>=121 && my<139 && mx>=left && mx<left+288)
            g.renderTooltip(font,Component.literal(String.format(java.util.Locale.ROOT,"#%06X",PALETTE[(mx-left)/18])),mx,my);
    }
    @Override public void onClose(){if(!applying)minecraft.setScreen(parent);}
}
