package com.deltabase.everphase.gui;

import com.deltabase.everphase.engine.render.RendererMaster;
import com.deltabase.everphase.engine.resource.TextureGui;
import com.deltabase.everphase.main.Main;
import org.joml.Vector4f;

public class GuiIngame extends Gui {

    private TextureGui textureGuiIngame;

    public GuiIngame(RendererMaster renderer) {
        super(renderer);

        textureGuiIngame = renderer.getModelLoader().loadGuiTexture("gui/guiIngame");
    }

    @Override
    public void render() {
        renderer.getRendererGui().drawTexture(0, -1.8F, 1.0F, new Vector4f(0, 0, 256, 24), textureGuiIngame);

        renderer.getRendererGui().drawTexture(-1.563F / Main.getAspectRatio() + 0.1955F * Main.getPlayer().getSelectedItem() / Main.getAspectRatio(), -1.5975F, 1.0F, new Vector4f(210, 25, 256, 50), textureGuiIngame);

        /*renderer.getRendererGui().drawTexture(-0.5F, 0.2F, 0.5F, new Vector4f(0, 195, 256, 122), textureGuiIngame);

        renderer.getRendererGui().drawTexture(-0.5F, 0.429F, 0.5F, new Vector4f(0, 188, 45, 103), textureGuiIngame);
        renderer.getRendererGui().drawTexture(-0.5F, 0.429F, 0.5F, new Vector4f(0, 175, 40, 100), textureGuiIngame);
        renderer.getRendererGui().drawTexture(-0.5F, 0.429F, 0.5F, new Vector4f(0, 163, 35, 95), textureGuiIngame);*/

    }
}