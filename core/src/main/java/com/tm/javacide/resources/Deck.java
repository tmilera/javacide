package com.tm.javacide.resources;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
	
	public void render(SpriteBatch batch) {
		batch.draw(texture, 100, 100);
	}
	
	public void dispose() {
		texture.dispose();
	}
}
