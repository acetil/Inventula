package acetil.inventula.client.gui;

import acetil.inventula.common.containers.SuperDispenserContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class SuperDispenserGui extends ContainerScreen<SuperDispenserContainer> {
    // derived from DispenserScreen
    private ITextComponent name;
    private static ResourceLocation background = new ResourceLocation("minecraft",
            "textures/gui/container/dispenser.png");
    public SuperDispenserGui (SuperDispenserContainer screenContainer, PlayerInventory inv, ITextComponent name) {
        super(screenContainer, inv, name);
        this.name = name;
    }

    @Override
    public void render (int p_render_1_, int p_render_2_, float p_render_3_) {
        super.render(p_render_1_, p_render_2_, p_render_3_);
        renderHoveredToolTip(p_render_1_, p_render_2_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer (float partialTicks, int mouseX, int mouseY) {
        minecraft.getTextureManager().bindTexture(background);
        blit((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer (int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String title = name.getFormattedText();
        font.drawString(title, (float)(xSize - font.getStringWidth(title)) / 2, 6.0f, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F,
                (float)(this.ySize - 96 + 2), 4210752);
    }
}
