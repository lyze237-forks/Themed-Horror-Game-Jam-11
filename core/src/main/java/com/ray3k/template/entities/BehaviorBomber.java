package com.ray3k.template.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.ray3k.template.Resources.*;
import dev.lyze.gdxUnBox2d.BehaviourState;
import dev.lyze.gdxUnBox2d.Box2DGameObject;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;
import dev.lyze.gdxUnBox2d.behaviours.fixtures.CreateCircleFixtureBehaviour;

import static com.ray3k.template.Core.*;
import static com.ray3k.template.Resources.SpineBomber.*;
import static com.ray3k.template.screens.GameScreen.*;
import static dev.lyze.gdxUnBox2d.box2D.BodyDefType.DynamicBody;

public class BehaviorBomber extends BehaviourAdapter<Box2DGameObject> {
    private Box2DGameObject go;
    private EntityData ed;
    public float startX;
    public float startY;
    public BehaviorBomber(Box2DGameObject gameObject) {
        super(gameObject);
    }
    public float timer = 10.0f;
    
    @Override
    public void start() {
        go = getGameObject();
        ed = new EntityData(go, skeletonData, animationData, startX, startY, DEPTH_ENEMIES);
        ed.animationState.setAnimation(0, animationSpawn, false);
        ed.animationState.setAnimation(1, animationSpark, true);
        ed.animationState.addListener(new AnimationStateAdapter() {
            @Override
            public void complete(TrackEntry entry) {
                if (entry.getAnimation() == animationShake) {
                    ed.score = 0;
                    boolean destroyed = getState() == BehaviourState.DESTROYED || getState() == BehaviourState.DESTROYING;
                    if (!destroyed) go.destroy();
                    var explosion = new Box2DGameObject(DynamicBody, unBox);
                    var explosionBehavior = new BehaviorExplosion(explosion);
                    explosionBehavior.startX = m2p(go.getBody().getPosition().x);
                    explosionBehavior.startY = m2p(go.getBody().getPosition().y);
                    
                    for (int angle = 0; angle < 360; angle += 360 / 8) {
                        var bullet = new Box2DGameObject(DynamicBody, unBox);
                        var bulletBehavior = new BehaviorBullet(bullet, SpineBullet.skinShell, explosionBehavior.startX, explosionBehavior.startY, angle, 400);
                        bulletBehavior.damage = 100;
                    }
                }
            }
        });
        ed.health = 500;
        ed.score = 300;
        
        var def = new FixtureDef();
        def.filter.categoryBits = CATEGORY_CHARACTER;
        new CreateCircleFixtureBehaviour(Vector2.Zero, p2m(15), def, go);
        
        new BehaviorEntity(go);
        new BehaviorZombieMovement(go, 50);
        new BehaviorEnemy(go);
    }
    
    @Override
    public void update(float delta) {
        var body = go.getBody();
        ed.skeleton.getRootBone().setRotation(body.getAngle() * MathUtils.radDeg);
        
        if (timer >= 0) {
            timer -= delta;
            if (timer < 0) {
                ed.animationState.setAnimation(0, animationShake, false);
            }
        }
    }
    
    @Override
    public void onDestroy() {
    }
}
