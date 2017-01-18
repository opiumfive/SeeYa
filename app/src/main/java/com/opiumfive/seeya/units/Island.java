package com.opiumfive.seeya.units;


import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import static com.opiumfive.seeya.GameActivity.CAMERA_WIDTH;

public class Island extends Sprite {

    private static final float DEMO_POSITION = 1.4f*CAMERA_WIDTH;
    private static final float ISLAND_WATER_LEVEL = 226;

    public Island(ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(DEMO_POSITION, ISLAND_WATER_LEVEL, pTextureRegion, pVertexBufferObjectManager);
    }
}
