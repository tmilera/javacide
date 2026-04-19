package com.tm.javacide.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.tm.javacide.javacideMain;

public class Panel {

    private Card targetCard;
    private float x, y, width, height;
    private Rectangle bounds;
    private Button[] subButtons;

    public Panel(Card targetCard, float x, float y, float width, float height, int numButtons) {
        this.targetCard = targetCard;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);

        this.subButtons = new Button[numButtons];

        float padding = 10f;
        float btnWidth = width - (padding * 2);
        
        // Calculate the physical space allocated for each button's slot
        float slotHeight = (height - (padding * (numButtons + 1))) / numButtons;
        
        // Make the actual button 6 pixels thinner than the slot height
        float btnHeight = slotHeight - 6f;

        for (int i = 0; i < numButtons; i++) {
            float btnX = x + padding;
            
            // Find the Y position of the slot
            float slotY = y + height - padding - (slotHeight * (i + 1)) - (padding * i);
            
            // Shift the button up by 3 pixels to keep it vertically centered inside its slot
            float btnY = slotY + 3f;
            
            subButtons[i] = new Button(Button.ButtonType.PANEL, btnX, btnY, btnWidth, btnHeight, "Action " + (i + 1));
        }
    }

    public Card getTargetCard() {
        return targetCard;
    }

    public boolean isHovered() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        javacideMain.viewport.unproject(mouse);
        return bounds.contains(mouse.x, mouse.y);
    }

    public void render(SpriteBatch batch) {
        // The background and border drawing logic has been completely removed 
        // to make the main panel invisible.

        // Render sub-buttons
        for (Button b : subButtons) {
            b.render(batch);
        }
    }

    public void dispose() {
        for (Button b : subButtons) {
            b.dispose();
        }
    }
}