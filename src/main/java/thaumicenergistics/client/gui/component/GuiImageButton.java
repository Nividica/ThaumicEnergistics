package thaumicenergistics.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.init.ModGlobals;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A button class that supports both images and text,
 * and can also change them on hover or set their transparency!
 * @author Alex811
 */
public class GuiImageButton extends GuiButtonImage {
    private static final AtomicInteger ID = new AtomicInteger(1000);
    private static final int DEFAULT_WIDTH = 16;
    private static final int DEFAULT_HEIGHT = 16;
    private static final int YDIFFTEXT = 0;
    private int offsetX;
    private int offsetY;
    private ResourceLocation image;
    private ResourceLocation hoverImage;
    private ResourceLocation buttonTexture;
    private String text;
    private String hoverText;
    private int hoverOffsetX;
    private int hoverOffsetY;
    private float buttonAlpha = 1.0F;
    private float buttonHoverAlpha = 1.0F;
    private float imageAlpha = 1.0F;
    private float imageHoverAlpha = 1.0F;
    private float textAlpha = 1.0F;
    private float textHoverAlpha = 1.0F;

    public GuiImageButton(int x, int y, String resource){
        this(x, y, 0, 0, resource);
    }

    public GuiImageButton(int x, int y, int indexX, int indexY, String resource){
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, indexX * DEFAULT_WIDTH, indexY * DEFAULT_HEIGHT, resource);
    }

    public GuiImageButton(int x, int y, int w, int h, int offsetX, int offsetY, String resource){
        this(x, y, w, h, offsetX, offsetY, new ResourceLocation(ModGlobals.MOD_ID, resource));
    }

    public GuiImageButton(int x, int y, ResourceLocation resource){
        this(x, y, 0, 0, resource);
    }

    public GuiImageButton(int x, int y, int indexX, int indexY, ResourceLocation resource) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, indexX * DEFAULT_WIDTH, indexY * DEFAULT_HEIGHT, resource);
    }

    public GuiImageButton(int x, int y, int w, int h, int offsetX, int offsetY, ResourceLocation resource){
        super(ID.getAndIncrement(), x, y, w, h, offsetX, offsetY, YDIFFTEXT, resource);
        this.image = this.hoverImage = resource;
        this.offsetX = this.hoverOffsetX = offsetX;
        this.offsetY = this.hoverOffsetY = offsetY;
    }

    public GuiImageButton recalculateY(int slots) {
        this.y += slots * 18;
        return this;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, this.hovered ? this.buttonHoverAlpha : this.buttonAlpha);
            // Draw responsive button
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height / 2);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height / 2);
            this.drawTexturedModalRect(this.x, this.y + this.height / 2, 0, 58 + i * 20, this.width / 2, this.height / 2);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height / 2, 200 - this.width / 2, 58 + i * 20, this.width / 2, this.height / 2);

            // Draw overlay
            if(this.hovered){
                this.zLevel = -10;
                if(this.hoverImage != null){
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, this.imageHoverAlpha);
                    mc.getTextureManager().bindTexture(this.hoverImage);
                    this.drawTexturedModalRect(this.x, this.y, this.hoverOffsetX, this.hoverOffsetY, this.width, this.height);
                }
                if(this.hoverText != null){
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, this.textHoverAlpha);
                    int j = 16777120;
                    if (this.packedFGColour != 0) j = this.packedFGColour;
                    else if (!this.enabled) j = 10526880;
                    this.drawCenteredString(mc.fontRenderer, this.hoverText , this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
                }
            }else{
                if(this.image != null){
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, this.imageAlpha);
                    mc.getTextureManager().bindTexture(this.image);
                    this.drawTexturedModalRect(this.x, this.y, this.offsetX, this.offsetY, this.width, this.height);
                }
                if(this.text != null){
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, this.textAlpha);
                    int j = 14737632;
                    if (this.packedFGColour != 0) j = this.packedFGColour;
                    else if (!this.enabled) j = 10526880;
                    this.drawCenteredString(mc.fontRenderer, this.text , this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
                }
            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    // Setters & getters

    public void setTextAlpha(float textAlpha, float textHoverAlpha) {
        this.textAlpha = textAlpha;
        this.textHoverAlpha = textHoverAlpha;
    }

    public void setImageAlpha(float imageAlpha, float imageHoverAlpha) {
        this.imageAlpha = imageAlpha;
        this.imageHoverAlpha = imageHoverAlpha;
    }

    public void setButtonAlpha(float buttonAlpha, float buttonHoverAlpha) {
        this.buttonAlpha = buttonAlpha;
        this.buttonHoverAlpha = buttonHoverAlpha;
    }

    public GuiImageButton setButtonTexture(ResourceLocation buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public GuiImageButton setButtonTexture(String buttonTexture){
        return setButtonTexture(new ResourceLocation(ModGlobals.MOD_ID, buttonTexture));
    }

    public GuiImageButton setAllImages(ResourceLocation resource, int offsetX, int offsetY){
        setImage(resource, offsetX, offsetY);
        setHoverImage(resource, offsetX, offsetY);
        return this;
    }

    public GuiImageButton setImage(ResourceLocation resource, int offsetX, int offsetY){
        this.image = resource;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    public GuiImageButton setImage(String resource, int offsetX, int offsetY){
        return setImage(new ResourceLocation(ModGlobals.MOD_ID, resource), offsetX, offsetY);
    }

    public GuiImageButton setImage(String resource){
        return setImage(resource, 0, 0);
    }

    public GuiImageButton setImage(ResourceLocation resource){
        return setImage(resource, 0, 0);
    }

    public GuiImageButton setHoverImage(ResourceLocation resource, int offsetX, int offsetY){
        this.hoverImage = resource;
        this.hoverOffsetX = offsetX;
        this.hoverOffsetY = offsetY;
        return this;
    }

    public GuiImageButton setHoverImage(String resource, int offsetX, int offsetY){
        return setHoverImage(new ResourceLocation(ModGlobals.MOD_ID, resource), offsetX, offsetY);
    }

    public GuiImageButton setHoverImage(String resource){
        return setHoverImage(resource, 0, 0);
    }

    public GuiImageButton setHoverImage(ResourceLocation resource){
        return setHoverImage(resource, 0, 0);
    }

    public GuiImageButton setAllText(String text){
        setText(text);
        setHoverText(text);
        return this;
    }

    public GuiImageButton setText(String text){
        this.text = this.displayString = text;
        if(this.hoverText == null) this.hoverText = text;
        return this;
    }

    public GuiImageButton setHoverText(String text){
        this.hoverText = text;
        return this;
    }

    public ResourceLocation getImage(){
        return this.image;
    }

    public ResourceLocation getHoverImage(){
        return this.hoverImage;
    }

    public String getText(){
        return this.text;
    }

    public String getHoverText(){
        return this.hoverText;
    }

    public ResourceLocation getButtonTexture() {
        return buttonTexture;
    }

    public boolean isHovered(){
        return this.hovered;
    }
}
