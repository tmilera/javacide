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
	private final ArrayList<Card> pendingCapture = new ArrayList<>();
	private final ArrayList<Card> pendingRelease = new ArrayList<>();

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

		Card.registerContainer(this);
		allTables.add(this);
	}

	public void setInteractable(boolean val) { this.isInteractable = val; }
	public void setOrganized(boolean val)    { this.isOrganized = val; }
	public boolean isInteractable()          { return isInteractable; }

	public Rectangle getBounds()    { return bounds; }
	public int getRemainingSpace() { return Math.max(0, javacideMain.tableMaxCards - capturedCards.size()); }

	public void update(List<Card> allCards) {
		pendingCapture.clear();
		pendingRelease.clear();

		for (Card card : allCards) {
			if (card.containedBy == this) continue;
			if (card.containedBy != null) continue;
			if (cardOverlaps(card) && isSuitAllowed(card.getSuit())
					&& (capturedCards.size() + pendingCapture.size()) < javacideMain.tableMaxCards) {
				pendingCapture.add(card);
			}
		}
		for (Card card : capturedCards) {
			if (!cardOverlaps(card)) pendingRelease.add(card);
		}
		for (Card card : pendingCapture) {
			capturedCards.add(card);
			card.containedBy = this;
		}
		for (Card card : pendingRelease) {
			capturedCards.remove(card);
			card.containedBy = null;
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
				c.setPosition(startX + i * (cardW + spacing), centeredY);
			}
		}
	}

	public boolean isSuitAllowed(CardSuit suit) {
		if (suit == CardSuit.NONE) return false; // Prevent info card capture
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
		for (Card card : capturedCards) card.containedBy = null;
		capturedCards.clear();
		Card.unregisterContainer(this);
		allTables.remove(this);
	}
}