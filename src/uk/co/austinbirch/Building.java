package uk.co.austinbirch;

import java.util.Random;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import uk.co.austinbirch.physics.ABPhysicsHelper;

public class Building extends Entity implements CollisionListener {

    /**
     * Has this been destroyed yet?
     */
    protected boolean isDead = false;
    
    /**
     * Are we on a planet? 
     */
    public boolean onPlanet = false;
    
    /**
     * How much damage we have done to the score
     */
    public int damageDone = 0;
    
    /**
     * For measuring how long we have been causing damage 
     */
    protected float damageTimeAccumulator = 0.0f;
    
    /**
     * How long to wait before the damage is incremented
     */
    protected float damageTimeLimit = 1000.0f;
    
    /**
     * Creates a new building with a random building sprite
     * @throws SlickException 
     */
    public Building(Vector2f position, Vector2f velocity) throws SlickException {
        super();
        this.position = new Vector2f(position);
        this.velocity = new Vector2f(velocity);
        Random rand = new Random();
        int x = rand.nextInt(3);
        if (x < 1) {
            this.sprite = new Image("res/images/building_01.png");
        } else if (x < 2) {
            this.sprite = new Image("res/images/building_02.png");
        } else if (x < 3) {
            this.sprite = new Image("res/images/building_03.png");
        }
    }
  
    @Override
    public void update(GameContainer gc, float deltaSeconds) throws SlickException {
        if (this.isDead) {
            McHammerGame.getInstance().removeBuilding(this);
        }
        
        if (this.onPlanet) {
            this.damageTimeAccumulator += deltaSeconds;
            if (this.damageTimeAccumulator >= this.damageTimeLimit/100.0f) {
                Random rand = new Random();
                int x = rand.nextInt(9);
                this.damageDone += 20 + x;
                this.damageTimeAccumulator = 0.0f;
            }
        }
        
        this.position.x += this.velocity.x * deltaSeconds;
        this.position.y += this.velocity.y * deltaSeconds;
    }
    
    @Override
    public void render(GameContainer gc, Graphics g) {
        g.drawImage(this.sprite, this.position.x, this.position.y);
    }

    @Override
    public void onCollision(Rectangle collisionRect) {
        // not really interested in this
    }

    @Override
    public void onCollision(Entity entity) {
        if (entity instanceof World) {
            Vector2f mtv = ABPhysicsHelper.calculateMTV(this.collisionRectangle(),
                                                        ((World) entity).collisionCircle());
            this.position = this.position.add(mtv);
            this.velocity = new Vector2f(0.0f, 0.0f);
            this.onPlanet = true;
        } else if (entity instanceof Hammer) {
            this.isDead = true;
            this.onPlanet = false;
        }
    }
    
}
