package com.tm.javacide;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.tm.javacide.resources.Card;
import com.tm.javacide.resources.Deck;
import com.tm.javacide.resources.Deck.DeckType;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class javacideMain extends ApplicationAdapter {
	
	//****************************
	public static int cardX = 100;
	public static int cardY = 200;
	//
	public static int deckX = 100;
	public static int deckY = 100;
	//
	public static int tableX = 200;
	public static int tableY = 50;
	//
	public static Texture cardTexture;
	public static Texture deckTexture;
	public static Texture tableTexture;
	//****************************
	
    private SpriteBatch batch;
    private Deck decktest;

    @Override
    public void create() { //on app start
        batch = new SpriteBatch();
        
        cardTexture = new Texture("cardTemp.png");
        deckTexture = new Texture("cardTemp.png");
        tableTexture = new Texture("cardTemp.png");
        
        decktest = new Deck(DeckType.PLAYERDECK,0,0,deckTexture);
        decktest.spawnCard(0, 0);
    }

    @Override
    public void render() { //update() per frame
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        
        decktest.render(batch);
        
        batch.end();
    }

    @Override
    public void dispose() { //on app end
        batch.dispose();
        decktest.dispose();
    }
}
