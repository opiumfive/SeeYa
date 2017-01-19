package com.opiumfive.seeya.pools;


import com.opiumfive.seeya.units.Mine;

import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;
import org.andengine.util.math.MathUtils;

import java.util.Random;


public class MinePool extends GenericPool<Mine> {

    private TiledTextureRegion mMineTextureRegion;
    private VertexBufferObjectManager mVertexBufferObjectManager;

    public MinePool(TiledTextureRegion pMineTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super();
        mMineTextureRegion = pMineTextureRegion;
        mVertexBufferObjectManager = pVertexBufferObjectManager;
    }

    @Override
    protected Mine onAllocatePoolItem() {
        return new Mine(mMineTextureRegion, mVertexBufferObjectManager);
    }

    @Override
    protected void onHandleRecycleItem(Mine pItem) {
//        pItem.setIgnoreUpdate(true);
//        pItem.setVisible(false);
    }

    @Override
    protected void onHandleObtainItem(Mine pItem) {
        pItem.reset();
    }

    @Override
    public synchronized Mine obtainPoolItem() {
        return super.obtainPoolItem();
    }
}