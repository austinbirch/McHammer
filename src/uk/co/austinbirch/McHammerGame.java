package uk.co.austinbirch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.Sys;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.util.Log;

import es.darkhogg.util.OperatingSystem;

public class McHammerGame extends BasicGame implements KeyListener {
    
    // Global variables
    
    protected final int GAME_OVER = 1;
    protected final int GAME_RUNNING = 2;
    protected final int GAME_PAUSED = 3;
    protected final int GAME_MENU = 4;
    
    /**
     *  Determines whether we are in debug mode or not.
     *  
     *  If we are in debug mode, we may wish to draw bounding boxes,
     *  collisionRects etc in the appropriate render() methods
     */
    public static final boolean DEBUG_MODE = false;
    
    /**
     * The McHammer instance (McHammerGame is a Singleton) 
     */
    private static McHammerGame _instance = null;
    
    // Member variables
    
    /**
     * The total width of the space environment we can move in 
     */
    protected int spaceWidth = 2000;
    
    /**
     * The total width of the space environment we can move in
     */
    protected int spaceHeight = 1000;
    
    /**
     * The rectangles that make up the boundaries of our "space"
     */
    protected ArrayList<Rectangle> spaceBoundaries;
    
    /**
     * Contains all of the worlds the player owns
     */
    protected ArrayList<World> worlds;
    
    /**
     * The background image/animation for the game 
     */
    protected Background background;
    
    /**
     * The camera that we are viewing through
     */
    protected Camera camera;
    
    /**
     * The player's character
     */
    protected Player mcHammer;
    
    /**
     * The hammers that we might be throwing
     */
    protected CopyOnWriteArrayList<Hammer> hammerList;

    /**
     * The array that will contain any buildings
     */
    protected CopyOnWriteArrayList<Building> buildings;
    
    /**
     * The array that will contain any attacking aliens
     */
    protected CopyOnWriteArrayList<Alien> aliens;

    /**
     * How long we have been playing for
     */
    protected float elapsedGameTime = 0.0f;
    
    /**
     * Stores the amount since the last alien release 
     */
    protected float alienReleaseTimer = 0.0f;
    
    /**
     * The amount of time to wait between alien releases, this will get lower
     */
    protected float alienReleaseLimit = 1000.0f;
    
    /**
     * The amount of aliens to release each time
     */
    protected float alienReleaseCount = 0.0f;
    
    /**
     * We don't want any more aliens than this
     */
    protected int alienCountLimit = 30;
    
    /**
     * The current game state
     */
    protected int gameState = 4;
    
    /**
     * The player's score
     */
    protected int propertyValue = 1000;
    
    // Methods
    
    /**
     * Gets the current instance of the ConvictedGame
     * 
     * @return the current instance of the ConvictedGame
     * @throws SlickException
     */
    public static synchronized McHammerGame getInstance()
            throws SlickException {
        if (_instance == null) {
            _instance = new McHammerGame();
        }
        return _instance;
    }
    
    public McHammerGame() {
        super("McHammer!");
    }
    
    public static void main(String[] args) throws SlickException {
        try {
            setupLibraryPath();
        } catch (Throwable ex) {
            Log.error(ex);
            Sys.alert("Fatal error", 
                    "fatal error occurred during game execution:\n\t" + ex.toString());
            System.exit(1);
        }
        
        AppGameContainer app = new AppGameContainer(new McHammerGame());
        app.setDisplayMode(800, 600, false);
        app.setTargetFrameRate(60);
        app.start();
    }
    
    /**
     * Modifies the <tt>java.library.path</tt> system property so it contains the appropriate folder with the LWJGL
     * natives
     */
    private static void setupLibraryPath () throws SecurityException, NoSuchFieldException, IllegalAccessException {
        OperatingSystem os = OperatingSystem.getCurrent();

        // If the OS is supported by LWJGL (Linux, Mac, Windows, Solaris)
        if ( os == OperatingSystem.LINUX | os == OperatingSystem.MAC | os == OperatingSystem.SOLARIS
            | os == OperatingSystem.WINDOWS )
        {
            String dirname = "natives-" + os.getName().toLowerCase();

            String pathsep = System.getProperty( "path.separator" );
            String dirsep = System.getProperty( "file.separator" );
            String oldlibpath = System.getProperty( "java.library.path" );

            // <oldpath>;lib/natives-os
            String newlibpath = oldlibpath + pathsep + "lib" + dirsep + dirname;
            System.setProperty( "java.library.path", newlibpath );

            // Propagate the change
            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
            fieldSysPath.setAccessible( true );

            if ( fieldSysPath != null ) {
                fieldSysPath.set( System.class.getClassLoader(), null );
            }
        }
    }

    /**
     * Setup the Game
     */
    @Override
    public void init(GameContainer gc) throws SlickException {
        // link the _instance
        _instance = this;
        
        // create a background
        Rectangle screenRect = new Rectangle(0,
                                             0,
                                             this.spaceWidth,
                                             this.spaceHeight);
        this.background = new Background(screenRect);
        
        // create a camera object
        this.camera = new Camera(this.spaceWidth,
                                 this.spaceHeight,
                                 800,
                                 600);
        
        // create the space boundaries
        this.createSpaceBoundaries();
        
        // create the worlds
        this.worlds = new ArrayList<World>();
        for (int x = 0; x < this.spaceWidth; x+=(this.spaceWidth/4)) {;
            World world = new World(new Vector2f(x + 150.0f, (float)(this.spaceHeight - (this.spaceHeight/1.8))));
            this.worlds.add(world);
        }
        
        // create the player
        this.mcHammer = new Player(new Vector2f(100.0f, 400.0f));
        
        // create the array for the hammers
        this.hammerList = new CopyOnWriteArrayList<Hammer>();
        
        // create an array list for the buildings
        this.buildings = new CopyOnWriteArrayList<Building>();
        
        // create the array list for the aliens
        this.aliens = new CopyOnWriteArrayList<Alien>();
    }
    
    /**
     * Puts the game in a stable, restartable state
     */
    public void restartGame() {
        this.hammerList.clear();
        this.aliens.clear();
        this.buildings.clear();
        
        this.propertyValue = 1000;
        this.alienReleaseCount = 0;
        this.alienReleaseTimer = 0.0f;
        this.elapsedGameTime = 0.0f;
        
        this.mcHammer.position = new Vector2f(80.0f, 80.0f);
        
        // start the game 
        this.gameState = this.GAME_RUNNING;
    }
    
    protected void createSpaceBoundaries() {
        int boundaryWidth = 20;
        Rectangle leftBoundary = new Rectangle(0 - boundaryWidth,
                                               0,
                                               boundaryWidth,
                                               this.spaceHeight);
        Rectangle rightBoundary = new Rectangle(this.spaceWidth,
                                                0,
                                                boundaryWidth,
                                                this.spaceHeight);
        Rectangle bottomBoundary = new Rectangle(0,
                                                 this.spaceHeight,
                                                 this.spaceWidth,
                                                 boundaryWidth);
        Rectangle topBoundary = new Rectangle(0,
                                              0 - boundaryWidth,
                                              this.spaceWidth,
                                              boundaryWidth);
        this.spaceBoundaries = new ArrayList<Rectangle>();
        this.spaceBoundaries.add(leftBoundary);
        this.spaceBoundaries.add(rightBoundary);
        this.spaceBoundaries.add(topBoundary);
        this.spaceBoundaries.add(bottomBoundary);
    }
    
    /**
     * Adds a hammer to the hammer array list
     * 
     * @param hammer the hammer to add to the array
     */
    public void addHammer(Hammer hammer) {
        this.hammerList.add(hammer);
    }
    
    /**
     * Finds the hammer in the hammer array list, and removes it
     * 
     * @param hammer the hammer to remove
     */
    public void removeHammer(Hammer hammer) {
        this.hammerList.remove(hammer);
    }
    
    /**
     * Remove alien from the aliens array
     * 
     * @param alien the alien to remove
     */
    public void removeAlien(Alien alien) {
        this.aliens.remove(alien);
    }
    
    /**
     * Adds a building to the building array 
     * 
     * @param building the building to add
     */
    public void addBuilding(Building building) {
        this.buildings.add(building);
    }
    
    /**
     * Removes a building from the building array
     * 
     * @param building the building to remove
     */
    public void removeBuilding(Building building) {
        this.buildings.remove(building);
    }
    
    /**
     * Renders the game by looping through all game objects and calling render.
     */
    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {        
        // clear the screen
        g.setBackground(Color.magenta);
        
        // move to the camera position
        Vector2f cameraPosition = this.camera.getPosition();
        g.translate(-1 * cameraPosition.x, -1 * cameraPosition.y);
        
        // render the background first
        this.background.render(gc, g);
        
        // render the worlds next
        for (World world : this.worlds) {
            world.render(gc, g);
        }
        
        // render any hammers
        for (Hammer hammer : this.hammerList) {
            hammer.render(gc, g);
        }
        
        // render any aliens
        for (Alien alien : this.aliens) {
            alien.render(gc, g);
        }
        
        // render any buildings
        for (Building building : this.buildings) {
            building.render(gc, g);
        }
        
        // render the player
        this.mcHammer.render(gc, g);
        
        // render the score
        g.setColor(Color.white);
//        g.drawString("Score: "+ this.propertyValue, 650.0f, 10.0f);
//        g.drawString("Time: " + this.elapsedGameTime, 650.0f, 30.0f);
        g.drawString("Property Value: "+ this.propertyValue, this.camera.position.x + 600.0f, this.camera.position.y + 10.0f);
        g.drawString("Time: " + Math.round(this.elapsedGameTime), this.camera.position.x + 600.0f, this.camera.position.y + 30.0f);
        
        if (this.gameState == this.GAME_MENU) {
            // render the menu
            Color oldColor = g.getColor();
            Vector2f menuOffset = new Vector2f(40.0f, 40.0f);
            
            g.setColor(new Color(1.0f, 0.0f, 1.0f, 0.5f));
            g.fillRect(menuOffset.x, menuOffset.y, 720.0f, 520.0f);
            
            g.setColor(Color.white);
            g.drawString("You are the scottish space property developer, McHammer.", 
                         menuOffset.x + 40.0f,
                         menuOffset.y + 40.0f);
            g.drawString("Your four tiny worlds would quickly lose value if you were to have some \ninvaders from space developing" +
            		    " on your land...", menuOffset.x + 40.0f, menuOffset.y + 80.0f);
            g.drawString("Don't let your property value reach zero!", menuOffset.x + 40.0f, menuOffset.y + 140.0f);
            g.drawString("CONTROLS:", menuOffset.x + 40.0f, menuOffset.y + 180.0f);
            g.drawString("LEFT/RIGHT arrows to rotate. UP/DOWN to jetpack.\n" +
            		"SPACEBAR to throw hammers.", menuOffset.x + 40.0f, menuOffset.y + 220.0f);
            g.drawString("P to PAUSE.", menuOffset.x + 40.0f, menuOffset.y + 260.0f);
            g.drawString("PRESS SPACEBAR TO START.", menuOffset.x + 40.0f, menuOffset.y + 320.0f);
            
            g.setColor(oldColor);
        } else if (this.gameState == this.GAME_PAUSED) {
            Color oldColor = g.getColor();
            
            Vector2f offset = this.camera.position;
            
            g.setColor(new Color(1.0f, 0.0f, 1.0f, 0.5f));
            g.fillRect(offset.x + 330.0f, offset.y + 250.0f, 80.0f, 55.0f);
            
            g.setColor(Color.white);
            g.drawString("PAUSED", offset.x + 300.0f + 40.0f, offset.y + 250.0f + 20.0f);
            
            g.setColor(oldColor);
        } else if (this.gameState == this.GAME_OVER) {
            Color oldColor = g.getColor();
            
            Vector2f offset = this.camera.position;
            
            g.setColor(new Color(1.0f, 0.0f, 1.0f, 0.5f));
            g.fillRect(offset.x + 230.0f, offset.y + 250.0f, 420.0f, 160.0f);
            
            g.setColor(Color.white);
            g.drawString("GAME OVER", offset.x + 200.0f + 40.0f, offset.y + 250.0f + 20.0f);
            g.drawString("Stop. Hammer time is over.", offset.x + 200.0f + 40.0f, offset.y + 250.0f + 40.0f);
            g.drawString("You survived for " + Math.round(this.elapsedGameTime) + " seconds. Well done!", offset.x + 200.0f + 40.0f, offset.y + 250.0f + 80.0f);
            g.drawString("Press R to Restart.", offset.x + 200.0f + 40.0f, offset.y + 250.0f + 120.0f);
            
            g.setColor(oldColor);
        }
    }
    
    /**
     * Updates the game by looping through all game objects that require
     * logic updates, and calls update on that object.
     */
    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        if (this.gameState == this.GAME_RUNNING) {
            // get the delta in seconds, we are actually sane.
            float deltaSeconds = delta/1000.0f;

            // increase the game time
            this.elapsedGameTime += deltaSeconds;
            // should we be generating aliens?
            this.alienReleaseTimer += deltaSeconds;
            if (this.alienReleaseTimer >= this.alienReleaseLimit/1000.0f) {
                // reset the time
                this.alienReleaseTimer = 0.0f;
                // lower the limit
                this.alienReleaseLimit -= 10.0f;

                // release some aliens
                this.alienReleaseCount += 0.5;
                for (int i = 0; i < this.alienReleaseCount; i++) {
                    if (this.aliens.size() < this.alienCountLimit) {
                        this.aliens.add(new Alien());
                    }
                }
            }

            // update the background
            this.background.update(gc, deltaSeconds);

            // update the player
            this.mcHammer.update(gc, deltaSeconds);

            // update the hammers
            for (Hammer hammer : this.hammerList) {
                hammer.update(gc, deltaSeconds);
            }

            // update the aliens
            for (Alien alien : this.aliens) {
                alien.update(gc, deltaSeconds);
            }

            // update the buildings
            for (Building building : this.buildings) {
                building.update(gc, deltaSeconds);
                
                // update the player score
                if (building.onPlanet) {
                    this.propertyValue -= building.damageDone;
                    building.damageDone = 0;
                }
                
            }

            // apply physics
            this.simulatePhysics(deltaSeconds);

            // run the collision detection
            this.detectCollisions();
            
            // update the camera
            this.camera.setPosition(new Vector2f(this.mcHammer.boundingRectangle().getCenter()));
            
            
            if (this.propertyValue < 0) {
                this.propertyValue = 0;
                this.gameState = this.GAME_OVER;
            }
            
        } else if (this.gameState == GAME_MENU) {
            
        } else if (this.gameState == GAME_OVER) {
            
        } 
    }
    

    /**
     * Runs the main physics simulation between interacting bodies
     */
    public void simulatePhysics(float deltaSeconds) {
        // gravitation force = m1*m2/r^2 where r = distance between objects        
        for (World world : this.worlds) {
            float distance, f, a;
            double theta, aX, aY, vX, vY;
            
            // calculate gravitation effects for the player
            // calculate force
            distance = this.mcHammer.position.distance(world.position);
            f = (world.mass * this.mcHammer.mass) / distance;
            // calculate acceleration
            a = f / this.mcHammer.mass;
            
            // calculate components of acceleration
            theta = Math.atan2(this.mcHammer.position.y - world.position.y,
                                      this.mcHammer.position.x - world.position.x);
            aX = a * Math.cos(theta);
            aY = a * Math.sin(theta);
            
            // calculate resulting velocity
            vX = this.mcHammer.velocity.x + (aX * deltaSeconds);
            vY = this.mcHammer.velocity.y - (aY * deltaSeconds);
            
            this.mcHammer.setVelocity((float)vX, (float)vY);
            
            // calculate gravitational effects for the hammers
            for (Hammer hammer : this.hammerList) {
                // calculate force
                distance = hammer.position.distance(world.position);
                f = (world.mass * hammer.mass) / distance;
                // calculate acceleration
                a = f / hammer.mass;
                
                // calculate components of acceleration
                theta = Math.atan2(hammer.position.y - world.position.y,
                                   hammer.position.x - world.position.x);
                aX = a * Math.cos(theta);
                aY = a * Math.sin(theta);
                
                // calculate velocity
                vX = hammer.velocity.x + (aX * deltaSeconds);
                vY = hammer.velocity.y - (aY * deltaSeconds);
                
                hammer.setVelocity((float)vX, (float)vY);
            }
        }
    }
    
    /**
     * Detects collisions between collision objects in the world, and notifies
     * them that a collision occurred.
     */
    public void detectCollisions() {
        for (Rectangle boundaryRect : this.spaceBoundaries) {
            // first, make sure we are not moving out of our "space"
            if (boundaryRect.intersects(this.mcHammer.collisionShape())) {
                this.mcHammer.onCollision(boundaryRect);
            }
            
            // collision detection for hammers
            for (Hammer hammer : this.hammerList) {
                // detect collisions with hammers and boundaries
                if (boundaryRect.intersects(hammer.collisionRectangle())) {
                    hammer.onCollision(boundaryRect);
                }
                
                // collision between hammers and buildings
                for (Building building : this.buildings) {
                    if (hammer.collisionRectangle().intersects(building.collisionRectangle())) {
                        hammer.onCollision(building);
                        building.onCollision(hammer);
                    }
                }
            }
        }
                
        // world collisions
        for (World world : this.worlds) {
            // detect collisions between player and the worlds
            if (world.collisionCircle().intersects(this.mcHammer.collisionShape())) {
                this.mcHammer.onCollision(world);
            }
            
            // detect collisions between buildings and the worlds
            for (Building building : this.buildings) {
                if (world.collisionCircle().intersects(building.collisionRectangle())) {
                    building.onCollision(world);
                }
            }
        }
        
    }
    
    public void keyPressed(int key, char c) {
        switch (key) {
            case Input.KEY_RIGHT:
                this.mcHammer.setRotatingRight();
                break;
            case Input.KEY_LEFT:
                this.mcHammer.setRotatingLeft();
                break;
            case Input.KEY_DOWN:
                this.mcHammer.setJetpackReverse();
                break;
            case Input.KEY_UP:
                this.mcHammer.setJetpackOn();
                break;
            case Input.KEY_SPACE:
                if (this.gameState == GAME_MENU) {
                    this.gameState = GAME_RUNNING;
                } else {
                    this.mcHammer.throwHammer();
                }
                break;
            case Input.KEY_P:
                if (this.gameState == this.GAME_RUNNING) {
                    this.gameState = this.GAME_PAUSED;
                } else if (this.gameState == this.GAME_PAUSED) {
                    this.gameState = this.GAME_RUNNING;
                }
                break;
            case Input.KEY_R:
                if (this.gameState == this.GAME_OVER) {
                    this.restartGame();
                }
                break;
        }       
    }

    public void keyReleased(int key, char c) {
        switch (key) {
            case Input.KEY_RIGHT:
                this.mcHammer.setNotRotating();
                break;
            case Input.KEY_UP:
                this.mcHammer.setJetpackOff();
                break;
            case Input.KEY_LEFT:
                this.mcHammer.setNotRotating();
                break;
            case Input.KEY_DOWN:
                this.mcHammer.setJetpackOff();
                break;
        }   
    }

}
