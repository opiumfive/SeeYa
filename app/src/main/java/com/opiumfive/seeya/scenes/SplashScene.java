package com.opiumfive.seeya.scenes;


import com.opiumfive.seeya.SceneManager;

import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

public class SplashScene extends BaseScene {
    @Override
    public void createScene() {

        setBackground(new Background(Color.WHITE));
        Text nameText = new Text(0, 0, mResourceManager.mFont2, "Sea Ya", new TextOptions(HorizontalAlign.LEFT), mVertexBufferObjectManager);
        nameText.setPosition((SCREEN_WIDTH - nameText.getWidth())/2f, 65);
        attachChild(nameText);

        /*Sprite splash = new Sprite(0, 0, mResourceManager.mSplashTextureRegion, mVertexBufferObjectManager) {
            @Override
            protected void preDraw(GLState pGLState, Camera pCamera)
            {
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
        };
        attachChild(splash);

        Text copyrightText = new Text(0, 0, mResourceManager.mFont1, "(c) 2011-2014", new TextOptions(HorizontalAlign.LEFT), mVertexBufferObjectManager);
        copyrightText.setPosition(SCREEN_WIDTH - copyrightText.getWidth()-5, SCREEN_HEIGHT - copyrightText.getHeight()-5);
        attachChild(copyrightText); */
    }

    @Override
    public void onBackKeyPressed() {
        mActivity.finish();
    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_SPLASH;
    }

    @Override
    public void disposeScene() {
    }
}
