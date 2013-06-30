package uk.co.austinbirch;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Ellipse;
import org.newdawn.slick.geom.Vector2f;

/**
 * @author Austin Birch
 * 
 * A physical world object
 *
 */
public class World extends Entity {
    
    // Member variables
    
    /**
     * The radius of the world
     */
    protected float radius;
    
    /**
     * Creates a world entity 
     */
    public World() {
        super();
        // try and load the world sprite
        try {
            this.sprite = new Image("res/images/world_1.png");
            this.radius = this.sprite.getWidth() / 2;
            this.mass = 2500.0f;
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a world Entity and sets its position
     * 
     * @param position the position to set the world to
     */
    public World(Vector2f position) {
        this();
        this.position = position;
    }
    
    /**
     * Return the collision Ellipse for this world
     * 
     * We use an Ellipse because the implementation for Circle is buggy in
     * Slick2D
     * 
     * @return
     */
    public Ellipse collisionCircle() {
        // TODO: Locate bug in Slick2D Circle 
        Ellipse e = new Ellipse(this.position.x + this.radius,
                                this.position.y + this.radius,
                                this.radius,
                                this.radius);
        return e;
    }

    @Override
    public void render(GameContainer gc, Graphics g) {
        g.drawImage(this.sprite, this.position.x, this.position.y);
        if (McHammerGame.DEBUG_MODE) {
            Color oldColor = g.getColor();
            
            g.setColor(new Color(0.8f, 0.0f, 0.0f, 0.5f));
            g.fill(this.collisionCircle());
            
            g.setColor(oldColor);
        }
    }
    
}
