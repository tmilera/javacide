package com.tm.javacide.resources;

import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tm.javacide.resources.Deck.DeckType;

/*	CARD PARADIGM:
 * 	card should be able to be spawned and rendered from the position of a deck
 * 	intially draggable and whatnot, with values according to preset
 * 	card moves around to the mouse when clicked on, with some animations of motion
 * 	cards can be brought into your table, of a certain maximal amount, 
 * 	in order to fight i'd imagine that the enemy cards, in the opposing deck
 * 	take your cards and drag onto the enemy card, maybe show a little decision panel:
 * 	::: DO YOU WANT TO ATTACK???? YES : CANCEL ::: something like that
 * 	same for a card's special abilities
 * 	maybe also little right-click menu for each cards to either attack or whatnot, for idiots who dont know as much
 */

public class Card {
	// cards which are passed out, drawn, etc etc
	public enum CardSuit {
		DIAMONDS,
		CLUBS,
		SPADES,
		HEARTS,
	}
	
	// definer variables
	final int value;
	final CardSuit suit;
	final Deck parentDeck;
	
	// action variables
	private boolean clickable;
	private boolean draggable;
	private boolean flippable;
	
	// state variables
	private boolean flipped;
	private boolean clicked;
	
	// positions
	private int x;
	private int y;
	
	// object variables
	private Texture texture;
	
	private static final CardSuit[] suitValues = CardSuit.values();
	
	public Card(Deck parentDeck, int x, int y, Texture texture) {
		// auto creator
		this.parentDeck = parentDeck;
		this.suit = suitValues[generateValue(0,3)];
		this.clickable = true;
		this.draggable = true;
		this.flippable = true;
		this.flipped = false;
		this.clicked = false;
		this.texture = texture;
		
		this.x = x;
		this.y = y;
		
		if (parentDeck.getDeckType()==DeckType.PLAYERDECK) {
			// card from the player's deck the player is supposed to use
			this.value = generateValue(2,11);
		} else {
			// card from the face deck of the enemies you're supposed to beat
			this.value = generateValue(1,4);
		}
	}
	
	public Card(Deck parentDeck, CardSuit suit, int value, boolean clickable, boolean draggable, boolean flippable, boolean flipped, boolean clicked, Texture texture, int x, int y) {
		// manual creation
		this.parentDeck = parentDeck;
		this.suit = suit;
		this.clickable = clickable;
		this.draggable = draggable;
		this.flippable = flippable;
		this.flipped = flipped;
		this.clicked = clicked;
		this.texture = texture;
		this.value = value;
		
		this.x = x;
		this.y = y;
	}
	
	private int generateValue(int low, int high) {
		return ThreadLocalRandom.current().nextInt(low,high);
	}
	
	public void render(SpriteBatch batch) {
		// recall, 0,0 here is position, not scale
		batch.draw(texture, 0, 0);
	}
	
	public void dispose() {
		texture.dispose();
	}
}
