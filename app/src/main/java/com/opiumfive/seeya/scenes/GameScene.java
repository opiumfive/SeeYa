package com.opiumfive.seeya.scenes;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.opiumfive.seeya.PhysicsHelper;
import com.opiumfive.seeya.managers.SceneManager;
import com.opiumfive.seeya.pools.IslandPool;
import com.opiumfive.seeya.pools.MinePool;
import com.opiumfive.seeya.units.Island;
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
    private static final float WATER_LEVEL_JUMP_HEIGHT = 0.3f;  //ZONE +- WHERE WHALE CAN DIVE OR JUMP
    private static final float TAP_JUMP_HEIGHT = 7.0f; // STRENGTH OF JUMP
    private static final float FLYING_ROTATION_ANGLE = 15.0f;
    private static final float KIT_X_OFFSET = 150.0f;
    private static final float UPDOWN_SEC_DIFFERENCE = 0.3f; // sec to distinguish between jump and dive
    private static final float UPDOWN_MAX_SEC_DIFFERENCE = 1.0f; // TODO configure to get max factor 0.5
    private static final float GAME_START_SPEED = 5f;
    private static final long ANIMATION_FRAME_DURATION = 33L;
    private static final String SHAPES_FILE = "shapes/island1.xml";

    private final float KIT_WATER_LEVEL = SCREEN_HEIGHT - mResourceManager.mKitSwimAnim.getHeight() / 2 - 188 - 10;

    private boolean mUsualJump = true;
    private boolean mDiveMade = false;
    private boolean mUpMade = false;

    private float mGameSpeed;
    private float mLastSecs;

    private PhysicsWorld mPhysicsWorld;
    private AnimatedSprite mKit;
    private Sprite mWaterAlpha;
    private Mine mMine;
    private MinePool mMinePool;
    private IslandPool mIslandPool;
    private Island mIsland;
    private float mSecsTotal;
    private float mDiveFactor = 1.0f;

    private PhysicsHelper mPhysicsHelper;

    private float mCameraZoomFactor;

    @Override
    public void createScene() {
        mEngine.registerUpdateHandler(new FPSLogger());
        mGameSpeed = GAME_START_SPEED;
        mCameraZoomFactor = 1f;
        mCamera.setZoomFactor(mCameraZoomFactor);

        setOnSceneTouchListener(this);

        /*
         * INIT BACKGROUND
         */

        final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0f, 0f, 0f, mGameSpeed);
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity( - 7f, new Sprite(0, SCREEN_HEIGHT - mResourceManager.mParallaxLayerFront.getHeight(), mResourceManager.mParallaxLayerFront, mVertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(0f, new Sprite(0, SCREEN_HEIGHT - mResourceManager.mParallaxLayerBackBot.getHeight(), mResourceManager.mParallaxLayerBackBot, mVertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity( - 14f, new Sprite(0, 50, mResourceManager.mParallaxLayerBack, mVertexBufferObjectManager)));
        setBackground(autoParallaxBackground);

        /*
         *  INIT SPRITES
         */

        mKit = new AnimatedSprite(KIT_X_OFFSET, KIT_WATER_LEVEL, mResourceManager.mKitSwimAnim, mVertexBufferObjectManager);
        mKit.animate(ANIMATION_FRAME_DURATION);
        attachChild(mKit);
        mKit.setPosition(KIT_X_OFFSET, SCREEN_HEIGHT - mResourceManager.mKitSwimAnim.getHeight() / 2 - 188 - 10);

        mWaterAlpha = new Sprite(0, SCREEN_HEIGHT - mResourceManager.mWaterAlpha.getHeight(), mResourceManager.mWaterAlpha, mVertexBufferObjectManager);
        attachChild(mWaterAlpha);
        mWaterAlpha.setZIndex(1);
        mWaterAlpha.setScaleCenterY( - mWaterAlpha.getHeight() / 4f);

        /*
         * INIT POOLS
         */

        mMinePool = new MinePool(mResourceManager.mMineSwimAnim, mVertexBufferObjectManager);
        mMinePool.batchAllocatePoolItems(10);
        mMine = mMinePool.obtainPoolItem();
        mMine.animate(ANIMATION_FRAME_DURATION);
        mMine.setVelocity(mGameSpeed);
        attachChild(mMine);
        mMine.setZIndex(0);

        mIslandPool = new IslandPool(mResourceManager.mIsland1, mVertexBufferObjectManager);
        mIslandPool.batchAllocatePoolItems(10);
        mIsland = mIslandPool.obtainPoolItem();
        attachChild(mIsland);
        mIsland.setZIndex(0);
        sortChildren();

        /*
         * INIT PHYSICS
         */

        mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
        mPhysicsWorld.setContactListener(createContactListener());
        mPhysicsHelper = new PhysicsHelper();
        registerUpdateHandler(mPhysicsWorld);

        final FixtureDef kitFixtureDef = PhysicsFactory.createFixtureDef(0f, 0.1f, 0f);
        final Body kitBody = PhysicsFactory.createCircleBody(mPhysicsWorld, 90f, 50f, 35f, BodyDef.BodyType.DynamicBody, kitFixtureDef);
        kitBody.setUserData("kit");
        mKit.setUserData(kitBody);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mKit, kitBody, true, false));

        mPhysicsHelper.open(mActivity, SHAPES_FILE);
        Body body = mPhysicsHelper.createBody("island_1", mIsland, mPhysicsWorld);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mIsland, body, true, false));
        body.setLinearVelocity( - mGameSpeed, 0f);


        mLastSecs = mEngine.getSecondsElapsedTotal();
        registerUpdateHandler(new IUpdateHandler() {

            @Override
            public void reset() {}

            @Override
            public void onUpdate(float pSecondsElapsed) {

                /* CHANGE OF GAME SPEED */
                if (mEngine.getSecondsElapsedTotal() - mLastSecs > 1f) {
                    mGameSpeed += 0.1f;
                    autoParallaxBackground.setParallaxChangePerSecond(mGameSpeed);
                    mLastSecs = mEngine.getSecondsElapsedTotal();
                }

                mPhysicsWorld.onUpdate(pSecondsElapsed);
                final Body faceBody = (Body) mKit.getUserData();
                float y = faceBody.getPosition().y;

                /* HANDLE CAMERA ZOOM DEPENDING ON Y POSITION*/
                float yy = mKit.getY();
                if (yy < 0f || yy + mKit.getHeight() > SCREEN_HEIGHT) {
                    float halfScreenHeight = SCREEN_HEIGHT / 2f;
                    if ( yy + mKit.getHeight() > SCREEN_HEIGHT) {
                        mCameraZoomFactor = halfScreenHeight / (yy + mKit.getHeight() - halfScreenHeight);
                    } else {
                        mCameraZoomFactor = halfScreenHeight / (halfScreenHeight - yy);
                    }
                    mCamera.setZoomFactor(mCameraZoomFactor);
                    mWaterAlpha.setScale(1f + (1f - mCameraZoomFactor) * 3);
                } else {
                    mCameraZoomFactor = 1f;
                    mCamera.setZoomFactor(mCameraZoomFactor);
                    mWaterAlpha.setScale(1f + (1f - mCameraZoomFactor) * 3);
                }

                /* HANDLING JUMPING AND DIVING */
                if (Math.abs(y - WATER_LEVEL) >= WATER_LEVEL_JUMP_HEIGHT) {
                    if (y > WATER_LEVEL) {
                        setGravity(((mDiveMade && mUsualJump) || (mUpMade && !mUsualJump)) ? -7 * 4 : -7);
                        mKit.animate(ANIMATION_FRAME_DURATION);
                    } else {
                        mKit.stopAnimation(7);
                        mUpMade = true;
                        mDiveMade = false;
                        setGravity(7);
                    }
                    if (y - WATER_LEVEL >= WATER_LEVEL_JUMP_HEIGHT * 2) {
                        mDiveMade = true;
                    }

                } else {
                    if (y > WATER_LEVEL - WATER_LEVEL_JUMP_HEIGHT && ((mDiveMade && mUsualJump) || (mDiveMade && mUpMade && !mUsualJump))) {
                        jumpSprite(mKit, 0);
                        if (mDiveMade) mDiveMade = false;
                        if (mUpMade) mUpMade = false;
                    }
                    setGravity(0);
                }
                mKit.setRotation(faceBody.getLinearVelocity().y * FLYING_ROTATION_ANGLE / 7.0f);


                /* POOLS */
                if (mMine.getX() < - SCREEN_WIDTH * 0.2f) {
                    mMine.stopAnimation();
                    detachChild(mMine);
                    mMinePool.recyclePoolItem(mMine);
                    mMinePool.shufflePoolItems();

                    mMine = mMinePool.obtainPoolItem();
                    mMine.animate(33);
                    mMine.setVelocity(mGameSpeed);
                    attachChild(mMine);
                    mMine.setZIndex(0);
                    sortChildren();
                }
                if (mIsland.getX() < - SCREEN_WIDTH * 2f) {
                   // final PhysicsConnector islandPhysicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mIsland);
                  //  mPhysicsWorld.unregisterPhysicsConnector(islandPhysicsConnector);
                  //  mPhysicsWorld.destroyBody(islandPhysicsConnector.getBody());
                    detachChild(mIsland);
                    mIslandPool.recyclePoolItem(mIsland);
                    mIslandPool.shufflePoolItems();

                    mIsland = mIslandPool.obtainPoolItem();
                    attachChild(mIsland);
                    mIsland.setZIndex(0);
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
        if (mPhysicsWorld != null) {

            if (pSceneTouchEvent.isActionDown()) {
                mSecsTotal = mEngine.getSecondsElapsedTotal();
                return true;
            }

            if(pSceneTouchEvent.isActionUp()) {
                float secDifference = mEngine.getSecondsElapsedTotal() - mSecsTotal;
                if (secDifference > UPDOWN_SEC_DIFFERENCE) {
                    mUsualJump = false;
                    final Body faceBody = (Body) mKit.getUserData();
                    float y = faceBody.getPosition().y;
                    if (secDifference > UPDOWN_MAX_SEC_DIFFERENCE) {
                        secDifference = UPDOWN_MAX_SEC_DIFFERENCE;
                    }
                    mDiveFactor = secDifference * 2;
                    if (Math.abs(y - WATER_LEVEL) <= WATER_LEVEL_JUMP_HEIGHT) {
                        jumpSprite(mKit, TAP_JUMP_HEIGHT * mDiveFactor);
                    }

                    mDiveFactor = secDifference * 2;
                } else {
                    mUsualJump = true;
                    final Body faceBody = (Body) mKit.getUserData();
                    float y = faceBody.getPosition().y;
                    if (Math.abs(y - WATER_LEVEL) <= WATER_LEVEL_JUMP_HEIGHT) {
                        jumpSprite(mKit, -TAP_JUMP_HEIGHT);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void jumpSprite(final Sprite kit, float pY) {
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
        mCameraZoomFactor = 1.0f;
        mCamera.setZoomFactor(mCameraZoomFactor);
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
