package com.tm.javacide;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class javacideMain extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() { //on app start
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void render() { //update() per frame
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() { //on app end
        batch.dispose();
        image.dispose();
    }
}
