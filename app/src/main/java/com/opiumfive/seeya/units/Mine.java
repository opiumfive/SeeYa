package com.opiumfive.seeya.units;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;


import static com.opiumfive.seeya.GameActivity.CAMERA_WIDTH;


public class Mine extends Sprite {

    private static final float DEMO_VELOCITY = 150.0f;
    private static final float DEMO_POSITION = 1.1f*CAMERA_WIDTH;
    private static final float MINE_WATER_LEVEL = 246;

    private final PhysicsHandler mPhysicsHandler;

    public Mine(ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(DEMO_POSITION, MINE_WATER_LEVEL, pTextureRegion, pVertexBufferObjectManager);
        mPhysicsHandler = new PhysicsHandler(this);
        registerUpdateHandler(mPhysicsHandler);
        mPhysicsHandler.setVelocity(-DEMO_VELOCITY, 0);
        //setPosition(DEMO_POSITION, MINE_WATER_LEVEL);
    }

}
