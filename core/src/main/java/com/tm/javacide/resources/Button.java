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
    private Texture texture;
    private Rectangle bounds;
    
    // Color tweening variables
    private Color borderColor = new Color(Color.WHITE);
    private float lerpTarget = 0f; // 0 = White, 1 = Yellow
    private static final float LERP_SPEED = 5f;

    public Button(ButtonType type, float x, float y, float width, float height, String text) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        // Default texture as requested
        this.texture = javacideMain.buttonTexture;
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
        // Update lerp target based on hover state
        lerpTarget = isHovered() ? 1f : 0f;

        // Smoothly transition the color
        borderColor.r = com.badlogic.gdx.math.MathUtils.lerp(borderColor.r, isHovered() ? Color.YELLOW.r : Color.WHITE.r, LERP_SPEED * dt);
        borderColor.g = com.badlogic.gdx.math.MathUtils.lerp(borderColor.g, isHovered() ? Color.YELLOW.g : Color.WHITE.g, LERP_SPEED * dt);
        borderColor.b = com.badlogic.gdx.math.MathUtils.lerp(borderColor.b, isHovered() ? Color.YELLOW.b : Color.WHITE.b, LERP_SPEED * dt);
    }

    public void render(SpriteBatch batch) {
        update(Gdx.graphics.getDeltaTime());

        // 1. Draw Background (Panel/Button body)
        batch.setColor(Color.DARK_GRAY); // Dim the background texture slightly
        batch.draw(texture, x, y, width, height);

        // 2. Draw Border (5 pixel width)
        batch.setColor(borderColor);
        float thickness = 5f;
        // Bottom
        batch.draw(texture, x, y, width, thickness);
        // Top
        batch.draw(texture, x, y + height - thickness, width, thickness);
        // Left
        batch.draw(texture, x, y, thickness, height);
        // Right
        batch.draw(texture, x + width - thickness, y, thickness, height);

        // 3. Draw Text
        if (text != null && javacideMain.font != null) {
            javacideMain.font.setColor(Color.WHITE);
            // Center the text
            GlyphLayout layout = new GlyphLayout(javacideMain.font, text);
            float textX = x + (width - layout.width) / 2;
            float textY = y + (height + layout.height) / 2;
            javacideMain.font.draw(batch, text, textX, textY);
        }

        // Reset batch color for next objects
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}