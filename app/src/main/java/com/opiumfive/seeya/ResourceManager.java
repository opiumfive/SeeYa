package com.opiumfive.seeya;


import android.graphics.Color;
import android.graphics.Typeface;

import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;

public class ResourceManager {
    private static final ResourceManager INSTANCE = new ResourceManager();
    private BitmapTextureAtlas mSplashTextureAtlas;
    public ITextureRegion mSplashTextureRegion;

    private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
    public ITextureRegion mParallaxLayerBack;
    public ITextureRegion mParallaxLayerBackBot;
    public ITextureRegion mParallaxLayerFront;

    private BitmapTextureAtlas mBitmapTextureAtlas;
    public ITextureRegion mKit;
    public ITextureRegion mWaterAlpha;

    public Font mFont1;
    public Font mFont2;
    public Font mFont3;
  //  public TiledTextureRegion mBirdTextureRegion;
   // public TiledTextureRegion mPipeTextureRegion;

   // private BitmapTextureAtlas mSubBitmapTextureAtlas;
   // public TiledTextureRegion mStateTextureRegion;
   // public ITextureRegion mPausedTextureRegion;
    //public ITextureRegion mResumedTextureRegion;
   // public TiledTextureRegion mButtonTextureRegion;
    //public TiledTextureRegion mMedalTextureRegion;

    public GameActivity mActivity;

    private ResourceManager() {}

    public static ResourceManager getInstance() {
        return INSTANCE;
    }

    public void prepare(GameActivity activity) {
        INSTANCE.mActivity = activity;
    }

    public void loadSplashResources() {
        mFont1 = FontFactory.create(mActivity.getFontManager(), mActivity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 16, Color.WHITE);
        mFont1.load();

        FontFactory.setAssetBasePath("fnt/");
        ITexture fontTexture2 = new BitmapTextureAtlas(mActivity.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
        mFont2 = FontFactory.createStrokeFromAsset(mActivity.getFontManager(), fontTexture2, mActivity.getAssets(), "font2.otf", 66, true, Color.BLACK, 2, Color.YELLOW);
        mFont2.load();

        ITexture fontTexture3 = new BitmapTextureAtlas(mActivity.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
        mFont3 = FontFactory.createFromAsset(mActivity.getFontManager(), fontTexture3, mActivity.getAssets(), "font2.otf", 46, true, Color.WHITE);
        mFont3.load();

        //BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/splash/");
        //mSplashTextureAtlas = new BitmapTextureAtlas(mActivity.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
        //mSplashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mSplashTextureAtlas, mActivity, "logo.png", 0, 0);
        //mSplashTextureAtlas.load();
    }

    public void unloadSplashResources() {
        //mFont2.unload();
        //mFont2 = null;
        //mFont3.unload();
        //mFont3 = null;
        //mSplashTextureAtlas.unload();
        //mSplashTextureRegion = null;
    }

    public void loadGameResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(mActivity.getTextureManager(), 1024, 1024);
        mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, mActivity, "backtop.png", 0, 0);
        mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, mActivity, "fronttop.png", 0, 480);
        mParallaxLayerBackBot = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, mActivity, "frontbot.png", 0, 612);
        mAutoParallaxBackgroundTexture.load();

        mBitmapTextureAtlas = new BitmapTextureAtlas(mActivity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
        mKit = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBitmapTextureAtlas, mActivity, "kit.png", 0, 0);
        mWaterAlpha = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBitmapTextureAtlas, mActivity, "wateralpha.png", 0, 73);
        //mBirdTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, mActivity, "Flappy_Birdies.png", 0, 0, 1, 3);
        //mPipeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, mActivity, "Flappy_Pipe.png", 0, 125, 2, 1);
        mBitmapTextureAtlas.load();


    }

    public void unloadGameResources() {
        mBitmapTextureAtlas.unload();
        mKit = null;


    }
}
