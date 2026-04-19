package com.tm.javacide.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.tm.javacide.javacideMain;
import com.tm.javacide.resources.Deck.DeckType;

public class Card {

	public enum CardSuit { DIAMONDS, CLUBS, SPADES, HEARTS, NONE } // Added NONE

	private static final List<Rectangle> obstacles = new ArrayList<>();
	public static void registerObstacle(Rectangle rect)   { obstacles.add(rect); }
	public static void unregisterObstacle(Rectangle rect) { obstacles.remove(rect); }

	private static final List<Table> containers = new ArrayList<>();
	public static void registerContainer(Table table)   { containers.add(table); }
	public static void unregisterContainer(Table table) { containers.remove(table); }

	private static boolean clickClaimedThisFrame = false;
	public static void resetClickClaim() { clickClaimedThisFrame = false; }

	final int value;
	private CardSuit suit;
	final Deck parentDeck;
	Table containedBy = null;

	public CardSuit getSuit() { return suit; }
	public float getX()      { return x; }
	public float getY()      { return y; }

	public void setPosition(float nx, float ny) {
		this.x = nx;
		this.y = ny;
		this.velocityX = 0;
		this.velocityY = 0;
	}

	private boolean clickable, draggable;
	private float x, y, velocityX, velocityY, rotation, targetRotation;
	private boolean dragging = false;
	private float offsetX, offsetY;
	private Texture texture; 
	private static final CardSuit[] suitValues = CardSuit.values();

	private static final float MAX_LEAN = 15f;
	private static final float LEAN_SENSITIVITY = 0.015f;
	private static final float SMOOTH_SPEED = 6f;

	/**
	 * Standard Deck Constructor
	 */
	public Card(Deck parentDeck, int x, int y, Texture ignoredDefaultTexture) {
		this.parentDeck = parentDeck;
		this.suit       = suitValues[ThreadLocalRandom.current().nextInt(0, 4)];
		
		this.value = (parentDeck.getDeckType() == DeckType.PLAYERDECK) 
					 ? ThreadLocalRandom.current().nextInt(1, 11) 
					 : ThreadLocalRandom.current().nextInt(1, 4);

		this.clickable  = true;
		this.draggable  = true;
		this.x          = x;
		this.y          = y;

		String suitName = this.suit.name().toLowerCase();
		String fileName = "cards/card-" + suitName + "-" + this.value + ".png";
		this.texture = new Texture(Gdx.files.internal(fileName));
	}

	/**
	 * Constructor for Special/Standalone Cards (e.g., Info Card)
	 */
	public Card(int x, int y, Texture texture, CardSuit suit) {
		this.parentDeck = null;
		this.suit       = suit;
		this.value      = 0;
		this.clickable  = true;
		this.draggable  = true;
		this.x          = x;
		this.y          = y;
		this.texture    = texture;
	}

	public void update() {
		boolean interactable = (containedBy == null || containedBy.isInteractable());
		float dt = Gdx.graphics.getDeltaTime();

		Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		javacideMain.viewport.unproject(mouse);

		boolean justPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
		boolean holding     = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

		if (interactable && justPressed && !clickClaimedThisFrame && clickable && draggable && contains(mouse.x, mouse.y)) {
			clickClaimedThisFrame = true;
			dragging = true;
			offsetX  = mouse.x - x;
			offsetY  = mouse.y - y;
			velocityX = velocityY = 0;
			
			if (parentDeck != null) { // Null check for standalone cards
				parentDeck.moveToFront(this);
			}
		}

		if (dragging && holding) {
			float prevX = x, prevY = y;
			x = mouse.x - offsetX;
			y = mouse.y - offsetY;
			if (dt > 0) {
				velocityX = (x - prevX) / dt;
				velocityY = (y - prevY) / dt;
			}
			targetRotation = MathUtils.clamp(-velocityX * LEAN_SENSITIVITY, -MAX_LEAN, MAX_LEAN);
		} else {
			targetRotation = 0;
		}

		if (dragging && !holding) dragging = false;

		if (!dragging) {
			x += velocityX * dt;
			y += velocityY * dt;
			velocityX *= 0.90f;
			velocityY *= 0.90f;
		}

		rotation += (targetRotation - rotation) * SMOOTH_SPEED * dt;
		resolveCollisions();
	}

	private void resolveCollisions() {
		float cardW = javacideMain.cardX, cardH = javacideMain.cardY;
		float screenW = javacideMain.WORLD_WIDTH, screenH = javacideMain.WORLD_HEIGHT;

		// Wall collisions - applies to all cards
		if (x < 0) { x = 0; velocityX = 0; }
		if (x + cardW > screenW) { x = screenW - cardW; velocityX = 0; }
		if (y < 0) { y = 0; velocityY = 0; }
		if (y + cardH > screenH) { y = screenH - cardH; velocityY = 0; }

		// Exit early for NONE suit - no container or obstacle collision
		if (this.suit == CardSuit.NONE) return;

		if (containedBy != null) {
			Rectangle b = containedBy.getBounds();
			if (x < b.x) { x = b.x; velocityX = 0; }
			if (x + cardW > b.x + b.width) { x = b.x + b.width - cardW; velocityX = 0; }
			if (y < b.y) { y = b.y; velocityY = 0; }
			if (y + cardH > b.y + b.height) { y = b.y + b.height - cardH; velocityY = 0; }
		}

		Rectangle cardRect = new Rectangle(x, y, cardW, cardH);
		for (Table table : containers) {
			if (containedBy == table || table.isSuitAllowed(this.suit)) continue;
			handleObstacleCollision(cardRect, table.getBounds());
			cardRect.set(x, y, cardW, cardH);
		}
	}

	private void handleObstacleCollision(Rectangle cardRect, Rectangle obs) {
		if (!cardRect.overlaps(obs)) return;
		float overlapLeft = (cardRect.x + cardRect.width) - obs.x;
		float overlapRight = (obs.x + obs.width) - cardRect.x;
		float overlapBottom = (cardRect.y + cardRect.height) - obs.y;
		float overlapTop = (obs.y + obs.height) - cardRect.y;
		float minX = Math.min(overlapLeft, overlapRight);
		float minY = Math.min(overlapBottom, overlapTop);
		if (minX < minY) {
			if (overlapLeft < overlapRight) { x -= overlapLeft; velocityX = 0; }
			else { x += overlapRight; velocityX = 0; }
		} else {
			if (overlapBottom < overlapTop) { y -= overlapBottom; velocityY = 0; }
			else { y += overlapTop; velocityY = 0; }
		}
	}

	private boolean contains(float mx, float my) {
		return mx >= x && mx <= x + javacideMain.cardX && my >= y && my <= y + javacideMain.cardY;
	}

	public void render(SpriteBatch batch) {
		update();
		
		batch.draw(this.texture, 
				x, y,                             
				javacideMain.cardX / 2f,          
				javacideMain.cardY / 2f,          
				javacideMain.cardX,               
				javacideMain.cardY,               
				1f, 1f,                           
				rotation,                         
				0, 0,                             
				this.texture.getWidth(), 
				this.texture.getHeight(), 
				false, false                      
		);
	}

	public void dispose() { 
		if (this.texture != null) {
			this.texture.dispose();
		}
	}
}
 