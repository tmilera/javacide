package com.tm.javacide.resources;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tm.javacide.javacideMain;

public class Deck {
	public enum DeckType {
		PLAYERDECK,
		ENEMYDECK,
	}
	
	final DeckType deckType;
	final int deckSize;
	private boolean clickable;
	
	private int x;
	private int y;
	
	private Texture texture;
	
	private ArrayList<Card> deckCards = new ArrayList<>();
	
	public Deck(DeckType deckType, int x, int y, Texture texture) {
		// usual constructor
		this.deckType = deckType;
		this.texture = texture;
		this.x = x;
		this.y = y;
		
		if (deckType == DeckType.PLAYERDECK) {
			this.clickable = true;
			this.deckSize = 59; // i dont know the actual size
		} else {
			this.clickable = false;
			this.deckSize = 12; // i dont know the actual size here either
		}
	}
	
	public Deck(DeckType deckType, int x, int y, int deckSize, boolean clickable, Texture texture) {
		this.deckType = deckType;
		this.x = x;
		this.y = y;
		this.deckSize = deckSize;
		this.clickable = clickable;
		this.texture = texture;
	}
	
	public DeckType getDeckType() {
		return this.deckType;
	}
	
	public void render(SpriteBatch batch) { // renders deck texture
		batch.draw(texture, 0, 0, javacideMain.deckX,javacideMain.deckY);
		
		for (Card c : deckCards) { // renders through every card that exists in it's deck
			c.render(batch);
		}
	}
	
	public void dispose() {
		texture.dispose();
		
		for (Card c : deckCards) { // renders through every card that exists in it's deck
			c.dispose();
		}
	}
	
	public void spawnCard(int x, int y) { // spawn card add to deck
		Card newCard = new Card(this, x, y, javacideMain.deckTexture);
		deckCards.add(newCard);
	}
}
