package com.opiumfive.seeya.pools;


import com.opiumfive.seeya.units.Mine;

import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;


public class MinePool extends GenericPool<Mine> {

    private ITextureRegion mMineTextureRegion;
    private VertexBufferObjectManager mVertexBufferObjectManager;
    private int mMineIndex;

    public MinePool(ITextureRegion pMineTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super();
        this.mMineTextureRegion = pMineTextureRegion;
        this.mVertexBufferObjectManager = pVertexBufferObjectManager;
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
        mMineIndex++;
        return super.obtainPoolItem();
    }

    public int getPipeIndex() {
        return mMineIndex;
    }

}