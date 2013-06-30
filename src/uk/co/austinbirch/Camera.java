package uk.co.austinbirch;

import org.newdawn.slick.geom.Vector2f;

/**
 * @author Austin Birch
 * 
 * Camera class to allow us to render arbitrary regions of the overall world
 *
 */
public class Camera {
    
    /**
     * The position of the camera
     */
    protected Vector2f position;
    
    /**
     * The viewable world. .x = width, .y = height 
     */
    protected Vector2f viewableWorld;
    
    /**
     * The width and height of the camera view
     */
    protected Vector2f viewingArea;
    
    /**
     * Creates a camera class
     * 
     * @param viewableWorldWidth the width that we can pan around
     * @param viewableWorldHeight the height that we can pan around
     */
    public Camera(float viewableWorldWidth,
                  float viewableWorldHeight,
                  float viewWidth,
                  float viewHeight) {
        this.position = new Vector2f(0.0f, 0.0f);
        this.viewableWorld = new Vector2f(viewableWorldWidth,
                                          viewableWorldHeight);
        this.viewingArea = new Vector2f(viewWidth, viewHeight);
    }
    
    /**
     * Sets the position of the Camera
     * 
     * @param position the position to set this Camera to
     */
    public void setPosition(Vector2f position) {
        this.position = new Vector2f();
        this.position.x = position.x - (this.viewingArea.x / 2);
        this.position.y = position.y - (this.viewingArea.y / 2);
        
        // check the limits
        if (this.position.x < 0) {
            this.position.x = 0;
        } else if (this.position.x + this.viewingArea.x > this.viewableWorld.x) {
            this.position.x = this.viewableWorld.x - this.viewingArea.x;
        }
        
        if (this.position.y < 0) {
            this.position.y = 0;
        } else if (this.position.y + this.viewingArea.y > this.viewableWorld.y) {
            this.position.y = this.viewableWorld.y - this.viewingArea.y;
        }
    }
    
    /**
     * Returns the position of the Camera
     * 
     * @return the current Camera position
     */
    public Vector2f getPosition() {
        return new Vector2f(this.position);
    }

}
