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
    private SpriteBatch batch;
    private Texture image;
    private Texture cardimg;
    
    private Card cardtest;
    private Deck decktest;

    @Override
    public void create() { //on app start
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        cardimg = new Texture("size card.png");
        decktest = new Deck(DeckType.PLAYERDECK,0,0,image);
        cardtest = new Card(decktest,20,10,cardimg);
    }

    @Override
    public void render() { //update() per frame
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        cardtest.render(batch);
        batch.end();
    }

    @Override
    public void dispose() { //on app end
        batch.dispose();
        image.dispose();
        cardimg.dispose();
        cardtest.dispose();
        decktest.dispose();
    }
}
