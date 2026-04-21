package com.tm.javacide.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.tm.javacide.javacideMain;

public class Button {

    public enum ButtonType { BUTTON, PANEL } // panel enum virtually useless but too lazy to rewrite

    private ButtonType type;
    private float x, y, width, height;
    private String text;
    private Rectangle bounds;
    
    private Color borderColor = new Color(Color.WHITE);
    private static final float LERP_SPEED = 5f;

    // Controls whether this button can be interacted with or highlight on hover
    private boolean isClickable = true;

    public Button(ButtonType type, float x, float y, float width, float height, String text) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void setClickable(boolean clickable) {
        this.isClickable = clickable;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public boolean isHovered() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        javacideMain.viewport.unproject(mouse);
        return bounds.contains(mouse.x, mouse.y);
    }

    public boolean isClicked() {
        return isClickable && isHovered() && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
    }

    public void update(float dt) {
        // Only glow yellow if it is actively hovered AND clickable
        Color targetColor = (isHovered() && isClickable) ? Color.YELLOW : Color.WHITE;
        
        borderColor.r = com.badlogic.gdx.math.MathUtils.lerp(borderColor.r, targetColor.r, LERP_SPEED * dt);
        borderColor.g = com.badlogic.gdx.math.MathUtils.lerp(borderColor.g, targetColor.g, LERP_SPEED * dt);
        borderColor.b = com.badlogic.gdx.math.MathUtils.lerp(borderColor.b, targetColor.b, LERP_SPEED * dt);
        borderColor.a = 1f; 
    }

    public void render(SpriteBatch batch) {
        Texture tex = javacideMain.buttonTexture;
        if (tex == null) return;

        update(Gdx.graphics.getDeltaTime());

        batch.setColor(Color.DARK_GRAY); 
        batch.draw(tex, x, y, width, height);

        // Only draw the thick border if this is a standard BUTTON (like AUTO-DRAW)
        if (this.type != ButtonType.PANEL) {
            batch.setColor(borderColor);
            float thickness = 5f;
            batch.draw(tex, x, y, width, thickness);
            batch.draw(tex, x, y + height - thickness, width, thickness);
            batch.draw(tex, x, y, thickness, height);
            batch.draw(tex, x + width - thickness, y, thickness, height);
        }

        if (text != null && javacideMain.font != null) {
            
            // If it's a panel, tint the text itself yellow on hover since it has no border
            if (this.type == ButtonType.PANEL) {
                javacideMain.font.setColor(borderColor);
            } else {
                javacideMain.font.setColor(Color.WHITE);
            }

            // Save the original font scale so we don't permanently shrink the AUTO-DRAW button
            float origScaleX = javacideMain.font.getData().scaleX;
            float origScaleY = javacideMain.font.getData().scaleY;

            // Make the text slightly smaller exclusively for Panel buttons
            if (this.type == ButtonType.PANEL) {
                javacideMain.font.getData().setScale(origScaleX * 0.7f);
            }

            GlyphLayout layout = new GlyphLayout(javacideMain.font, text, javacideMain.font.getColor(), width, Align.center, true);
            float textY = y + (height + layout.height) / 2;
            javacideMain.font.draw(batch, text, x, textY, width, Align.center, true);
            
            // Revert the font scale back to normal so other UI elements render correctly
            if (this.type == ButtonType.PANEL) {
                javacideMain.font.getData().setScale(origScaleX, origScaleY);
            }
        }
        
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
    }
}