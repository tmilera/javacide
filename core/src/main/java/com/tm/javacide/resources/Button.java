package com.tm.javacide.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.tm.javacide.javacideMain;

public class Button {

    public enum ButtonType { BUTTON, PANEL }

    private ButtonType type;
    private float x, y, width, height;
    private String text;
    private Rectangle bounds;
    
    private Color borderColor = new Color(Color.WHITE);
    private static final float LERP_SPEED = 5f;

    public Button(ButtonType type, float x, float y, float width, float height, String text) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public boolean isHovered() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        javacideMain.viewport.unproject(mouse);
        return bounds.contains(mouse.x, mouse.y);
    }

    public boolean isClicked() {
        return isHovered() && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
    }

    public void update(float dt) {
        Color targetColor = isHovered() ? Color.YELLOW : Color.WHITE;
        
        // FIX: Safely lerp the RGB values, forcing Alpha to 1 to prevent disappearing glitches
        borderColor.r = com.badlogic.gdx.math.MathUtils.lerp(borderColor.r, targetColor.r, LERP_SPEED * dt);
        borderColor.g = com.badlogic.gdx.math.MathUtils.lerp(borderColor.g, targetColor.g, LERP_SPEED * dt);
        borderColor.b = com.badlogic.gdx.math.MathUtils.lerp(borderColor.b, targetColor.b, LERP_SPEED * dt);
        borderColor.a = 1f; 
    }

    public void render(SpriteBatch batch) {
        // Safe check for the global texture
        Texture tex = javacideMain.buttonTexture;
        if (tex == null) return;

        update(Gdx.graphics.getDeltaTime());

        batch.setColor(Color.DARK_GRAY); 
        batch.draw(tex, x, y, width, height);

        batch.setColor(borderColor);
        float thickness = 5f;
        batch.draw(tex, x, y, width, thickness);
        batch.draw(tex, x, y + height - thickness, width, thickness);
        batch.draw(tex, x, y, thickness, height);
        batch.draw(tex, x + width - thickness, y, thickness, height);

        if (text != null && javacideMain.font != null) {
            javacideMain.font.setColor(Color.WHITE);
            GlyphLayout layout = new GlyphLayout(javacideMain.font, text);
            float textX = x + (width - layout.width) / 2;
            float textY = y + (height + layout.height) / 2;
            javacideMain.font.draw(batch, text, textX, textY);
        }
        
        // Reset the batch back to default
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        // FIX: Do NOT dispose javacideMain.buttonTexture here! 
        // Calling dispose here destroys the global texture causing all other buttons to turn invisible.
    }
}