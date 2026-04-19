package com.tm.javacide.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.tm.javacide.javacideMain;

public class Panel {

    private Card targetCard;
    private float x, y, width, height;
    private Rectangle bounds;
    private Button[] subButtons;

    public Panel(Card targetCard, float x, float y, float width, float height) {
        this.targetCard = targetCard;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);

        // 1. Determine Button Content based on Suit
        String[] buttonTexts;
        boolean[] clickables;
        int value = targetCard.getValue();

        switch(targetCard.getSuit()) {
            case CLUBS:
                buttonTexts = new String[] { "Enemy Card\nAttacks for: " + value };
                clickables = new boolean[] { false };
                break;
            case SPADES:
                buttonTexts = new String[] { "Attack for: " + value };
                clickables = new boolean[] { true };
                break;
            case DIAMONDS:
                buttonTexts = new String[] { "Draw for: " + value, "Attack for: " + value };
                clickables = new boolean[] { true, true };
                break;
            case HEARTS:
                buttonTexts = new String[] { "Heal for: " + value };
                clickables = new boolean[] { true };
                break;
            default:
                buttonTexts = new String[0];
                clickables = new boolean[0];
                break;
        }

        int numButtons = buttonTexts.length;
        this.subButtons = new Button[numButtons];

        // 2. Format Button Geometry inside the Panel
        float padding = 10f;
        float btnWidth = width - (padding * 2);
        
        // Force the layout to calculate slots as if there are at least 2 buttons
        int maxSlots = Math.max(2, numButtons);
        
        float slotHeight = (height - (padding * (maxSlots + 1))) / maxSlots;
        float btnHeight = slotHeight - 6f; 

        for (int i = 0; i < numButtons; i++) {
            float btnX = x + padding;
            
            // This naturally starts at the top slot and moves down
            float slotY = y + height - padding - (slotHeight * (i + 1)) - (padding * i);
            float btnY = slotY + 3f; 
            
            subButtons[i] = new Button(Button.ButtonType.PANEL, btnX, btnY, btnWidth, btnHeight, buttonTexts[i]);
            subButtons[i].setClickable(clickables[i]);
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
        for (int i = 0; i < subButtons.length; i++) {
            Button b = subButtons[i];
            b.render(batch);

            if (b.isClicked()) {
                handleAction(i);
            }
        }
    }

    private void handleAction(int buttonIndex) {
        switch(targetCard.getSuit()) {
            case SPADES:
                if (buttonIndex == 0) {
                    System.out.println("Spades attacked for: " + targetCard.getValue());
                }
                break;
            case DIAMONDS:
                if (buttonIndex == 0) {
                    System.out.println("Diamonds drew for: " + targetCard.getValue());
                } else if (buttonIndex == 1) {
                    System.out.println("Diamonds attacked for: " + targetCard.getValue());
                }
                break;
            case HEARTS:
                if (buttonIndex == 0) {
                    System.out.println("Hearts healed for: " + targetCard.getValue());
                }
                break;
            case CLUBS:
                break;
            default:
                break;
        }
    }

    public void dispose() {
        for (Button b : subButtons) {
            b.dispose();
        }
    }
}