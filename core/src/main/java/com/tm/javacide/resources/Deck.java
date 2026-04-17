package com.tm.javacide.resources;

public class Deck {
	public enum DeckType {
		PLAYERDECK,
		ENEMYDECK,
	}
	
	private DeckType deckType;
	
	Deck() {
		
	}
	
	public DeckType getDeckType() {
		return this.deckType;
	}
}
