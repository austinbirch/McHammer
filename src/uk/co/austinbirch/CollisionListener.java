package uk.co.austinbirch;

import org.newdawn.slick.geom.Rectangle;

import uk.co.austinbirch.Entity;

public interface CollisionListener {

    void onCollision(Rectangle collisionRect);
    void onCollision(Entity entity);

}
