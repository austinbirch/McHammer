package uk.co.austinbirch;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.util.Log;

import uk.co.austinbirch.physics.ABPhysicsHelper;

public class Player extends Entity implements CollisionListener {
    
    /**
     * The sprite for the base of McHammer's body
     */
    protected Image bodySprite;
    
    /**
     * The image to use when the jetpack is on in normal mode
     */
    protected Image jetpackOnSprite;
    
    /**
     * The image to use when the jetpack is off 
     */
    protected Image jetpackOffSprite;
    
    /**
     * The image to use when the jetpack is on in reverse mode 
     */
    protected Image jetpackReverseSprite;
    
    /**
     * Whether we should be rotating to the right
     */
    protected boolean rotatingRight = false;
    
    /**
     * Whether we should be rotating to the left
     */
    protected boolean rotatingLeft = false;
    
    /**
     * Our current amount of rotation
     */
    protected float rotation = 0.0f;
    
    /**
     * The speed in which we should rotate (in degrees per second)
     */
    protected float rotationSpeed = 180.0f;
    
    /**
     * The velocity that the jetpack gives to the player 
     */
    protected float jetpackVelocity = 400.0f;
    
    /**
     * The absolute maximum velocity we are allowed to travel at
     */
    protected float maxVelocity = 800.0f;
    
    /**
     * Whether or not we should be applying the jetpack velocity
     */
    protected boolean jetpackActive = false;
    
    /**
     * Wheter we should be reversing the jetpack velocity
     */
    protected boolean jetpackReverse = false;
    
    /**
     * Creates a player object
     */
    public Player() {
        super();
        try {
            this.jetpackOffSprite = new Image("res/images/McHammer.png");
            this.jetpackOnSprite = new Image("res/images/McHammer_JetpackOn.png");
            this.jetpackReverseSprite = new Image("res/images/McHammer_JetpackReverse.png");
            this.bodySprite = this.jetpackOffSprite;
            this.mass = 70.0f;
            this.velocity = new Vector2f(0.0f, 0.0f);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a player with a default position
     * 
     * @param position the default position
     */
    public Player(Vector2f position) {
       this();
       this.position = position;
    }
    
    /** 
     * Custom render method for McHammer, as he needs to be built from multiple
     * separate sprites (body, arms, jetpack etc..)
     */
    @Override
    public void render(GameContainer gc, Graphics g) {
        // rotate the sprites
        this.bodySprite.setRotation(this.rotation);
        
        // draw the bodySprite
        g.drawImage(this.bodySprite, this.position.x, this.position.y);
        
        if (McHammerGame.DEBUG_MODE) {
            Color oldColor = g.getColor();
            
            g.setColor(new Color(0.9f, 0.0f, 0.0f, 0.6f));
            g.fill(this.collisionShape());
            
            g.setColor(Color.green);
            g.fillOval(this.position.x + this.bodySprite.getCenterOfRotationX(),
                       this.position.y + this.bodySprite.getCenterOfRotationY(), 4, 4);
            
            g.setColor(oldColor);
        }
    }
    
    /**
     * Turns on the jetpack, which gives him velocity in a parallel direction
     * to that of the jetpack.
     */
    public void setJetpackOn() {
        this.jetpackActive = true;
        this.jetpackReverse = false;
        this.bodySprite = this.jetpackOnSprite;
    }
    
    /**
     * Turns off the jetpack.
     */
    public void setJetpackOff() {
        this.jetpackActive = false;
        this.jetpackReverse = false;
        this.bodySprite = this.jetpackOffSprite;
    }
    
    /**
     * Turns the jetpack on in reverse
     */
    public void setJetpackReverse() {
        this.jetpackActive = false;
        this.jetpackReverse = true;
        this.bodySprite = this.jetpackReverseSprite;
    }
    
    @Override
    public void update(GameContainer gc, float deltaSeconds) {
        
        // update rotation
        if (this.rotatingRight) {
            this.rotation += this.rotationSpeed * deltaSeconds;
            if (rotation > 360.0f) {
                rotation = 0.0f;
            }
        } else if (this.rotatingLeft) {
            this.rotation -= this.rotationSpeed * deltaSeconds;
            if (rotation < 0.0f) {
                rotation = 360.0f;
            }
        }
        
        if (this.jetpackActive || this.jetpackReverse) {
            // calculate the velocity components for x and y
            double theta = Math.toRadians(this.rotation + 90.0f);
            double xVel = Math.cos(theta) * this.jetpackVelocity * deltaSeconds;
            double yVel = Math.sin(theta) * this.jetpackVelocity * deltaSeconds;
            
            if (this.jetpackActive) {
                this.velocity.x += (float)xVel;
                this.velocity.y -= (float)yVel;
            } else if (this.jetpackReverse) {
                this.velocity.x -= (float)xVel;
                this.velocity.y += (float)yVel;
            }
        }
        
        // make sure we are not traveling too fast
        if (Math.abs(this.velocity.x) > this.maxVelocity) {
            if (velocity.x < 0) {
                this.velocity.x = -1 * this.maxVelocity;
            } else {
                this.velocity.x = this.maxVelocity;
            }
        } 
        
        if (Math.abs(this.velocity.y) > this.maxVelocity) {
            Log.info("MAX VEL!");
            if (velocity.y < 0) {
                this.velocity.y = -1 * this.maxVelocity;
            } else {
                this.velocity.y = this.maxVelocity;
            }
        }
        
        // move the player
        this.position.x -= this.velocity.x * deltaSeconds;
        this.position.y += this.velocity.y * deltaSeconds;
    }
    
    /**
     * Returns the collision Shape for this player object.
     * 
     * @return the collision Shape for the player
     */
    public Shape collisionShape() {
        Rectangle rect = new Rectangle(this.position.x,
                                       this.position.y,
                                       this.bodySprite.getWidth(),
                                       this.bodySprite.getHeight());
        return rect.transform(Transform.createRotateTransform(
                (float) Math.toRadians(this.rotation), 
                this.position.x + this.bodySprite.getCenterOfRotationX(),
                this.position.y + this.bodySprite.getCenterOfRotationY()));
    }
    
    /**
     * Returns the rectangle that contains all of our parts
     * 
     * @return the bounding rectangle for the player
     */
    public Rectangle boundingRectangle() {
        Rectangle rect = new Rectangle(this.position.x,
                                       this.position.y,
                                       this.bodySprite.getWidth(),
                                       this.bodySprite.getHeight());
        return rect;
    }

    /**
     * Sets this player to be rotating right, and disables rotatingLeft
     */
    public void setRotatingRight() {
        this.rotatingRight = true;
        this.rotatingLeft = false;
    }
    
    /**
     * Sets this player to be rotating left, and disables rotatingRight
     */
    public void setRotatingLeft() {
        this.rotatingLeft = true;
        this.rotatingRight = false;
    }
    
    /**
     * Sets this player to not be rotating
     */
    public void setNotRotating() {
        this.rotatingLeft = false;
        this.rotatingRight = false;
    }
    
    /**
     * Throws a hammer in our current direction 
     */
    public void throwHammer() {
        Hammer hammer;
        try {
            hammer = new Hammer(new Vector2f(this.position.x + 10.0f, this.position.y + 24.0f));
            
            // calculate the velocity/direction for the hammer
            double theta = Math.toRadians(this.rotation);
            double xVel = Math.cos(theta) * 400.0f;
            double yVel = Math.sin(theta) * 400.0f; 
            hammer.velocity = new Vector2f((float)xVel, (float)yVel);
            
            McHammerGame.getInstance().addHammer(hammer);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Notification of collision between us and another Rectangle.
     * 
     * This method is exclusively used for collisions agains the boundaries of 
     * space, any other collisions will have an Entity as the colliding 
     * parameter.
     *  
     */
    @Override
    public void onCollision(Rectangle collisionRect) {
        // get the MTV and resolve
        Vector2f mtv = ABPhysicsHelper.calculateMTV(this.boundingRectangle(),
                                                    collisionRect);
        this.position = this.position.add(mtv);
        this.velocity = new Vector2f((float)(this.velocity.x * -0.5), (float)(this.velocity.y * -0.5));
    }

    @Override
    public void onCollision(Entity entity) {
        // get the MTV and resolve
        if (entity instanceof World) {
            Vector2f mtv = ABPhysicsHelper.calculateMTV(this.boundingRectangle(),
                                                        ((World) entity).collisionCircle());
            this.position = this.position.add(mtv);
            this.velocity = new Vector2f((float)(this.velocity.x * -1), (float)(this.velocity.y * -1));
        }
    }

}
