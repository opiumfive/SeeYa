package com.opiumfive.seeya.scenes;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.opiumfive.seeya.PhysicsHelper;
import com.opiumfive.seeya.managers.SceneManager;
import com.opiumfive.seeya.pools.IslandPool;
import com.opiumfive.seeya.pools.MinePool;
import com.opiumfive.seeya.units.Island;
import com.opiumfive.seeya.units.Kit;
import com.opiumfive.seeya.units.Mine;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.HorizontalAlign;


public class GameScene extends BaseScene implements IOnSceneTouchListener {

    private static final float WATER_LEVEL = 9.0f;
    private static final float WATER_LEVEL_JUMP_HEIGHT = 0.3f;  //ZONE +- WHERE WHALE CAN DIVE OR JUMP
    private static final float TAP_JUMP_HEIGHT = 7.0f; // STRENGTH OF JUMP
    private static final float FLYING_ROTATION_ANGLE = 2.0f;
    private static final float KIT_X_OFFSET = 150.0f;
    private static final float UPDOWN_SEC_DIFFERENCE = 0.3f; // sec to distinguish between jump and dive
    private static final float UPDOWN_MAX_SEC_DIFFERENCE = 1.0f; // TODO configure to get max factor 0.5
    private static final float GAME_START_SPEED = 5f;
    private static final long ANIMATION_FRAME_DURATION = 33L;
    private static final String SHAPES_FILE = "shapes/island1.xml";
    private static final int ROTATE_NO = 0;
    private static final int ROTATE_LEFT = 1;
    private static final int ROTATE_RIGHT = 2;
    private static final float GRAVITY = 7f;

    private static final String KIT_LABEL = "kit";
    private static final String ISLAND_LABEL = "island1";
    private static final String MINE_LABEL = "mine";

    private boolean mUsualJump = true;
    private boolean mDiveMade = false;
    private boolean mUpMade = false;

    private float mGameSpeed;
    private float mLastSecs;

    private PhysicsWorld mPhysicsWorld;
    private Kit mKit;
    private Sprite mWaterAlpha;
    private Mine mMine;
    private MinePool mMinePool;
    private IslandPool mIslandPool;
    private Island mIsland;
    private float mSecsTotal;
    private float mDiveFactor = 1.0f;

    private PhysicsHelper mPhysicsHelper;

    private float mCameraZoomFactor;
    private int mRotate = 0; // 0 - no, 1 - left, 2 - right
    private float mKitAngle = 0f;

    private Text mHudText;
    private int mScore;
    private CameraScene mGameOverScene;

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
         * INIT HUD
         */

        HUD gameHUD = new HUD();
        mHudText = new Text(SCREEN_WIDTH / 2f, 15f, mResourceManager.mFont2, "0123456789", new TextOptions(HorizontalAlign.LEFT), mVertexBufferObjectManager);
        mHudText.setText(String.valueOf(mScore));
        mHudText.setX((SCREEN_WIDTH - mHudText.getWidth()) / 2);
        mHudText.setVisible(true);
        gameHUD.attachChild(mHudText);
        mCamera.setHUD(gameHUD);

        /*
         * INIT GAMEOVER CAMERA SCENE
         */

        mGameOverScene = new CameraScene(mCamera);
        Text gameOverText = new Text(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, mResourceManager.mFont2, "GAME OVER", new TextOptions(HorizontalAlign.LEFT), mVertexBufferObjectManager);
        mGameOverScene.attachChild(gameOverText);
        mGameOverScene.setBackgroundEnabled(false);
        mGameOverScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

            @Override
            public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
                if (pSceneTouchEvent.isActionUp()) {
                    clearChildScene();
                    mHudText.setVisible(true);
                }
                return true;
            }
        });
        /*
         *  INIT SPRITES
         */

        mKit = new Kit(mResourceManager.mKitSwimAnim, mVertexBufferObjectManager);
        mKit.animate(ANIMATION_FRAME_DURATION);
        attachChild(mKit);
        mKit.setPosition(KIT_X_OFFSET, SCREEN_HEIGHT - mResourceManager.mKitSwimAnim.getHeight() / 2 - 188 - 10);

        mWaterAlpha = new Sprite(0, SCREEN_HEIGHT - mResourceManager.mWaterAlpha.getHeight(), mResourceManager.mWaterAlpha, mVertexBufferObjectManager);
        mWaterAlpha.setZIndex(1);
        attachChild(mWaterAlpha);
        mWaterAlpha.setScaleCenterY( - mWaterAlpha.getHeight() / 4f);

        /*
         * INIT POOLS
         */

        mMinePool = new MinePool(mResourceManager.mMineSwimAnim, mVertexBufferObjectManager);
        mMinePool.batchAllocatePoolItems(10);
        mMine = mMinePool.obtainPoolItem();
        mMine.animate(ANIMATION_FRAME_DURATION);
        mMine.setZIndex(0);
        attachChild(mMine);

        mIslandPool = new IslandPool(mResourceManager.mIsland1, mVertexBufferObjectManager);
        mIslandPool.batchAllocatePoolItems(10);
        mIsland = mIslandPool.obtainPoolItem();
        mIsland.setZIndex(0);
        attachChild(mIsland);

        sortChildren(); //TODO find a way to remove it

        /*
         * INIT PHYSICS
         */

        mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
        mPhysicsWorld.setContactListener(createContactListener());
        mPhysicsHelper = new PhysicsHelper();
        registerUpdateHandler(mPhysicsWorld);

        final FixtureDef kitFixtureDef = PhysicsFactory.createFixtureDef(0f, 0.1f, 0f);
        final Body kitBody = PhysicsFactory.createCircleBody(mPhysicsWorld, 90f, 50f, 35f, BodyDef.BodyType.DynamicBody, kitFixtureDef);
        kitBody.setUserData(KIT_LABEL);
        mKit.setUserData(kitBody);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mKit, kitBody, true, false));

        final FixtureDef mineFixtureDef = PhysicsFactory.createFixtureDef(1f, 0f, 0f);
        final Body mineBody = PhysicsFactory.createCircleBody(mPhysicsWorld, mMine, BodyDef.BodyType.KinematicBody, mineFixtureDef);
        mineBody.setUserData(MINE_LABEL);
        mMine.setUserData(mineBody);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mMine, mineBody, true, false));
        mineBody.setLinearVelocity( - mGameSpeed, 0f);

        mPhysicsHelper.open(mActivity, SHAPES_FILE);
        final Body body = mPhysicsHelper.createBody(ISLAND_LABEL, mIsland, mPhysicsWorld);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mIsland, body, true, false));
        body.setLinearVelocity( - mGameSpeed, 0f);

        /*
         * MAIN GAME LOOP
         */

        mLastSecs = mEngine.getSecondsElapsedTotal();
        registerUpdateHandler(new IUpdateHandler() {

            @Override
            public void reset() {}

            @Override
            public void onUpdate(float pSecondsElapsed) {

                mPhysicsWorld.onUpdate(pSecondsElapsed);
                final Body faceBody = (Body) mKit.getUserData();
                float y = faceBody.getPosition().y;

                /* CHANGE OF GAME SPEED */
                if (mEngine.getSecondsElapsedTotal() - mLastSecs > 1f) {
                    mHudText.setText(String.valueOf(++mScore));
                    mGameSpeed += 0.1f;
                    autoParallaxBackground.setParallaxChangePerSecond(mGameSpeed);
                    mLastSecs = mEngine.getSecondsElapsedTotal();
                }

                /* EQUILIBLIUM of KIT_X */
                if (Math.abs(y - WATER_LEVEL) <= WATER_LEVEL_JUMP_HEIGHT) {
                    float velY = faceBody.getLinearVelocity().y;
                    if (mKit.getX() <= -5f) {
                        final Vector2 velocity = Vector2Pool.obtain(2, velY);
                        faceBody.setLinearVelocity(velocity);
                    } else if (mKit.getX() > 5f) {
                        final Vector2 velocity = Vector2Pool.obtain(-2, velY);
                        faceBody.setLinearVelocity(velocity);
                    } else {
                        final Vector2 velocity = Vector2Pool.obtain(0, velY);
                        faceBody.setLinearVelocity(velocity);
                    }
                }

                /* HANDLE CAMERA ZOOM DEPENDING ON Y POSITION */
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
                        setGravity(((mDiveMade && mUsualJump) || (mUpMade && !mUsualJump)) ? - GRAVITY * 4 : - GRAVITY);
                        mKit.animate(ANIMATION_FRAME_DURATION);
                    } else {
                        mKit.stopAnimation(7);
                        mUpMade = true;
                        mDiveMade = false;
                        setGravity(GRAVITY);
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

                switch (mRotate) {
                    case ROTATE_NO:
                        mKit.setRotation(faceBody.getLinearVelocity().y * FLYING_ROTATION_ANGLE);
                        mKitAngle = 0f;
                        break;
                    case ROTATE_LEFT:
                        mKitAngle -= 5f;
                        mKit.setRotation(mKitAngle);
                        break;
                    case ROTATE_RIGHT:
                        mKitAngle += 5f;
                        mKit.setRotation(mKitAngle);
                        break;
                }

                /* POOLS */
                if (mMine.getX() < - SCREEN_WIDTH * 2f) {
                    freeBody(mMine);
                    mMine.stopAnimation();
                    detachChild(mMine);
                    mMinePool.recyclePoolItem(mMine);
                    mMinePool.shufflePoolItems();

                    mMine = mMinePool.obtainPoolItem();
                    mMine.animate(ANIMATION_FRAME_DURATION);
                    mMine.setZIndex(0);
                    attachChild(mMine);

                    final FixtureDef mineFixtureDef = PhysicsFactory.createFixtureDef(1f, 0f, 0f);
                    final Body mineBody = PhysicsFactory.createCircleBody(mPhysicsWorld, mMine, BodyDef.BodyType.KinematicBody, mineFixtureDef);
                    mineBody.setUserData(MINE_LABEL);
                    mMine.setUserData(mineBody);
                    mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mMine, mineBody, true, false));
                    mineBody.setLinearVelocity( - mGameSpeed, 0f);
                    sortChildren(); //TODO find a way to remove it
                }   // ===============================> NEED HARD REFACTOR
                if (mIsland.getX() < - SCREEN_WIDTH * 2f) {
                    freeBody(mIsland);
                    detachChild(mIsland);
                    mIslandPool.recyclePoolItem(mIsland);
                    mIslandPool.shufflePoolItems();

                    mIsland = mIslandPool.obtainPoolItem();
                    mIsland.setZIndex(0);
                    attachChild(mIsland);
                    Body body = mPhysicsHelper.createBody("island_1", mIsland, mPhysicsWorld);
                    mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mIsland, body, true, false));
                    body.setLinearVelocity( - mGameSpeed, 0f);
                    sortChildren(); //TODO find a way to remove it
                }
            }
        });
    }

    private void freeBody(IShape shape) {
        PhysicsConnector physicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(shape);
        mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
        mPhysicsWorld.destroyBody(physicsConnector.getBody());
    }

    public void setGravity(float gy) {
        final Vector2 gravity = Vector2Pool.obtain(0, gy);
        mPhysicsWorld.setGravity(gravity);
        Vector2Pool.recycle(gravity);
    }

    @Override
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        if (mPhysicsWorld != null) {

            final Body faceBody = (Body) mKit.getUserData();
            float y = faceBody.getPosition().y;

            if (pSceneTouchEvent.isActionDown()) {
                mSecsTotal = mEngine.getSecondsElapsedTotal();
                if (Math.abs(y - WATER_LEVEL) > WATER_LEVEL_JUMP_HEIGHT * 1.3 && y < WATER_LEVEL) {
                    if (pSceneTouchEvent.getX() < SCREEN_WIDTH / 2f) {
                        mRotate = ROTATE_LEFT;
                    } else {
                        mRotate = ROTATE_RIGHT;
                    }
                } else {
                    mRotate = ROTATE_NO;
                }
                return true;
            }

            if(pSceneTouchEvent.isActionUp()) {
                mRotate = ROTATE_NO;
                float secDifference = mEngine.getSecondsElapsedTotal() - mSecsTotal;
                if (secDifference > UPDOWN_SEC_DIFFERENCE) {
                    mUsualJump = false;
                    if (secDifference > UPDOWN_MAX_SEC_DIFFERENCE) {
                        secDifference = UPDOWN_MAX_SEC_DIFFERENCE;
                    }
                    mDiveFactor = secDifference * 2;
                    if (Math.abs(y - WATER_LEVEL) <= WATER_LEVEL_JUMP_HEIGHT) {
                        jumpSprite(mKit, TAP_JUMP_HEIGHT * mDiveFactor); //DIVE
                    }
                } else {
                    mUsualJump = true;
                    if (Math.abs(y - WATER_LEVEL) <= WATER_LEVEL_JUMP_HEIGHT) {
                        jumpSprite(mKit, - TAP_JUMP_HEIGHT); //JUMP
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void jumpSprite(final Sprite kit, float pY) {
        final Body faceBody = (Body) kit.getUserData();
        final Vector2 velocity = Vector2Pool.obtain(faceBody.getLinearVelocity().x, pY);
        faceBody.setLinearVelocity(velocity);
        Vector2Pool.recycle(velocity);
    }

    private ContactListener createContactListener() {
        ContactListener contactListener = new ContactListener() {
            @Override
            public void beginContact(Contact pContact) {
                final Fixture fixtureA = pContact.getFixtureA();
                final Body bodyA = fixtureA.getBody();
                final String userDataA = (String) bodyA.getUserData();

                final Fixture fixtureB = pContact.getFixtureB();
                final Body bodyB = fixtureB.getBody();
                final String userDataB = (String) bodyB.getUserData();

                if ((MINE_LABEL.equals(userDataA) && KIT_LABEL.equals(userDataB)) || (KIT_LABEL.equals(userDataA) && MINE_LABEL.equals(userDataB))) {
                    setChildScene(mGameOverScene, false, true, true);
                }
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }

        };
        return contactListener;
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
