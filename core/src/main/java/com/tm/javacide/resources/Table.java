package com.tm.javacide.resources;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.tm.javacide.javacideMain;
import com.tm.javacide.resources.Card.CardSuit;

public class Table {

	public enum TableType { ENEMYTABLE, PLAYERTABLE }

	private static final List<Table> allTables = new ArrayList<>();

	public final TableType tableType;
	private final CardSuit restrictedSuit;
	private final boolean isExclusionary;

	private boolean isInteractable = true;
	private boolean isOrganized    = false;

	private final float x, y, width, height;
	private final Rectangle bounds;
	private final Texture texture;

	private final ArrayList<Card> capturedCards = new ArrayList<>();

	public Table(TableType tableType, CardSuit restrictedSuit, boolean isExclusionary, float x, float y, Texture texture) {
		this.tableType      = tableType;
		this.restrictedSuit = restrictedSuit;
		this.isExclusionary = isExclusionary;
		this.x              = x;
		this.y              = y;
		this.width          = javacideMain.tableX;
		this.height         = javacideMain.tableY;
		this.texture        = texture;
		this.bounds         = new Rectangle(x, y, width, height);

		allTables.add(this);
		Card.registerContainer(this);
	}

	public void setInteractable(boolean val) { this.isInteractable = val; }
	public boolean isInteractable()          { return isInteractable; }

	public void setOrganized(boolean val) { this.isOrganized = val; }
	public Rectangle getBounds()          { return bounds; }
	public float getX() { return x; }
	public float getY() { return y; }

	public void removeCard(Card card) {
		capturedCards.remove(card);
	}

	public void update(List<Card> allCards) {
		for (Card c : allCards) {
			if (cardOverlaps(c) && !capturedCards.contains(c) && isSuitAllowed(c.getSuit())) {
				capturedCards.add(c);
				c.containedBy = this;
			} else if (!cardOverlaps(c) && capturedCards.contains(c)) {
				capturedCards.remove(c);
				if (c.containedBy == this) c.containedBy = null;
			}
		}

		if (isOrganized && !capturedCards.isEmpty()) {
			float cardW   = javacideMain.cardX;
			float cardH   = javacideMain.cardY;
			float spacing = 20f;
			
			float totalWidth = (capturedCards.size() * cardW) + ((capturedCards.size() - 1) * spacing);
			float startX = x + (width - totalWidth) / 2f;
			float centeredY = y + (height - cardH) / 2f;

			for (int i = 0; i < capturedCards.size(); i++) {
				Card c = capturedCards.get(i);
				// FIX: Use setAutoTarget instead of setPosition so they lerp smoothly inside the table!
				c.setAutoTarget(startX + i * (cardW + spacing), centeredY);
			}
		}
	}

	public boolean isSuitAllowed(CardSuit suit) {
		if (suit == CardSuit.NONE) return false; 
		if (restrictedSuit == null) return true;
		return isExclusionary ? (suit != restrictedSuit) : (suit == restrictedSuit);
	}

	private boolean cardOverlaps(Card card) {
		Rectangle cardRect = new Rectangle(card.getX(), card.getY(), javacideMain.cardX, javacideMain.cardY);
		return bounds.overlaps(cardRect);
	}

	public void render(SpriteBatch batch) {
		Color oldColor = batch.getColor();
		batch.setColor(Color.WHITE);
		
		float thickness = 5f;
		batch.draw(texture, x, y, width, thickness); 
		batch.draw(texture, x, y + height - thickness, width, thickness); 
		batch.draw(texture, x, y, thickness, height); 
		batch.draw(texture, x + width - thickness, y, thickness, height); 

		batch.setColor(oldColor);
	}

	public void dispose() {
		allTables.remove(this);
		Card.unregisterContainer(this);
	}
}