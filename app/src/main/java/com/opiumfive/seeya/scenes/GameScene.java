package com.opiumfive.seeya.scenes;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.opiumfive.seeya.SceneManager;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.TouchEvent;


public class GameScene extends BaseScene implements IOnSceneTouchListener {

    private PhysicsWorld mPhysicsWorld;
    Sprite mKit;

    @Override
    public void createScene() {
        mEngine.registerUpdateHandler(new FPSLogger());

        setOnSceneTouchListener(this);

        AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0f, 0f, 0f, 5.0f);
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-5.0f, new Sprite(0, SCREEN_HEIGHT - mResourceManager.mParallaxLayerFront.getHeight(), mResourceManager.mParallaxLayerFront, mVertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-5.0f, new Sprite(0, SCREEN_HEIGHT - mResourceManager.mParallaxLayerBackBot.getHeight(), mResourceManager.mParallaxLayerBackBot, mVertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-10.0f, new Sprite(0, 100, mResourceManager.mParallaxLayerBack, mVertexBufferObjectManager)));
        setBackground(autoParallaxBackground);

        final float kitX = 100;
        final float kitY = (SCREEN_HEIGHT - mResourceManager.mKit.getHeight() / 2 - 188 - 10);
        mKit = new Sprite(kitX, kitY, mResourceManager.mKit, mVertexBufferObjectManager);
        //mKit.setRotation(-15);
        attachChild(mKit);

        mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        mPhysicsWorld.setContactListener(createContactListener());

        final FixtureDef kitFixtureDef = PhysicsFactory.createFixtureDef(1, 0, 0);
        final Body kitBody = PhysicsFactory.createCircleBody(mPhysicsWorld, mKit, BodyDef.BodyType.DynamicBody, kitFixtureDef);
        kitBody.setUserData("bird");
        mKit.setUserData(kitBody);

        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mKit, kitBody, true, false));


        /* The actual collision-checking. */
        registerUpdateHandler(new IUpdateHandler() {

            @Override
            public void reset() {}

            @Override
            public void onUpdate(float pSecondsElapsed) {
                mPhysicsWorld.onUpdate(pSecondsElapsed);
                final Body faceBody = (Body)mKit.getUserData();
                float y = faceBody.getPosition().y;
                if (y > 140) jumpFace(mKit);
            }
        });

        //TODO create CameraScene for 'get ready'

        //TODO create CameraScene for 'game over'

        //TODO create HUD for score
    }

    @Override
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        if(mPhysicsWorld != null) {
            if(pSceneTouchEvent.isActionDown()) {
                jumpFace(mKit);
                return true;
            }
        }
        return false;
    }

    private void jumpFace(final Sprite kit) {
        kit.setRotation(-15);
        final Body faceBody = (Body)kit.getUserData();

        final Vector2 velocity = Vector2Pool.obtain(0, -5);
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
