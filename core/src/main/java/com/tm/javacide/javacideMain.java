package com.tm.javacide;
 
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import java.util.List;
 
public class javacideMain extends ApplicationAdapter {
 
	public static int tableMaxCards = 5;
	public static int playerHealth = 50;
	public static int playerScore = 0;
	public static int playerCurrentRound = 1;
	
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
 
	public static Texture deckTexture;
	public static Texture enemyDeckTexture;
	public static Texture tableTexture;
	public static Texture infoTexture;
	public static Texture buttonTexture;
	public static Texture panelTexture; // NEW: Texture for the context panel
	public static BitmapFont font; 
 
	public static OrthographicCamera camera;
	public static Viewport viewport;
 
	private SpriteBatch batch;
 
	private static final int TABLE_GAP = 40;  
	private static final int DECK_GAP  = 40;  
 
	private Table playerTable;       
	private Table playerClubsTable;  
	private Table enemyTable;        
 
	private Deck playerDeck;         
	private Deck enemyDeck;          
	public static Card info; 
	private Button autoDrawButton;

	public static Panel activePanel = null; // NEW: Global reference to the currently open panel
 
	@Override
	public void create() {
		batch = new SpriteBatch();
 
		camera   = new OrthographicCamera();
		viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
		viewport.apply(true);
 
		deckTexture  = new Texture("cards/card-back2.png");
		enemyDeckTexture  = new Texture("cards/card-back1.png");
		tableTexture = new Texture("cards/card-blank.png");
		infoTexture  = new Texture("cards/card-info.png"); 
		buttonTexture = new Texture("cards/card-blank.png");
		panelTexture = new Texture("cards/card-blank.png"); // Initialize panel texture

		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(0.8f);
 
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
		
		float autoDrawW = tableX / 4f;
		float autoDrawH = tableY / 4f;
		float autoDrawX = tableCenterX + (tableX / 2f) - (autoDrawW / 2f);
		float autoDrawY = playerTableY - autoDrawH - 15f; 
		
		autoDrawButton = new Button(ButtonType.BUTTON, autoDrawX, autoDrawY, autoDrawW, autoDrawH, "AUTO-DRAW");
 
		playerDeck = new Deck(DeckType.PLAYERDECK, deckX_pos, playerTableY, deckTexture);
		enemyDeck  = new Deck(DeckType.ENEMYDECK,  deckX_pos, enemyTableY,  enemyDeckTexture);

		info = new Card(50, 50, infoX, infoY, infoTexture, CardSuit.NONE);
	}
 
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}
 
	@Override
	public void render() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
 
		camera.update();
		batch.setProjectionMatrix(camera.combined);
 
		batch.begin();
		playerTable.render(batch);
		playerClubsTable.render(batch);
		enemyTable.render(batch);
		playerDeck.render(batch);
		enemyDeck.render(batch);
		
		autoDrawButton.render(batch);

		info.render(batch); 

		// Render active panel on top of everything
		if (activePanel != null) {
			activePanel.render(batch);
		}

		batch.end();
 
		playerTable.update(playerDeck.getCards());
		playerClubsTable.update(playerDeck.getCards());
		enemyTable.update(enemyDeck.getCards());

		if(autoDrawButton.isClicked()) {
			handleAutoDraw();
		}

		// LOGIC: Dismiss the active panel if you left click anywhere EXCEPT on the panel itself
		if (activePanel != null && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
			if (!activePanel.isHovered()) {
				activePanel.dispose();
				activePanel = null;
			}
		}
	}

	private void handleAutoDraw() {
		while (playerDeck.getCards().size() < javacideMain.tableMaxCards) {
			playerDeck.spawnCard(playerDeck.getX(), playerDeck.getY());
		}

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
	public void dispose() {
		batch.dispose();
		playerTable.dispose();
		playerClubsTable.dispose();
		enemyTable.dispose();
		playerDeck.dispose();
		enemyDeck.dispose();
		deckTexture.dispose();
		enemyDeckTexture.dispose();
		tableTexture.dispose();
		infoTexture.dispose();
		buttonTexture.dispose();
		panelTexture.dispose();
		font.dispose(); 
		info.dispose();
		autoDrawButton.dispose();
		if (activePanel != null) activePanel.dispose();
	}
}