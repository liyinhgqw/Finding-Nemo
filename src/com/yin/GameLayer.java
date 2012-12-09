package com.yin;

import java.util.ArrayList;
import java.util.Random;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.sound.SoundEngine;
import android.content.Context;
import android.view.MotionEvent;

public class GameLayer extends CCColorLayer
{
	public static final int FAKE_NUM = 10;
	public static final float VELOCITY = 100;
	protected CCSprite player_, target_;
	protected ArrayList<CCSprite> fakes_;
	private Random rand_;
	CGSize winSize_;
	
	public static CCScene scene()
	{
		CCScene scene = CCScene.node();
		CCColorLayer layer = new GameLayer(ccColor4B.ccc4(255, 255, 255, 255));
		
		scene.addChild(layer);
		
		return scene;
	}
	
	protected GameLayer(ccColor4B color)
	{
		super(color);
		
		this.setIsTouchEnabled(true);
		
		winSize_ = CCDirector.sharedDirector().displaySize();	
		rand_ = new Random(System.currentTimeMillis());
		
		player_ = CCSprite.sprite("shanmao.png");
		player_.setPosition(CGPoint.ccp(winSize_.width / 2.0f, winSize_.height / 2.0f));
		addChild(player_);
		
		target_ = CCSprite.sprite("humao.png");
		do {
			target_.setPosition(genPos(winSize_.width, target_.getContentSize().width), 
					genPos(winSize_.height, target_.getContentSize().height));
		} while (intersectFlex(player_, target_));
		addChild(target_);
		fakes_ = new ArrayList<CCSprite>();
		for (int i = 0; i < FAKE_NUM; i++) {
			CCSprite fake = CCSprite.sprite("fake.png");
			do {
				fake.setPosition(genPos(winSize_.width, fake.getContentSize().width), 
					genPos(winSize_.height, fake.getContentSize().height));
			} while (intersectFlex(player_, fake));
			addChild(fake);
			fakes_.add(fake);
		}

		
		// Handle sound
		Context context = CCDirector.sharedDirector().getActivity();
		SoundEngine.sharedEngine().preloadEffect(context, R.raw.pew_pew_lei);
		SoundEngine.sharedEngine().playSound(context, R.raw.background_music_aac, true);
	
		// Schedule event
		this.schedule("move");
//		this.schedule("gameLogic", 1.0f);
		this.schedule("update");
	}
	
	@Override
	public boolean ccTouchesEnded(MotionEvent event)
	{
		// Choose one of the touches to work with
		CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
		
		float x = player_.getPosition().x;
		float y = player_.getPosition().y;
		
		CGPoint realDest = CGPoint.ccp(location.x, location.y);
		
		// Determine the length of how far we're shooting
		int offRealX = (int)(location.x - x);
		int offRealY = (int)(location.y - y);
		float length = (float)Math.sqrt((offRealX * offRealX) + (offRealY * offRealY));
		float velocity = VELOCITY / 1.0f; // 480 pixels / 1 sec
		float realMoveDuration = length / velocity;
		
		// Move projectile to actual endpoint
		player_.runAction(CCSequence.actions(
				CCMoveTo.action(realMoveDuration, realDest),
				CCCallFuncN.action(this, "spriteMoveFinished")));
		
		// Pew!
//		Context context = CCDirector.sharedDirector().getActivity();
//		SoundEngine.sharedEngine().playEffect(context, R.raw.pew_pew_lei);
		
		return true;
	}
	
//	public void gameLogic(float dt)
//	{
//		addTarget();
//	}
	
	
	public void moveSprite(CCSprite sprite) {
		float xSpeed = genFloat(10.0f) - 5.0f;
	    float ySpeed = genFloat(10.0f) - 5.0f;
	    float x = sprite.getPosition().x;
	    float y = sprite.getPosition().y;
	    
        if (x > winSize_.width - x - xSpeed || x + xSpeed < 0) {
            xSpeed = -xSpeed;
        }
        if (y > winSize_.height - y - ySpeed || y + ySpeed < 0) {
            ySpeed = -ySpeed;
        }
        sprite.setPosition(x + xSpeed, y + ySpeed);
	}
	public void moveSpriteNew(CCSprite sprite) {
		float newx = genPos(winSize_.width, sprite.getContentSize().width);
		float newy = genPos(winSize_.height, sprite.getContentSize().height);
		CGPoint realDest = CGPoint.ccp(newx, newy);
		int offRealX = (int)(newx - sprite.getPosition().x);
		int offRealY = (int)(newy - sprite.getPosition().y);
		float length = (float)Math.sqrt((offRealX * offRealX) + (offRealY * offRealY));
		float velocity = VELOCITY / 1.0f; // 480 pixels / 1 sec
		float realMoveDuration = length / velocity;
		
		// Move projectile to actual endpoint
		sprite.runAction(CCSequence.actions(
				CCMoveTo.action(realMoveDuration, realDest),
				CCCallFuncN.action(this, "spriteMoveFinished")));
	}
	public void move(float dt) {
		moveSpriteNew(target_);
		for (int i = 0; i < fakes_.size(); i++) {
			CCSprite fake = fakes_.get(i);
			moveSpriteNew(fake);
		}
	}
	
	public CGRect getRect(CCSprite sprite) {
		return CGRect.make(sprite.getPosition().x - sprite.getContentSize().width / 2.0f,
				sprite.getPosition().y - sprite.getContentSize().height / 2.0f,
				sprite.getContentSize().width,
				sprite.getContentSize().height);
	}
	public CGRect getRectFlex(CCSprite sprite) {
		return CGRect.make(sprite.getPosition().x - sprite.getContentSize().width,
				sprite.getPosition().y - sprite.getContentSize().height,
				sprite.getContentSize().width * 2.0f,
				sprite.getContentSize().height * 2.0f);
	}
	
	boolean intersect(CCSprite spriteA, CCSprite spriteB) {
		CGRect ARect = getRect(spriteA);
		CGRect BRect = getRect(spriteB);
		return CGRect.intersects(ARect, BRect);
	}
	boolean intersectFlex(CCSprite spriteA, CCSprite spriteB) {
		CGRect ARect = getRectFlex(spriteA);
		CGRect BRect = getRectFlex(spriteB);
		return CGRect.intersects(ARect, BRect);
	}
	
	public void update(float dt)
	{
		
		CGRect playerRect = getRect(player_);
		CGRect targetRect = getRect(target_);
		if (CGRect.intersects(playerRect, targetRect)) {
			CCDirector.sharedDirector().replaceScene(GameOverLayer.scene("You Win!"));
			return;
		} else {
			for (CCSprite fake : fakes_) {
				CGRect fakeRect = getRect(fake);
				if (CGRect.intersects(playerRect, fakeRect)) {
					CCDirector.sharedDirector().replaceScene(GameOverLayer.scene("You Lost :("));
					return;
				}
			}
		}
	}
	
//	protected void addTarget()
//	{
//		Random rand = new Random();
//		CCSprite target = CCSprite.sprite("Target.png");
//		
//		// Determine where to spawn the target along the Y axis
//		CGSize winSize = CCDirector.sharedDirector().displaySize();
//		int minY = (int)(target.getContentSize().height / 2.0f);
//		int maxY = (int)(winSize.height - target.getContentSize().height / 2.0f);
//		int rangeY = maxY - minY;
//		int actualY = rand.nextInt(rangeY) + minY;
//		
//		// Create the target slightly off-screen along the right edge,
//		// and along a random position along the Y axis as calculated above
//		target.setPosition(CGPoint.ccp(winSize.width + (target.getContentSize().width / 2.0f) , actualY/ 1.0f));
//		addChild(target);
//		
//		target.setTag(1);
//		_targets.add(target);
//		
//		// Determine speed of the target
//		int minDuration = 2;
//		int maxDuration = 4;
//		int rangeDuration = maxDuration - minDuration;
//		int actualDuration = rand.nextInt(rangeDuration) + minDuration;
//		
//		// Create the actions
//		CCMoveTo actionMove = CCMoveTo.action(actualDuration, CGPoint.ccp(-target.getContentSize().width / 2.0f, actualY));
//		CCCallFuncN actionMoveDone = CCCallFuncN.action(this, "spriteMoveFinished");
//		CCSequence actions = CCSequence.actions(actionMove, actionMoveDone);
//		
//		target.runAction(actions);
//	}
	
	public void spriteMoveFinished(Object sender)
	{
	}
	
	public float genFloat(float r) {
		return rand_.nextFloat() * r;
	}
	public float genPos(float r, float img) {
		float pos = genFloat(r);
		System.out.println("" + pos);
		if (pos < img / 2.0) pos = img / 2.0f;
		else if (pos > r - img / 2.0) pos = r - img / 2.0f;
		return pos;
	}
}
