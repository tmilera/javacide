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
			this.deckSize  = 52;
		}
	}
 
	public DeckType getDeckType() { return deckType; }
	public int getX() { return x; }
	public int getY() { return y; }
	public int getHandSize() { return deckCards.size(); }
	public List<Card> getCards() { return deckCards; }
 
	public void spawnCard(float startX, float startY) {
		Card newCard = new Card(this, (int)startX, (int)startY, texture);
		deckCards.add(newCard);
	}
 
	public void moveToFront(Card card) {
		pendingFront = card;
	}
 
	public void removeCard(Card card) {
		deckCards.remove(card);
		if (card.containedBy != null) {
			card.containedBy.removeCard(card);
			card.containedBy = null;
		}
		card.dispose();
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
 
		if (this.deckType == DeckType.PLAYERDECK) {
		    if (javacideMain.deckLocked) return;
		    
		    // FIX: Tie manual clicking strictly to the allowed draws phase limit
		    if (javacideMain.instance.cardsDrawnThisPhase >= javacideMain.instance.allowedDrawsThisPhase) return;
		    
		    spawnCard(x, y);
		    javacideMain.instance.cardsDrawnThisPhase++;
		} else {
		    if (deckCards.size() >= javacideMain.tableMaxCards) return;
		    spawnCard(x, y);
		}
	}
 
	public void render(SpriteBatch batch) {
		handleClick();
 
		batch.draw(texture, x, y, javacideMain.deckX, javacideMain.deckY);
 
		if (pendingFront != null) {
			deckCards.remove(pendingFront);
			deckCards.add(pendingFront);
			pendingFront = null;
		}
 
		for (Card c : deckCards) {
			c.render(batch);
		}
	}
 
	public void dispose() {
		for (Card c : deckCards) c.dispose();
		deckCards.clear();
	}
}