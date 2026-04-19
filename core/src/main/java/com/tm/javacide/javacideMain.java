package com.tm.javacide;
 
import com.badlogic.gdx.ApplicationAdapter;
//import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tm.javacide.resources.Card;
import com.tm.javacide.resources.Card.CardSuit;
import com.tm.javacide.resources.Deck;
import com.tm.javacide.resources.Deck.DeckType;
import com.tm.javacide.resources.Table;
import com.tm.javacide.resources.Table.TableType;
 
public class javacideMain extends ApplicationAdapter {
 
	public static int tableMaxCards = 5;
	public static int playerHealth = 50;
	public static int playerScore = 0;
	public static int playerCurrentRound = 1;
	
	public static final int WORLD_WIDTH  = 1920;
	public static final int WORLD_HEIGHT = 1080;
 
	public static int cardX = 120;
	public static int cardY = 180;
    
    // New custom dimensions for the info object
    private static int infoX = 200;
    private static int infoY = 200;
 
	public static int deckX = 120;
	public static int deckY = 180;
 
	public static int tableX = 760;
	public static int tableY = 220;
 
	public static Texture deckTexture;
	public static Texture enemyDeckTexture;
	public static Texture tableTexture;
	private static Texture infoTexture;
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
 
	@Override
	public void create() {
		batch = new SpriteBatch();
 
		camera   = new OrthographicCamera();
		viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
		viewport.apply(true);
 
		deckTexture  = new Texture("cards/card-back2.png");
		enemyDeckTexture  = new Texture("cards/card-back1.png");
		tableTexture = new Texture("cards/card-blank.png");
		infoTexture  = new Texture("cards/card-blank.png"); 

		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(0.8f); // Adjusted scale for a smaller 100x100 card
 
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
 
		playerDeck = new Deck(DeckType.PLAYERDECK, deckX_pos, playerTableY, deckTexture);
		enemyDeck  = new Deck(DeckType.ENEMYDECK,  deckX_pos, enemyTableY,  enemyDeckTexture);

		// Initialize info card with custom infoX and infoY
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

		info.render(batch); 
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
		enemyDeckTexture.dispose();
		tableTexture.dispose();
		infoTexture.dispose();
		font.dispose(); 
		info.dispose();
	}
}