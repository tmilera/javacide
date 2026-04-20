package com.tm.javacide.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.tm.javacide.javacideMain;
import com.tm.javacide.resources.Deck.DeckType;

public class Card {

	public enum CardSuit { DIAMONDS, CLUBS, SPADES, HEARTS, NONE }

	private static final List<Rectangle> obstacles = new ArrayList<>();
	public static void registerObstacle(Rectangle rect)   { obstacles.add(rect); }
	public static void unregisterObstacle(Rectangle rect) { obstacles.remove(rect); }

	private static final List<Table> containers = new ArrayList<>();
	public static void registerContainer(Table table)   { containers.add(table); }
	public static void unregisterContainer(Table table) { containers.remove(table); }

	private static boolean clickClaimedThisFrame = false;
	public static void resetClickClaim() { clickClaimedThisFrame = false; }

	int value; 
	private CardSuit suit;
	final Deck parentDeck;
	public Table containedBy = null; 
	
	// FIX: Flag to disable collisions and interactions while attacking
	public boolean isAttacking = false;

	public CardSuit getSuit() { return suit; }
	public float getX()      { return x; }
	public float getY()      { return y; }
	public int getValue()    { return value; }
	public Deck getParentDeck() { return parentDeck; }

	public void setPosition(float nx, float ny) {
		this.x = nx;
		this.y = ny;
		this.velocityX = 0;
		this.velocityY = 0;
		this.isAutoMoving = false; 
	}

	private boolean clickable, draggable;
	private float x, y, width, height, velocityX, velocityY, rotation, targetRotation;
	private boolean dragging = false;
	private float offsetX, offsetY;
	private Texture texture; 
	private static final CardSuit[] suitValues = CardSuit.values();

	private static final float MAX_LEAN = 15f;
	private static final float LEAN_SENSITIVITY = 0.015f;
	private static final float SMOOTH_SPEED = 6f;

	private float targetX, targetY;
	private boolean isAutoMoving = false;
	private static final float MOVE_SPEED = 5f;

	public boolean isAutoMoving() { return isAutoMoving; }

	public Card(Deck parentDeck, int x, int y, Texture ignoredDefaultTexture) {
		this.parentDeck = parentDeck;
		this.suit       = suitValues[ThreadLocalRandom.current().nextInt(0, 4)];
		this.value      = (parentDeck.getDeckType() == DeckType.PLAYERDECK) 
					      ? ThreadLocalRandom.current().nextInt(1, 11) 
					      : ThreadLocalRandom.current().nextInt(11, 14);

		if (parentDeck.getDeckType() == DeckType.PLAYERDECK && javacideMain.tableMaxCards >= 9) {
			while (this.value == 1 && this.suit != CardSuit.CLUBS) {
				this.suit  = suitValues[ThreadLocalRandom.current().nextInt(0, 4)];
				this.value = ThreadLocalRandom.current().nextInt(1, 11);
			}
		}

		this.clickable  = true;
		this.draggable  = true;
		this.x          = x;
		this.y          = y;
		this.width      = javacideMain.cardX;
		this.height     = javacideMain.cardY;

		String suitName = this.suit.name().toLowerCase();
		String fileName = "cards/card-" + suitName + "-" + this.value + ".png";
		this.texture = new Texture(Gdx.files.internal(fileName));
	}

	public Card(int x, int y, float width, float height, Texture texture, CardSuit suit) {
		this.parentDeck = null;
		this.suit       = suit;
		this.value      = 0;
		this.clickable  = true;
		this.draggable  = true;
		this.x          = x;
		this.y          = y;
		this.width      = width;
		this.height     = height;
		this.texture    = texture;
	}

	public void setAutoTarget(float tx, float ty) {
		this.targetX = tx;
		this.targetY = ty;
		this.isAutoMoving = true;
	}

	public void update() {
		boolean interactable = (containedBy == null || containedBy.isInteractable());
		float dt = Gdx.graphics.getDeltaTime();

		Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		javacideMain.viewport.unproject(mouse);

		boolean justPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
		boolean rightJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
		boolean holding     = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

		// Prevent clicking/interacting while attacking
		if (rightJustPressed && contains(mouse.x, mouse.y) && containedBy != null && !isAttacking) {
			if (javacideMain.activePanel != null && javacideMain.activePanel.getTargetCard() == this) {
				javacideMain.activePanel.dispose();
				javacideMain.activePanel = null;
			} else {
				if (javacideMain.activePanel != null) {
					javacideMain.activePanel.dispose();
				}

				float panelX = x + width + 10f;
				float panelW = this.width;
				float panelH = this.height;
				
				javacideMain.activePanel = new Panel(this, panelX, y, panelW, panelH);
			}
		}

		if (interactable && justPressed && !clickClaimedThisFrame && clickable && draggable && contains(mouse.x, mouse.y) && !isAttacking) {
			clickClaimedThisFrame = true;
			dragging = true;
			isAutoMoving = false; 
			offsetX  = mouse.x - x;
			offsetY  = mouse.y - y;
			velocityX = velocityY = 0;
			if (parentDeck != null) parentDeck.moveToFront(this);
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

		if (!dragging && !isAutoMoving) {
			x += velocityX * dt;
			y += velocityY * dt;
			velocityX *= 0.90f;
			velocityY *= 0.90f;
		}

		if (isAutoMoving) {
			x = MathUtils.lerp(x, targetX, dt * MOVE_SPEED);
			y = MathUtils.lerp(y, targetY, dt * MOVE_SPEED);
			
			if (Math.abs(x - targetX) < 1f && Math.abs(y - targetY) < 1f) {
				x = targetX;
				y = targetY;
				isAutoMoving = false;
			}
		}

		rotation += (targetRotation - rotation) * SMOOTH_SPEED * dt;
		resolveCollisions();
	}

	private void resolveCollisions() {
		// FIX: Bypass all collision and containment logic while attacking
		if (isAttacking) return;

		float screenW = javacideMain.WORLD_WIDTH, screenH = javacideMain.WORLD_HEIGHT;

		if (x < 0) { x = 0; velocityX = 0; }
		if (x + width > screenW) { x = screenW - width; velocityX = 0; }
		if (y < 0) { y = 0; velocityY = 0; }
		if (y + height > screenH) { y = screenH - height; velocityY = 0; }

		if (this.suit == CardSuit.NONE) return;

		if (containedBy != null) {
			Rectangle b = containedBy.getBounds();
			if (x < b.x) { x = b.x; velocityX = 0; }
			if (x + width > b.x + b.width) { x = b.x + b.width - width; velocityX = 0; }
			if (y < b.y) { y = b.y; velocityY = 0; }
			if (y + height > b.y + b.height) { y = b.y + b.height - height; velocityY = 0; }
		}

		if (isAutoMoving) return;

		Rectangle cardRect = new Rectangle(x, y, width, height);
		for (Table table : containers) {
			if (containedBy == table || table.isSuitAllowed(this.suit)) continue;
			handleObstacleCollision(cardRect, table.getBounds());
			cardRect.set(x, y, width, height);
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
		return mx >= x && mx <= x + width && my >= y && my <= y + height;
	}

	public void render(SpriteBatch batch) {
		update();
		
		batch.draw(this.texture, 
				x, y,                             
				width / 2f,          
				height / 2f,          
				width,               
				height,               
				1f, 1f,                           
				rotation,                         
				0, 0,                             
				this.texture.getWidth(), 
				this.texture.getHeight(), 
				false, false                      
		);

		if (this.suit == CardSuit.NONE && javacideMain.font != null) {
			float textPaddingX = 30f;
			float textPaddingY = 30f;
			
			float baseScale = (width / 100f) * 0.7f; 
			javacideMain.font.getData().setScale(baseScale);
			javacideMain.font.setColor(Color.BLACK);

			float lineSpacing = 20f * baseScale;
			
			javacideMain.font.draw(batch, "Hand Size: " + javacideMain.tableMaxCards, x + textPaddingX, y + height - textPaddingY);
			javacideMain.font.draw(batch, "Health: " + javacideMain.playerHealth, x + textPaddingX, y + height - textPaddingY - lineSpacing);
			javacideMain.font.draw(batch, "Score: " + javacideMain.playerScore, x + textPaddingX, y + height - textPaddingY - (lineSpacing * 2));
			javacideMain.font.draw(batch, "Round: " + javacideMain.playerCurrentRound, x + textPaddingX, y + height - textPaddingY - (lineSpacing * 3));
            
            String preRoundText = javacideMain.playerPreRound ? "Yes" : "No";
            javacideMain.font.draw(batch, "Pre-Round: " + preRoundText, x + textPaddingX, y + height - textPaddingY - (lineSpacing * 4));
		}
	}

	public void dispose() { 
		if (this.texture != null) this.texture.dispose();
	}
}