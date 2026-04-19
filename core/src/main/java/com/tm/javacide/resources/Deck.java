package com.tm.javacide.resources;
 
import java.util.ArrayList;
import java.util.List;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
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
 
	private final ArrayList<Card> deckCards = new ArrayList<>();
 
	private Card pendingFront = null;
 
	private boolean wasHeld = false;
 
	public Deck(DeckType deckType, int x, int y, Texture texture) {
		this.deckType = deckType;
		this.texture  = texture;
		this.x        = x;
		this.y        = y;
 
		if (deckType == DeckType.PLAYERDECK) {
			this.clickable = true;
			this.deckSize  = 59;
		} else {
			this.clickable = false;
			this.deckSize  = 12;
		}
	}
 
	public Deck(DeckType deckType, int x, int y, int deckSize, boolean clickable, Texture texture) {
		this.deckType  = deckType;
		this.x         = x;
		this.y         = y;
		this.deckSize  = deckSize;
		this.clickable = clickable;
		this.texture   = texture;
	}
 
	public DeckType getDeckType() { return this.deckType; }
	public int getX() { return this.x; }
	public int getY() { return this.y; }
 
	public List<Card> getCards() {
		return new ArrayList<>(deckCards);
	}
 
	public void spawnCard(int x, int y) {
		Card newCard = new Card(this, x, y, javacideMain.deckTexture);
		deckCards.add(newCard);
	}
 
	public void moveToFront(Card card) {
		if (deckCards.contains(card)) {
			pendingFront = card;
		}
	}
 
	private void handleClick() {
		if (!clickable) return;
 
		boolean holding     = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		boolean justPressed = holding && !wasHeld;
		wasHeld = holding;
 
		if (!justPressed) return;
 
		Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		javacideMain.viewport.unproject(mouse);
 
		boolean hit = mouse.x >= x && mouse.x <= x + javacideMain.deckX
				   && mouse.y >= y && mouse.y <= y + javacideMain.deckY;
 
		if (!hit) return;
 
		if (deckCards.size() >= javacideMain.tableMaxCards) return;
 
		spawnCard(x, y);
	}
 
	public void render(SpriteBatch batch) {
		if (pendingFront != null) {
			deckCards.remove(pendingFront);
			deckCards.add(pendingFront);
			pendingFront = null;
		}
 
		Card.resetClickClaim();
		handleClick();
 
		batch.draw(texture, x, y, javacideMain.deckX, javacideMain.deckY);
 
		List<Card> snapshot = new ArrayList<>(deckCards);
		for (Card c : snapshot) {
			c.render(batch);
		}
	}
 
	public void dispose() {
		texture.dispose();
		for (Card c : deckCards) {
			c.dispose();
		}
	}
}
