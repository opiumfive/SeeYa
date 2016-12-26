package com.opiumfive.seeya.scenes;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.opiumfive.seeya.managers.SceneManager;
import com.opiumfive.seeya.pools.MinePool;
import com.opiumfive.seeya.units.Mine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.TouchEvent;


public class GameScene extends BaseScene implements IOnSceneTouchListener {

    private static final float WATER_LEVEL = 9.0f;
    private static final float WATER_LEVEL_JUMP_HEIGHT = 0.3f;
    private static final float TAP_JUMP_HEIGHT = 7.0f;
    private static final float FLYING_ROTATION_ANGLE = 15.0f;
    private static final float KIT_X_OFFSET = 100.0f;
    private final float KIT_WATER_LEVEL = SCREEN_HEIGHT - mResourceManager.mKit.getHeight() / 2 - 188 - 10;

    private boolean mUsualJump = true;
    private boolean mDiveMade = false;


    private PhysicsWorld mPhysicsWorld;
    //private Sprite mKit;
    private AnimatedSprite mKit;
    private Sprite mWaterAlpha;
    private Mine mMine;
    private MinePool mMinePool;

    @Override
    public void createScene() {
        mEngine.registerUpdateHandler(new FPSLogger());

        setOnSceneTouchListener(this);

        AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0f, 0f, 0f, 5.0f);
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-5.0f, new Sprite(0, SCREEN_HEIGHT - mResourceManager.mParallaxLayerFront.getHeight(), mResourceManager.mParallaxLayerFront, mVertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-5.0f, new Sprite(0, SCREEN_HEIGHT - mResourceManager.mParallaxLayerBackBot.getHeight(), mResourceManager.mParallaxLayerBackBot, mVertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-10.0f, new Sprite(0, 100, mResourceManager.mParallaxLayerBack, mVertexBufferObjectManager)));
        setBackground(autoParallaxBackground);

        final float kitX = KIT_X_OFFSET;
        final float kitY = KIT_WATER_LEVEL;
        //mKit = new Sprite(kitX, kitY, mResourceManager.mKit, mVertexBufferObjectManager);
        mKit = new AnimatedSprite(kitX, kitY, mResourceManager.mKitSwimAnim, mVertexBufferObjectManager);
        mKit.animate(33);
        attachChild(mKit);
        mKit.setPosition(kitX, SCREEN_HEIGHT - mResourceManager.mKit.getHeight() / 2 - 188 - 10);

        mWaterAlpha = new Sprite(0, SCREEN_HEIGHT - mResourceManager.mWaterAlpha.getHeight(), mResourceManager.mWaterAlpha, mVertexBufferObjectManager);
        attachChild(mWaterAlpha);
        mWaterAlpha.setZIndex(1);

        mPhysicsWorld = new PhysicsWorld(new Vector2(0, 7), false);
        mPhysicsWorld.setContactListener(createContactListener());

        final FixtureDef kitFixtureDef = PhysicsFactory.createFixtureDef(1, 0, 0);
        final Body kitBody = PhysicsFactory.createCircleBody(mPhysicsWorld, mKit, BodyDef.BodyType.DynamicBody, kitFixtureDef);
        kitBody.setUserData("kit");
        mKit.setUserData(kitBody);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mKit, kitBody, true, false));

        mMinePool = new MinePool(mResourceManager.mMineBlue, mResourceManager.mMineRed, mVertexBufferObjectManager);
        mMinePool.batchAllocatePoolItems(10);
        mMine = mMinePool.obtainPoolItem();
        attachChild(mMine);
        mMine.setZIndex(0);
        sortChildren();

        registerUpdateHandler(new IUpdateHandler() {

            @Override
            public void reset() {}

            @Override
            public void onUpdate(float pSecondsElapsed) {
                mPhysicsWorld.onUpdate(pSecondsElapsed);
                final Body faceBody = (Body) mKit.getUserData();
                float y = faceBody.getPosition().y;

                if (Math.abs(y - WATER_LEVEL) >= WATER_LEVEL_JUMP_HEIGHT) {
                    if (y > WATER_LEVEL) {
                        setGravity(mUsualJump ? -7 * 4 : -7);
                    } else {
                        setGravity(7);
                    }
                    if (y - WATER_LEVEL >= WATER_LEVEL_JUMP_HEIGHT * 2) {
                        mDiveMade = true;
                    }

                } else {
                    if (y > WATER_LEVEL - WATER_LEVEL_JUMP_HEIGHT && mDiveMade) {
                        jumpFace(mKit, 0);
                        mDiveMade = false;
                    }
                    setGravity(0);
                }
                mKit.setRotation(faceBody.getLinearVelocity().y * FLYING_ROTATION_ANGLE / 7.0f);

                if (mMine.getX() < -SCREEN_WIDTH * 0.2f) {
                    detachChild(mMine);
                    mMinePool.recyclePoolItem(mMine);
                    mMinePool.shufflePoolItems();

                    mMine = mMinePool.obtainPoolItem();
                    attachChild(mMine);
                    mMine.setZIndex(0);
                    sortChildren();
                }
            }
        });

    }

    public void setGravity(float gy) {
        final Vector2 gravity = Vector2Pool.obtain(0, gy);
        mPhysicsWorld.setGravity(gravity);
        Vector2Pool.recycle(gravity);
    }

    @Override
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        if(mPhysicsWorld != null) {
            if(pSceneTouchEvent.isActionDown()) {
                mUsualJump = true;
                final Body faceBody = (Body)mKit.getUserData();
                float y = faceBody.getPosition().y;
                if (Math.abs(y - WATER_LEVEL) <= WATER_LEVEL_JUMP_HEIGHT) {
                    jumpFace(mKit, -TAP_JUMP_HEIGHT);
                }
                return true;
            }
            if(pSceneTouchEvent.isActionMove()) {
                mUsualJump = false;
                final Body faceBody = (Body)mKit.getUserData();
                float y = faceBody.getPosition().y;
                if (Math.abs(y - WATER_LEVEL) <= WATER_LEVEL_JUMP_HEIGHT) {
                    jumpFace(mKit,  TAP_JUMP_HEIGHT);
                }
                return true;
            }

        }
        return false;
    }

    private void jumpFace(final Sprite kit, float pY) {
        final Body faceBody = (Body)kit.getUserData();
        final Vector2 velocity = Vector2Pool.obtain(0, pY);
        faceBody.setLinearVelocity(velocity);
        Vector2Pool.recycle(velocity);
    }

    private ContactListener createContactListener() {
        //TODO implement
        return null;
    }

    @Override
    public void onBackKeyPressed() {
        mSceneManager.setScene(SceneManager.SceneType.SCENE_MENU);
    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene() {
        //TODO
    }
}
