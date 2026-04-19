package com.tm.javacide.resources;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.tm.javacide.javacideMain;
import com.tm.javacide.resources.Table.TableType;
 
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
 
	// Deferred bring-to-front — applied before the next render loop to avoid
	// structural modification during iteration.
	private Card pendingFront = null;
 
	private boolean wasHeld = false;
 
	// -------------------------
	// CONSTRUCTORS
	// -------------------------
 
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
 
	// -------------------------
	// ACCESSORS
	// -------------------------
 
	public DeckType getDeckType() { return this.deckType; }
 
	/**
	 * Returns a snapshot copy of the card list for safe external iteration
	 * (e.g. Table.update). The copy is decoupled from the live list so
	 * concurrent modifications cannot cause ConcurrentModificationException.
	 */
	public List<Card> getCards() {
		return new ArrayList<>(deckCards);
	}
 
	// -------------------------
	// CARD MANAGEMENT
	// -------------------------
 
	public void spawnCard(int x, int y) {
		Card newCard = new Card(this, x, y, javacideMain.deckTexture);
		deckCards.add(newCard);
	}
 
	/**
	 * Schedules a card to become the top render layer.
	 * The actual list mutation is deferred to the start of the next render()
	 * call so it never happens while the card list is being iterated.
	 */
	public void moveToFront(Card card) {
		if (deckCards.contains(card)) {
			pendingFront = card;
		}
	}
 
	// -------------------------
	// CLICK → SPAWN LOGIC
	// -------------------------
 
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
 
		// FIX: Simply check if the number of cards already spawned by this deck
		// has reached the global limit. This ignores whether they are in tables or not.
		if (deckCards.size() >= javacideMain.tableMaxCards) return;
 
		spawnCard(x, y);
	}
 
	// -------------------------
	// RENDER
	// -------------------------
 
	public void render(SpriteBatch batch) {
		// Apply any deferred bring-to-front from the previous frame.
		if (pendingFront != null) {
			deckCards.remove(pendingFront);
			deckCards.add(pendingFront);
			pendingFront = null;
		}
 
		// Reset the per-frame click claim so the topmost card in THIS deck's
		// render pass can claim the click. Each deck resets independently so
		// the top card across all decks wins correctly.
		Card.resetClickClaim();
 
		// Handle click-to-spawn. spawnCard() appends to deckCards, which is
		// safe here because we haven't started iterating yet.
		handleClick();
 
		// Draw the deck base sprite.
		batch.draw(texture, x, y, javacideMain.deckX, javacideMain.deckY);
 
		// Snapshot before iterating. Card.update() calls moveToFront() which
		// now only sets pendingFront, but the snapshot is a safety guarantee
		// against any future structural modifications mid-loop.
		List<Card> snapshot = new ArrayList<>(deckCards);
		for (Card c : snapshot) {
			c.render(batch);
		}
	}
 
	// -------------------------
	// CLEANUP
	// -------------------------
 
	public void dispose() {
		texture.dispose();
		for (Card c : deckCards) {
			c.dispose();
		}
	}
}
