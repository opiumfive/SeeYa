package com.opiumfive.seeya.units;


import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import static com.opiumfive.seeya.GameActivity.CAMERA_WIDTH;


public class Mine extends AnimatedSprite {

    private static final float DEMO_POSITION = 4f * CAMERA_WIDTH;
    private static final float MINE_WATER_LEVEL = 246;
    //private static final float MINE_UNDERWATER_LEVEL = 350;

    public Mine(TiledTextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(DEMO_POSITION, MINE_WATER_LEVEL, pTextureRegion, pVertexBufferObjectManager);
    }

}
