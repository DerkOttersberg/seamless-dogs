package io.github.derkottersberg.seamlessdogs.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

/** Drafts survive page changes/resizing. Server changes are applied only after acknowledgement. */
public final class DogsSettingsScreen extends Screen {
    private final Screen parent;
    private int page;
    private boolean prompt=ClientOptions.prompt,eyes=ClientOptions.eyes,animation=ClientOptions.animation;
    private boolean ownDig,worldDig,finds,stretch,serverDirty,pending;
    private long revision=-1;
    private String error="";
    private int pendingTicks;
    private int contentY(){return height<230?58:80;}
    private int spacing(){return height<230?20:26;}
    public DogsSettingsScreen(Screen parent) { super(Component.translatable("seamlessdogs.settings"));this.parent=parent; snapshot(DogsClient.settings);DogsClient.refreshSettings(); }
    private void snapshot(DogsClient.Settings s) { ownDig=(s.flags()&8)!=0;worldDig=(s.flags()&1)!=0;finds=(s.flags()&2)!=0;stretch=(s.flags()&4)!=0;revision=s.revision(); }
    public void serverSnapshot(DogsClient.Settings s) {
        if(pending) {
            int mask=page==1?8:7,expected=page==1?(ownDig?8:0):(worldDig?1:0)|(finds?2:0)|(stretch?4:0);
            boolean saved=s.revision()>revision && (s.flags()&mask)==expected;
            pending=false;
            if(saved){serverDirty=false;snapshot(s);error="";}
            else {revision=s.revision();error=s.status().contains("not saved")?s.status():"Settings changed on the server. Review and save again.";}
        }
        else if(!serverDirty) snapshot(s);
        if(minecraft!=null)rebuildWidgets();
    }
    protected void init() {
        int w=Math.min(340,width-24),x=(width-w)/2;
        String[] pages={"Client visuals","My pets","World/server"};
        for(int i=0;i<3;i++) { final int p=i;var b=addRenderableWidget(Button.builder(Component.literal(pages[i]),v->{page=p;rebuildWidgets();}).bounds(x+i*(w/3),height<230?34:44,w/3-3,height<230?18:20).build());b.active=i!=page; }
        boolean available=DogsClient.settings.ready()&&DogsClient.settings.writable()&&!pending;
        int y=contentY();
        if(page==0) {
            toggle(x,y,w,"Petting prompt",prompt,b->{prompt=!prompt;b.setMessage(label("Petting prompt",prompt));},true);
            toggle(x,y+spacing(),w,"Eye expressions",eyes,b->{eyes=!eyes;b.setMessage(label("Eye expressions",eyes));},true);
            toggle(x,y+2*spacing(),w,"Pet animations",animation,b->{animation=!animation;b.setMessage(label("Pet animations",animation));},true);
            var keyButton=addRenderableWidget(Button.builder(
                Component.translatable("seamlessdogs.setting.pet_key",DogsKeys.PET.getTranslatedKeyMessage()),
                b->minecraft.setScreen(new KeyBindsScreen(this,minecraft.options)))
                .bounds(x,y+3*spacing(),w,height<230?18:20).build());
            keyButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("seamlessdogs.keyhelp")));
        } else if(page==1) toggle(x,y,w,"My dogs can dig",ownDig,b->{ownDig=!ownDig;serverDirty=true;b.setMessage(label("My dogs can dig",ownDig));},available);
        else {
            available=available&&DogsClient.settings.admin();
            toggle(x,y,w,"Dogs can dig",worldDig,b->{worldDig=!worldDig;serverDirty=true;b.setMessage(label("Dogs can dig",worldDig));},available);
            toggle(x,y+spacing(),w,"Additional digging finds",finds,b->{finds=!finds;serverDirty=true;b.setMessage(label("Additional digging finds",finds));},available);
            toggle(x,y+2*spacing(),w,"Pet idle expressions",stretch,b->{stretch=!stretch;serverDirty=true;b.setMessage(label("Pet idle expressions",stretch));},available);
        }
        int bw=(w-12)/3;
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"),b->{if(page==0)prompt=eyes=animation=true;else {if(page==1)ownDig=true;else worldDig=finds=stretch=true;serverDirty=true;}rebuildWidgets();}).bounds(x,height-28,bw,20).build()).active=page==0||available;
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),b->onClose()).bounds(x+bw+6,height-28,bw,20).build());
        addRenderableWidget(Button.builder(Component.translatable("seamlessdogs.save"),b->{
            try {
                if(page==0){ClientOptions.save(prompt,eyes,animation);onClose();}
                else {pending=true;pendingTicks=0;error="Saving...";DogsClient.saveSettings(page==1,page==1?(ownDig?1:0):(worldDig?1:0)|(finds?2:0)|(stretch?4:0),revision);rebuildWidgets();}
            }catch(IllegalArgumentException e){error=e.getMessage();}
        }).bounds(x+2*(bw+6),height-28,bw,20).build()).active=page==0||available;
    }
    private void toggle(int x,int y,int w,String title,boolean value,java.util.function.Consumer<Button> click,boolean enabled) {
        var button=addRenderableWidget(Button.builder(label(title,value),click::accept).bounds(x,y,w,height<230?18:20).build());button.active=enabled;
        if(page!=0)button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(DogsClient.settings.status())));
    }
    private Component label(String title,boolean value) {return Component.literal(title+": ").append(Component.translatable(value?"options.on":"options.off"));}
    public void onClose(){minecraft.setScreen(parent);}
    public boolean isPauseScreen(){return false;}
    public void tick(){if(pending&&++pendingTicks>100){pending=false;error="Server did not confirm the save. Refresh and try again.";DogsClient.refreshSettings();rebuildWidgets();}}
    public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial) {
        graphics.fill(0,0,width,height,0xF0182125);
        // Screen draws its native blurred background before widgets. Labels must follow it.
        super.render(graphics,mouseX,mouseY,partial);
        graphics.drawCenteredString(font,title,width/2,height<230?8:14,0xFFFFFFFF);
        String scope=page==0?"This client's visuals":page==1?"Your pets in this world":"Server rules · host or permission level 2";
        graphics.drawCenteredString(font,Component.literal(scope),width/2,height<230?22:32,0xFFB7E8BD);
        if(page!=0) {
            String status=page==1 && !worldDig?"Server digging is disabled.":DogsClient.settings.status();
            int y=contentY()+(page==1?28:2*spacing()+28);
            for(var line:font.split(Component.literal(status),Math.min(340,width-24))) {if(y>height-(!error.isEmpty()?54:38))break;graphics.drawString(font,line,(width-font.width(line))/2,y,0xFFCCCCCC,false);y+=10;}
        }
        if(!error.isEmpty())graphics.drawCenteredString(font,Component.literal(error),width/2,height-44,0xFFFF8888);
    }
}
