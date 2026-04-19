package com.tm.javacide;
 
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tm.javacide.resources.Card.CardSuit;
import com.tm.javacide.resources.Deck;
import com.tm.javacide.resources.Deck.DeckType;
import com.tm.javacide.resources.Table;
import com.tm.javacide.resources.Table.TableType;
 
public class javacideMain extends ApplicationAdapter {
 
	public static final int WORLD_WIDTH  = 1920;
	public static final int WORLD_HEIGHT = 1080;
 
	public static int cardX = 120;
	public static int cardY = 180;
 
	public static int deckX = 120;
	public static int deckY = 180;
 
	public static int tableX = 760;
	public static int tableY = 220;
 
	public static int tableMaxCards = 5;
	public static Texture deckTexture;
	public static Texture tableTexture;
 
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
 
	@Override
	public void create() {
		batch = new SpriteBatch();
 
		camera   = new OrthographicCamera();
		viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
		viewport.apply(true);
 
		deckTexture  = new Texture("cards/card-back2.png");
		tableTexture = new Texture("cards/card-blank.png");
 
		int tableCenterX = (WORLD_WIDTH - tableX) / 2;
 
		int totalStackHeight = 3 * tableY + 2 * TABLE_GAP;
		int stackBottomY     = (WORLD_HEIGHT - totalStackHeight) / 2;
 
		int playerTableY      = stackBottomY;
		int playerClubsTableY = stackBottomY + tableY + TABLE_GAP;
		int enemyTableY       = stackBottomY + 2 * (tableY + TABLE_GAP);
 
		int deckX_pos = tableCenterX - deckX - DECK_GAP;
 
		// ---- Tables ----
		
		// All tables now use the 6-parameter constructor for consistency.
		
		// 1. playerTable: Excludes CLUBS (restrictedSuit = CLUBS, isExclusionary = true)
		playerTable = new Table(TableType.PLAYERTABLE, CardSuit.CLUBS, true, tableCenterX, playerTableY, tableTexture);
		
		// 2. playerClubsTable: Only CLUBS (restrictedSuit = CLUBS, isExclusionary = false)
		playerClubsTable = new Table(TableType.PLAYERTABLE, CardSuit.CLUBS, false, tableCenterX, playerClubsTableY, tableTexture);
		playerClubsTable.setInteractable(false); // New behavior: Cannot click cards inside
		playerClubsTable.setOrganized(true);    // New behavior: Centered auto-layout
		
		// 3. enemyTable: No restrictions (restrictedSuit = null, isExclusionary = false)
		enemyTable = new Table(TableType.ENEMYTABLE, null, false, tableCenterX, enemyTableY, tableTexture);
 
		// ---- Decks ----
		playerDeck = new Deck(DeckType.PLAYERDECK, deckX_pos, playerTableY, deckTexture);
		enemyDeck  = new Deck(DeckType.ENEMYDECK,  deckX_pos, enemyTableY,  deckTexture);
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
 
		batch.end();
 
		playerTable.update(playerDeck.getCards());
		playerClubsTable.update(playerDeck.getCards());
		enemyTable.update(enemyDeck.getCards());
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
		tableTexture.dispose();
	}
}