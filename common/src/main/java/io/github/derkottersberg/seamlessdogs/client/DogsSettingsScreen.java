package io.github.derkottersberg.seamlessdogs.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DogsSettingsScreen extends Screen {
    private final Screen parent;
    private boolean prompt = ClientOptions.prompt, eyes = ClientOptions.eyes, animation = ClientOptions.animation;
    private String error = "";
    public DogsSettingsScreen(Screen parent) { super(Component.translatable("seamlessdogs.settings")); this.parent = parent; }
    protected void init() {
        int w = Math.min(280, width - 24), x = (width - w) / 2;
        addRenderableWidget(Button.builder(label("seamlessdogs.setting.prompt", prompt), b -> { prompt = !prompt; b.setMessage(label("seamlessdogs.setting.prompt", prompt)); }).bounds(x, 56, w, 20).build());
        addRenderableWidget(Button.builder(label("seamlessdogs.setting.eyes", eyes), b -> { eyes = !eyes; b.setMessage(label("seamlessdogs.setting.eyes", eyes)); }).bounds(x, 82, w, 20).build());
        addRenderableWidget(Button.builder(label("seamlessdogs.setting.animation", animation), b -> { animation = !animation; b.setMessage(label("seamlessdogs.setting.animation", animation)); }).bounds(x, 108, w, 20).build());
        int bw = (w - 12) / 3;
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), b -> { prompt = eyes = animation = true; rebuildWidgets(); }).bounds(x, height - 28, bw, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).bounds(x + bw + 6, height - 28, bw, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("seamlessdogs.save"), b -> {
            try { ClientOptions.save(prompt, eyes, animation); onClose(); } catch (IllegalArgumentException e) { error = e.getMessage(); }
        }).bounds(x + (bw + 6) * 2, height - 28, bw, 20).build());
    }
    private Component label(String key, boolean enabled) { return Component.translatable(key).append(": ").append(Component.translatable(enabled ? "options.on" : "options.off")); }
    public void onClose() { minecraft.setScreenAndShow(parent); }
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        graphics.fill(0, 0, width, height, 0xF0182125);
        graphics.centeredText(font, title, width / 2, 14, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("seamlessdogs.scope"), width / 2, 32, 0xFFB7E8BD);
        graphics.centeredText(font, Component.translatable("seamlessdogs.keyhelp"), width / 2, 140, 0xFFCCCCCC);
        if (!error.isEmpty()) graphics.centeredText(font, error, width / 2, height - 44, 0xFFFF8888);
        super.extractRenderState(graphics, mouseX, mouseY, partial);
    }
}
