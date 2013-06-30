package uk.co.austinbirch;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/**
 * @author Austin Birch
 * 
 * The throwable hammer that the player can use
 *
 */
public class Hammer extends Entity implements CollisionListener {
    
    /**
     * The visual representation of the Hammer
     */
    protected Image sprite;
    
    /**
     * The rotation of the Hammer, we will want it to spin around
     */
    protected float rotation;
    
    /**
     * Speed of the hammer rotation
     */
    protected float rotationSpeed = 360.0f;
    
    /**
     * Creates a Hammer to throw
     * @throws SlickException 
     */
    public Hammer(Vector2f position) throws SlickException {
        super();
        this.sprite = new Image("res/images/hammer.png");
        this.position = position;
        this.rotation = 0.0f;
        this.mass = 1000;
    }
    
    @Override
    public void update(GameContainer gc, float deltaSeconds) {
        this.rotation += this.rotationSpeed * deltaSeconds;
        
        // update the position based on our velocity
        this.position.x += this.velocity.x * deltaSeconds;
        this.position.y += this.velocity.y * deltaSeconds;
    }
    
    @Override
    public void render(GameContainer gc, Graphics g) {
        this.sprite.setRotation(this.rotation);
        g.drawImage(this.sprite, this.position.x, this.position.y);
    }

    @Override
    public void onCollision(Rectangle collisionRect) {
        // this will be us leaving the world. We just need to delete ourselves
        try {
            McHammerGame.getInstance().removeHammer(this);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCollision(Entity entity) {
        if (entity instanceof Building) {
            try {
                McHammerGame.getInstance().removeHammer(this);
            } catch (SlickException e) {
                e.printStackTrace();
            }
        }
    }

}
