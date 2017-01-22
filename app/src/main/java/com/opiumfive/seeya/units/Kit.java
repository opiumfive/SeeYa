package com.opiumfive.seeya.units;


import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Kit extends AnimatedSprite {

    public Kit(TiledTextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(0, 0, pTextureRegion, pVertexBufferObjectManager);
    }
}
