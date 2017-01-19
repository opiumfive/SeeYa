package com.opiumfive.seeya.pools;


import com.opiumfive.seeya.units.Island;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;


public class IslandPool extends GenericPool<Island> {

    private ITextureRegion mIslandTextureRegion;
    private VertexBufferObjectManager mVertexBufferObjectManager;

    public IslandPool(ITextureRegion pIslandTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super();
        mIslandTextureRegion = pIslandTextureRegion;
        mVertexBufferObjectManager = pVertexBufferObjectManager;
    }

    @Override
    protected Island onAllocatePoolItem() {
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
        return super.obtainPoolItem();
    }
}