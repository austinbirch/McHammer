package uk.co.austinbirch;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/**
 * @author Austin Birch
 *
 *  An Entity is an object that can have a position, a velocity, a mass,
 *  an on-screen representation, and can be involved in collisions  
 *
 */
public class Entity {
    
    // Member variables
    /**
     * The Entities position
     */
    protected Vector2f position;
    
    /**
     * The velocity of this Entity
     */
    protected Vector2f velocity;
    
    /**
     * The mass of this Entity, used for physics calculations
     */
    protected float mass;
    
    /**
     * The visual representation of this Entity
     */
    protected Image sprite;
    
    /**
     * Creates a new Entity 
     */
    public Entity() {
        super();
        this.position = new Vector2f(0.0f, 0.0f);
        this.velocity = new Vector2f(0.0f, 0.0f);
        this.mass = 0.0f;
    }
    
    /**
     * Creates a new Entity
     * 
     * @param position the default position for this Entity
     * @param velocity the default velocity for this Entity
     * @param mass the mass of this Entity
     */
    public Entity(Vector2f position, 
                  Vector2f velocity,
                  float mass) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
    }
    
    /**
     * Creates a new Entity with a position, velocity, mass, and 
     * an image.
     * 
     * @param position the default position for this Entity
     * @param velocity the default velocity for this Entity
     * @param mass the mass of this Entity
     * @param sprite the visual representation of this Entity
     */
    public Entity(Vector2f position,
                  Vector2f velocity,
                  float mass,
                  Image sprite) {
        this(position, velocity, mass);
        this.sprite = sprite;
    }    
    
    /**
     * Render the Entity using the graphics context that is passed
     * 
     * If this method is not overridden, it will just render the current
     * sprite to the screen if there is one.
     * 
     * @param gc the GameContainer we belong to
     * @param g the graphics context to draw to
     * @throws SlickException
     */
    public void render(GameContainer gc, Graphics g) throws SlickException {
        g.drawImage(this.sprite, this.position.x, this.position.y);
        if (McHammerGame.DEBUG_MODE) {
            Color oldColor = g.getColor();
            
            g.setColor(new Color(0.5f, 0.5f, 0.0f, 0.5f));
            g.fill(this.collisionRectangle());
            
            g.setColor(oldColor);
        }
    }
    
    /**
     * Sets the velocity of this Entity
     * 
     * @param x the x component of the velocity
     * @param y the y component of the velocity
     */
    public void setVelocity(float x, float y) {
        this.velocity = new Vector2f(x, y);
    }

    /**
     * Update the Entity based on a timestep that has been passed
     * 
     * @param gc the GameContainer we belong to
     * @param delta the time in seconds since the last update
     * @throws SlickException
     */
    public void update(GameContainer gc, float deltaSeconds) throws SlickException {
        // should be overridden in subclass
    }
    
    /**
     * Calculates and returns the collision Rectangle for this Entity
     * 
     * @return the collision Rectangle for this Entity
     */
    public Rectangle collisionRectangle() {
        if (this.sprite != null) {
            Rectangle rect = new Rectangle(this.position.x,
                    this.position.y,
                    this.sprite.getWidth(),
                    this.sprite.getHeight());
            return rect;
        } else {
            return new Rectangle(this.position.x,
                                 this.position.y,
                                 0, 0);
        }
    }

}
