package com.tm.javacide;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tm.javacide.resources.Button;
import com.tm.javacide.resources.Button.ButtonType;
import com.tm.javacide.resources.Card;
import com.tm.javacide.resources.Card.CardSuit;
import com.tm.javacide.resources.Deck;
import com.tm.javacide.resources.Deck.DeckType;
import com.tm.javacide.resources.Panel;
import com.tm.javacide.resources.Table;
import com.tm.javacide.resources.Table.TableType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
 
public class javacideMain extends ApplicationAdapter {
 
	public static javacideMain instance; 

	// player
	public static int tableMaxCards = 5;
	public static int playerHealth = 50;
	public static int playerScore = 0;
	public static int playerCurrentRound = 1;
	
	public static boolean playerPreRound = true;
	public static boolean hasDrawnThisRound = false;
	public static boolean deckLocked = false; 
	public static boolean hasHealedThisTurn = false;
	public static boolean isGameOver = false;

	public int allowedDrawsThisPhase = 0;
	public int cardsDrawnThisPhase = 0;

	// defaults
	public static final int WORLD_WIDTH  = 1920;
	public static final int WORLD_HEIGHT = 1080;
 
	public static int cardX = 120;
	public static int cardY = 180;
    
    private static int infoX = 200;
    private static int infoY = 200;
 
	public static int deckX = 120;
	public static int deckY = 180;
 
	public static int tableX = 760;
	public static int tableY = 220;
	
	private static final int TABLE_GAP = 40;  
	private static final int DECK_GAP  = 40;  
 
	// textures
	public static Texture deckTexture, enemyDeckTexture, tableTexture, infoTexture, buttonTexture, panelTexture, backgroundTexture;;
	public static BitmapFont font; 
 
	// cameras
	public static OrthographicCamera camera;
	public static Viewport viewport;
 
	// sprite renderers
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
 
	// objects
	public Table playerTable;       
	public Table playerClubsTable;  
	public Table enemyTable;        
 
	public Deck playerDeck;         
	public Deck enemyDeck;          
	public static Card info;  // info object, displays info
	private Button autoDrawButton;
	private Button endPreRoundButton; 
	private Button restartButton; 

	public static Panel activePanel = null; 

	public static Card attackingCard = null;
	public static Card targetClubCard = null;
	public static boolean isAttackAnimating = false;
	public static boolean justStartedAttack = false; 
	private Map<Card, Float> clubHoverTimers = new HashMap<>(); // async 
 
	@Override
	public void create() {
		instance = this;
		
		Gdx.graphics.setWindowedMode(1600, 900);

		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
 
		camera   = new OrthographicCamera();
		viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
		viewport.apply(true);
 
		deckTexture  = new Texture("cards/card-back2.png");
		enemyDeckTexture  = new Texture("cards/card-back1.png");
		tableTexture = new Texture("cards/card-blank.png");
		infoTexture  = new Texture("cards/card-info.png"); 
		buttonTexture = new Texture("cards/card-blank.png");
		panelTexture = new Texture("cards/card-blank.png"); 
		backgroundTexture = new Texture("back.png");

		//font defaults
		font = new BitmapFont();
		font.getData().markupEnabled = true;
		font.setColor(Color.WHITE);
		font.getData().setScale(0.8f);
 
		// initialize the static info card once at boot so its texture isn't tied to game restarts
		info = new Card(50, 50, infoX, infoY, infoTexture, CardSuit.NONE);

		initGame();
	}

	public void initGame() {
		// reset player variables for every restart
		tableMaxCards = 5;
		playerHealth = 50;
		playerScore = 0;
		playerCurrentRound = 1;
		
		playerPreRound = true;
		hasDrawnThisRound = false;
		deckLocked = false; 
		hasHealedThisTurn = false;
		isGameOver = false;

		allowedDrawsThisPhase = tableMaxCards;
		cardsDrawnThisPhase = 0;
		
		attackingCard = null;
		targetClubCard = null;
		isAttackAnimating = false;
		justStartedAttack = false;
		clubHoverTimers.clear();

		if (activePanel != null) {
			activePanel.dispose();
			activePanel = null;
		}

		if (playerTable != null) playerTable.dispose();
		if (playerClubsTable != null) playerClubsTable.dispose();
		if (enemyTable != null) enemyTable.dispose();
		if (playerDeck != null) playerDeck.dispose();
		if (enemyDeck != null) enemyDeck.dispose();
		
		if (autoDrawButton != null) autoDrawButton.dispose();
		if (endPreRoundButton != null) endPreRoundButton.dispose();
		if (restartButton != null) restartButton.dispose();

		int tableCenterX = (WORLD_WIDTH - tableX) / 2;
		int totalStackHeight = 3 * tableY + 2 * TABLE_GAP;
		int stackBottomY     = (WORLD_HEIGHT - totalStackHeight) / 2;
 
		int playerTableY      = stackBottomY;
		int playerClubsTableY = stackBottomY + tableY + TABLE_GAP;
		int enemyTableY       = stackBottomY + 2 * (tableY + TABLE_GAP);
 
		int deckX_pos = tableCenterX - deckX - DECK_GAP;
 
		playerTable = new Table(TableType.PLAYERTABLE, CardSuit.CLUBS, true, tableCenterX, playerTableY, tableTexture);
		
		playerClubsTable = new Table(TableType.PLAYERTABLE, CardSuit.CLUBS, false, tableCenterX, playerClubsTableY, tableTexture);
		playerClubsTable.setInteractable(false);
		playerClubsTable.setOrganized(true);
		
		enemyTable = new Table(TableType.ENEMYTABLE, null, false, tableCenterX, enemyTableY, tableTexture);
		enemyTable.setInteractable(false);
		enemyTable.setOrganized(true);
		
		float btnW = tableX / 4f;
		float btnH = tableY / 4f;
		float btnSpacing = 20f;
		float totalBtnGroupWidth = (btnW * 2) + btnSpacing;
		
		float startX = tableCenterX + (tableX / 2f) - (totalBtnGroupWidth / 2f);
		float btnY = playerTableY - btnH - 15f; 
		
		autoDrawButton = new Button(ButtonType.BUTTON, startX, btnY, btnW, btnH, "AUTO-DRAW");
		endPreRoundButton = new Button(ButtonType.BUTTON, startX + btnW + btnSpacing, btnY, btnW, btnH, "END PRE-ROUND");

		float rBtnW = 300f;
		float rBtnH = 80f;
		restartButton = new Button(ButtonType.BUTTON, (WORLD_WIDTH - rBtnW) / 2f, (WORLD_HEIGHT / 2f) - 100f, rBtnW, rBtnH, "RESTART");
 
		playerDeck = new Deck(DeckType.PLAYERDECK, deckX_pos, playerTableY, deckTexture);
		enemyDeck  = new Deck(DeckType.ENEMYDECK,  deckX_pos, enemyTableY,  enemyDeckTexture);

		spawnEnemyCard();
	}

	private void spawnEnemyCard() {
		enemyDeck.spawnCard(enemyDeck.getX(), enemyDeck.getY());
		List<Card> startEnemyCards = enemyDeck.getCards();
		if (!startEnemyCards.isEmpty()) {
			Card startingEnemyCard = startEnemyCards.get(startEnemyCards.size() - 1);
			startingEnemyCard.setAutoTarget(
				enemyTable.getX() + (tableX / 2f) - (cardX / 2f), 
				enemyTable.getY() + (tableY / 2f) - (cardY / 2f)
			);
		}
	}

	public void nextRound() {
		playerCurrentRound++;
		playerScore += 10; // 10 score for each win against a face ad infinitum
		hasDrawnThisRound = false;
		hasHealedThisTurn = false; 
		
		allowedDrawsThisPhase = tableMaxCards - getCardsInHand();
		cardsDrawnThisPhase = 0;
		
		if (allowedDrawsThisPhase <= 0) {
			deckLocked = true;
			playerPreRound = hasPreRoundCards();
		} else {
			deckLocked = false;
			playerPreRound = true;
		}

		spawnEnemyCard();
	}

	public int getCardsInHand() { // get cards i/hand
		int count = 0;
		for (Card c : playerDeck.getCards()) {
			if (c.getSuit() != CardSuit.CLUBS) {
				count++;
			}
		}
		return count;
	}

	public int getEnemyDamage(Card card) { // get enemy hand dmg 
		if (card.containedBy == playerClubsTable) return card.getValue();
		
		int val = card.getValue();
		int baseDamage = (val == 13) ? 20 : (val == 12) ? 15 : 10;
		int suitBonus = 0;
		switch (card.getSuit()) {
			case DIAMONDS: suitBonus = 10; break;
			case HEARTS:   suitBonus = 5;  break;
			case CLUBS:    suitBonus = 2;  break;
			case SPADES:   suitBonus = 0;  break;
			default: break;
		}
		return baseDamage + suitBonus;
	}
 
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}
 
	@Override
	public void render() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        
		Card.resetClickClaim();
 
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		shapeRenderer.setProjectionMatrix(camera.combined);

		if (playerHealth <= 0 && !isGameOver) {
			isGameOver = true;
			deckLocked = true; 
			if (activePanel != null) {
				activePanel.dispose();
				activePanel = null;
			}
		}
 
		batch.begin();
		// background drawn first so it sits below every other object
		if (backgroundTexture != null) {
			batch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		}
		playerTable.render(batch);
		playerClubsTable.render(batch);
		enemyTable.render(batch);
		
		enemyDeck.render(batch);
		playerDeck.render(batch);
		
		autoDrawButton.setClickable(!deckLocked && !isGameOver);
		autoDrawButton.render(batch);
		
		endPreRoundButton.setClickable(deckLocked && playerPreRound && !isGameOver);
		endPreRoundButton.render(batch);

		// info card never disposed and simply renders across
		info.render(batch); 

		if (activePanel != null && !isGameOver) {
			activePanel.render(batch);
		}
		batch.end();

		if (isGameOver) { // if you lose
			Gdx.gl.glEnable(GL20.GL_BLEND);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(0, 0, 0, 0.75f);
			shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
			shapeRenderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);

			batch.begin(); 
			float origScaleX = font.getData().scaleX;
			float origScaleY = font.getData().scaleY;
			font.getData().setScale(3.0f);
			font.setColor(Color.WHITE);
			
			GlyphLayout layout = new GlyphLayout(font, "SCORE: " + Integer.toString(playerScore));
			font.draw(batch, "SCORE: " + Integer.toString(playerScore), (WORLD_WIDTH - layout.width) / 2f, (WORLD_HEIGHT / 2f) + 150f);
			
			font.getData().setScale(origScaleX, origScaleY);

			restartButton.setClickable(true);
			restartButton.render(batch);
			batch.end();

			if (restartButton.isClicked()) { // resets and replaces all cards/rids card arrays
				initGame();
			}

			return; 
		}

		if (attackingCard != null && activePanel != null) {
			activePanel.dispose();
			activePanel = null;
		}

		if (attackingCard != null) {
			handleTargetingLogic();
		}
 
		playerTable.update(playerDeck.getCards());
		playerClubsTable.update(playerDeck.getCards());
		enemyTable.update(enemyDeck.getCards());

		if (!deckLocked && cardsDrawnThisPhase >= allowedDrawsThisPhase) {
			deckLocked = true;
			playerPreRound = hasPreRoundCards(); 
		}

		if(autoDrawButton.isClicked()) {
			handleAutoDraw();
		}
		if (endPreRoundButton.isClicked() && deckLocked && playerPreRound) {
			playerPreRound = false;
		}

		if (activePanel != null && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
			if (!activePanel.isHovered()) {
				activePanel.dispose();
				activePanel = null;
			}
		}
	}

	private void handleTargetingLogic() {
		//targeting card atop other card for attacking animation
		// very slick
		if (isAttackAnimating) {
			float dx = attackingCard.getX() - targetClubCard.getX();
			float dy = attackingCard.getY() - targetClubCard.getY();
			float dist = (float) Math.sqrt(dx * dx + dy * dy);

			if (dist < 15f) { 
				int targetDamage = getEnemyDamage(targetClubCard);
				boolean wasBoss = (targetClubCard.containedBy == enemyTable);

				int damageToPlayer = Math.max(0, targetDamage - attackingCard.getValue());
				playerHealth -= damageToPlayer;
				
				attackingCard.getParentDeck().removeCard(attackingCard);
				targetClubCard.getParentDeck().removeCard(targetClubCard);
				
				attackingCard = null;
				targetClubCard = null;
				isAttackAnimating = false;
				System.out.println("Attack resolved! Player Health is now: " + playerHealth);

				hasHealedThisTurn = false;

				if (wasBoss) {
					nextRound();
				}
			}
			return; 
		}

		Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		viewport.unproject(mouse);
		boolean clickJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
		boolean clickedValidTarget = false;

		boolean clubsEmpty = true;
		for (Card c : playerDeck.getCards()) {
			if (c.containedBy == playerClubsTable) {
				clubsEmpty = false;
				break;
			}
		}

		List<Card> validTargets = new ArrayList<>();
		for (Card c : playerDeck.getCards()) {
			if (c.containedBy == playerClubsTable) validTargets.add(c);
		}
		if (clubsEmpty) {
			for (Card c : enemyDeck.getCards()) {
				if (c.containedBy == enemyTable) validTargets.add(c);
			}
		}

		Gdx.gl.glEnable(GL20.GL_BLEND);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		
		for (Card c : validTargets) {
			boolean isHovered = mouse.x >= c.getX() && mouse.x <= c.getX() + cardX &&
								mouse.y >= c.getY() && mouse.y <= c.getY() + cardY;

			float timer = clubHoverTimers.getOrDefault(c, 0f);
			if (isHovered) {
				timer = Math.min(1f, timer + Gdx.graphics.getDeltaTime() * 8f);
				if (clickJustPressed) {
					targetClubCard = c;
					clickedValidTarget = true;
				}
			} else {
				timer = Math.max(0f, timer - Gdx.graphics.getDeltaTime() * 8f);
			}
			clubHoverTimers.put(c, timer);

			Color borderColor = new Color(1f, 0f, 0f, 1f).lerp(Color.YELLOW, timer);
			shapeRenderer.setColor(borderColor);

			for (int i = 0; i < 4; i++) {
				shapeRenderer.rect(c.getX() - i, c.getY() - i, cardX + (i*2), cardY + (i*2));
			}
		}
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);

		if (clickedValidTarget) { // if the card is not an empty space OR is not face (whilst clubs exist)
			isAttackAnimating = true;
			attackingCard.containedBy = null;
			attackingCard.isAttacking = true; 
			attackingCard.setAutoTarget(targetClubCard.getX(), targetClubCard.getY());
			attackingCard.getParentDeck().moveToFront(attackingCard);
		} else if (clickJustPressed) {
			if (justStartedAttack) {
				// Consume click
			} else {
				attackingCard = null;
			}
		}
		justStartedAttack = false; 
	}

	public boolean hasPreRoundCards() {
		int cardsInHand = getCardsInHand();
		for (Card c : playerDeck.getCards()) {
			if (c.getSuit() == CardSuit.DIAMONDS) {
				if (tableMaxCards - (cardsInHand - 1) > 0) return true;
			}
			if (c.getValue() == 1 && c.getSuit() != CardSuit.CLUBS) {
				if (tableMaxCards < 9) return true;
			}
		}
		return false;
	}

	private void handleAutoDraw() {
		int safetyNet = 0; 
		while (cardsDrawnThisPhase < allowedDrawsThisPhase && safetyNet < 50) {
			playerDeck.spawnCard(playerDeck.getX(), playerDeck.getY());
			cardsDrawnThisPhase++;
			safetyNet++;
		}
		assignFloatingCards();
		deckLocked = true; 
		playerPreRound = hasPreRoundCards();
	}

	public void drawSpecificAmount(int amount) {
		for (int i = 0; i < amount; i++) {
			playerDeck.spawnCard(playerDeck.getX(), playerDeck.getY());
		}
		assignFloatingCards();
	}

	private void assignFloatingCards() { // assigns order
		List<Card> cards = playerDeck.getCards();
		for (Card card : cards) {
			if (card.containedBy != null) continue;

			Table targetTable = null;
			
			if (playerClubsTable.isSuitAllowed(card.getSuit())) {
				targetTable = playerClubsTable;
			} else if (playerTable.isSuitAllowed(card.getSuit())) {
				targetTable = playerTable;
			}

			if (targetTable != null) {
				float randomOffsetX = (float) (Math.random() * 60 - 30);
				float randomOffsetY = (float) (Math.random() * 60 - 30);
				
				card.setAutoTarget(
					targetTable.getX() + (tableX / 2f) - (cardX / 2f) + randomOffsetX, 
					targetTable.getY() + (tableY / 2f) - (cardY / 2f) + randomOffsetY
				);
			}
		}
	}
 
	@Override
	public void dispose() { // rid
		batch.dispose();
		shapeRenderer.dispose();
		if (playerTable != null) playerTable.dispose();
		if (playerClubsTable != null) playerClubsTable.dispose();
		if (enemyTable != null) enemyTable.dispose();
		if (playerDeck != null) playerDeck.dispose();
		if (enemyDeck != null) enemyDeck.dispose();
		if (deckTexture != null) deckTexture.dispose();
		if (enemyDeckTexture != null) enemyDeckTexture.dispose();
		if (tableTexture != null) tableTexture.dispose();
		if (infoTexture != null) infoTexture.dispose();
		if (buttonTexture != null) buttonTexture.dispose();
		if (panelTexture != null) panelTexture.dispose();
		if (backgroundTexture != null) backgroundTexture.dispose();
		if (font != null) font.dispose(); 
		if (info != null) info.dispose();
		if (autoDrawButton != null) autoDrawButton.dispose();
		if (endPreRoundButton != null) endPreRoundButton.dispose();
		if (restartButton != null) restartButton.dispose();
		if (activePanel != null) activePanel.dispose();
	}
}