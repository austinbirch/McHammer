package uk.co.austinbirch;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/**
 * @author Austin Birch
 *
 * The Background class creates, renders and updates a space background
 * that has scrolling stars etc.
 */
public class Background {
    
    // Member variables
    /**
     * The rectangle we are rendering in
     */
    protected Rectangle boundingRect;
    
    /**
     * The amount of stars to render
     */
    protected int starCount = 200;
    
    /**
     * The color with which we should render the stars
     */
    protected Color starColor = Color.white;
    
    /**
     * The color that we should render as the color of "space"
     */
    protected Color abyssColor = Color.black;
    
    /**
     * The array to store all of the stars
     */
    protected ArrayList<Vector2f> starField;
    
    /**
     * The width of a rendered star
     */
    protected float starWidth = 2.0f;
    
    /**
     * The height of a rendered star
     */
    protected float starHeight = 4.0f;
    
    /**
     * The velocity of the stars
     */
    protected Vector2f starVelocity = new Vector2f(-10.0f, 20.0f);
    
    /**
     * Creates a new background in the specified rect
     * 
     * @param rect
     */
    public Background(Rectangle rect) {
        super();
        this.boundingRect = rect;
        this.starField = new ArrayList<Vector2f>();
        
        // create a star field
        this.generateStarfield();
    }
    
    /**
     * Creates a star field that suits the parameters set for this background,
     * and puts the resultant stars into starField array.
     */
    protected void generateStarfield() {
        for (int i = 0; i < this.starCount; i++) {
            // generate a random position vector for a star
            float minX = this.boundingRect.getX();
            float maxX = this.boundingRect.getX() + this.boundingRect.getWidth();
            float minY = this.boundingRect.getY();
            float maxY = this.boundingRect.getY() + this.boundingRect.getHeight();
            float xPos = minX + (int)(Math.random() * ((maxX - minX) + 1));
            float yPos = minY + (int)(Math.random() * ((maxY - minY) + 1));
            
            // store it
            Vector2f starPos = new Vector2f(xPos, yPos);
            this.starField.add(starPos);
        }
    }
    
    /**
     * Updates the background
     * 
     * @param gc the game container we belong to
     * @param deltaSeconds the amount of seconds passed since last update
     * @return 
     */
    public void update(GameContainer gc, float deltaSeconds) {
        for (Vector2f star : this.starField) {
            star.x += this.starVelocity.x * deltaSeconds;
            star.y += this.starVelocity.y * deltaSeconds;
            
            // apply an offset so that we come onto screen, not appear suddenly
            float offset = 2.0f;
            
            // if we have moved off of the bounding rect in the x-axis
            if (star.x < (this.boundingRect.getX() - offset)) {
                // put star back on the right
                star.x = this.boundingRect.getX() + this.boundingRect.getWidth() + offset;
            } else if (star.x > (this.boundingRect.getX() + 
                    this.boundingRect.getWidth() + offset)) {
                // put star back on the left
                star.x = this.boundingRect.getX() - offset;
            }
            
            // if we have moved off of the bounding rect in the y-axis
            if (star.y < (this.boundingRect.getY() - offset)) {
                // put star back on the bottom
                star.y = this.boundingRect.getY() + this.boundingRect.getHeight() + offset;
            } else if (star.y > (this.boundingRect.getY() + 
                    this.boundingRect.getHeight() + offset)) {
                // put star back on the top
                star.y = this.boundingRect.getY() - offset;
            }            
        }
    }
    
    /**
     * Renders the background by iterating through all of the stored stars,
     * and drawing them with the current star color
     * 
     * @param gc the GameContainer we belong to
     * @param g the graphics context to draw to
     * @return 
     */
    public void render(GameContainer gc, Graphics g) {
        // store the old color so we can reset it
        Color oldColor = g.getColor();
        
        // render the space background color
        g.setColor(abyssColor);
        g.fillRect(this.boundingRect.getX(),
                   this.boundingRect.getY(),
                   this.boundingRect.getWidth(),
                   this.boundingRect.getHeight());
        
        // set to the star color
        g.setColor(starColor);
        
        // draw the stars
        for (Vector2f star : this.starField) {
            g.fillRect(star.x, star.y, this.starWidth, this.starHeight);
        }
        
        // reset the old color for the graphics context
        g.setColor(oldColor);
    }
    
}
