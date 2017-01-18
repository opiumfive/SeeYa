package com.opiumfive.seeya.pools;


import com.opiumfive.seeya.units.Island;

import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;

import java.util.Random;

public class IslandPool extends GenericPool<Island> {

    private ITextureRegion mIslandTextureRegion;
    private VertexBufferObjectManager mVertexBufferObjectManager;
    private int mIslandIndex;

    public IslandPool(ITextureRegion pIslandTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super();
        mIslandTextureRegion = pIslandTextureRegion;
        //mRedMineTextureRegion = pRedMineTextureRegion;
        mVertexBufferObjectManager = pVertexBufferObjectManager;
    }

    @Override
    protected Island onAllocatePoolItem() {
        Random random = new Random();
        //boolean isRed = random.nextBoolean();
        return new Island(mIslandTextureRegion, mVertexBufferObjectManager);
    }

    @Override
    protected void onHandleRecycleItem(Island pItem) {
//        pItem.setIgnoreUpdate(true);
//        pItem.setVisible(false);
    }

    @Override
    protected void onHandleObtainItem(Island pItem) {
        pItem.reset();
    }

    @Override
    public synchronized Island obtainPoolItem() {
        mIslandIndex++;
        return super.obtainPoolItem();
    }

    public int getMineIndex() {
        return mIslandIndex;
    }

}