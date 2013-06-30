package uk.co.austinbirch;

import java.util.Random;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Ellipse;
import org.newdawn.slick.geom.Vector2f;

public class Alien extends Entity {

    /**
     * The destination in which they will release their payload
     */
    protected Vector2f destinationPosition;
    
    /**
     * The position from which we will start
     */
    protected Vector2f startingPosition;
    
    /**
     * Whether this alien is leaving the stage, or entering it
     */
    protected boolean leavingStage = false;
    
    /**
     * The payload we are launching
     */
    protected Building payload;
    
    /**
     * Creates a new alien
     */
    public Alien() {
        try {
            this.sprite = new Image("res/images/alien.png");
            this.setupPositions();
            Vector2f payloadPosition = new Vector2f();
            payloadPosition.x = this.position.x;
            payloadPosition.y = this.position.y + this.sprite.getHeight();
            this.payload = new Building(payloadPosition,
                                        this.velocity);
            McHammerGame.getInstance().addBuilding(this.payload);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generates the starting and destination positions for this alien
     * @throws SlickException 
     */
    protected void setupPositions() throws SlickException {
        // select a target destination first
        float minX = 0.0f;
        float maxX = McHammerGame.getInstance().spaceWidth;
        float minY = 0.0f;
        float maxY = McHammerGame.getInstance().spaceHeight; 
        
        float xPos = minX + (int)(Math.random() * ((maxX - minX) + 1));
        float yPos = minY + (int)(Math.random() * ((maxY - minY) + 1));
        
        this.destinationPosition = new Vector2f(xPos, yPos);
        
        // generate a random distance away
        int distance = 700 + (int)(Math.random() * ((5000 - 700) + 1));
        
        // calculate the starting position
        double delta = Math.sin(Math.toRadians(45.0f)) * distance;
        Random rand = new Random();
        int x = rand.nextInt(4);
        
        float alienVel = 200.0f;
        if (x < 1) {
            this.startingPosition = new Vector2f((float)(xPos - delta), (float)(yPos - delta));
            this.velocity = new Vector2f(alienVel, alienVel);   
        } else if (x < 2) {
            this.startingPosition = new Vector2f((float)(xPos + delta), (float)(yPos - delta));
            this.velocity = new Vector2f(-alienVel, alienVel);               
        } else if (x < 3) {
            this.startingPosition = new Vector2f((float)(xPos + delta), (float)(yPos + delta));
            this.velocity = new Vector2f(-alienVel, -alienVel);   
        } else if (x < 4) {
            this.startingPosition = new Vector2f((float)(xPos - delta), (float)(yPos + delta));
            this.velocity = new Vector2f(alienVel, -alienVel);   
        }
        
        this.position = new Vector2f(this.startingPosition);
    }
    
    @Override
    public void update(GameContainer gc, float deltaSeconds) throws SlickException {
        if (this.leavingStage) {
            // we are on the way out, remove ourselves
            if (this.position.x < -100.0f || 
                    this.position.x > McHammerGame.getInstance().spaceWidth + 100.0f ||
                    this.position.y < -100.0f ||
                    this.position.y > McHammerGame.getInstance().spaceHeight + 100.0f)
                McHammerGame.getInstance().removeAlien(this);
        }
        
        Ellipse nearDetection = new Ellipse(this.position.x, this.position.y, 5.0f, 5.0f);
        if (nearDetection.contains(this.destinationPosition.x, this.destinationPosition.y)) {
            if (!this.leavingStage) {
                // we are on the way in, go back!
                this.destinationPosition = this.startingPosition;
                // reverse our velocity
                this.velocity.x *= -1;
                this.velocity.y *= -1;
                // we are now reversing the motion
                this.leavingStage = true;
            }
        }
        
        this.position.x += this.velocity.x * deltaSeconds;
        this.position.y += this.velocity.y * deltaSeconds;
    }
    
}
