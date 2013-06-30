package uk.co.austinbirch.physics;

import org.newdawn.slick.geom.Ellipse;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.util.Log;

public class ABPhysicsHelper {

    /**
     * Calculates the Minimum Translation Vector(MTV) required to resolve a
     * collision between two Shapes. The MTV returned is for shapeA
     * 
     * @param shapeA the Shape that the MTV is calculated for
     * @param shapeB the Shape that shapeA is intersecting with
     * @return the Vector2f MTV
     */
    public static Vector2f calculateMTV(Shape shapeA, Shape shapeB) {
        if (shapeB instanceof Rectangle) {
            return ABPhysicsHelper.calculateMTV((Rectangle)shapeA, (Rectangle)shapeB);
        } else if (shapeB instanceof Ellipse) {
            return ABPhysicsHelper.calculateMTV((Rectangle)shapeA, (Ellipse)shapeB);
        }
        
        // TODO: Error handling
        // we don't know what these Shapes are, so we should throw an error
        // here
        Log.error("Unknown shapes passed to calculateMTV");
        return new Vector2f(0.0f, 0.0f);
    }
    
    /**
     * Calculates the Minimum Translation Vector(MTV) to resolve a collision 
     * between two Rectangle objects. The MTV returned is for rectA
     * 
     * @param rectA the Rectangle that the MTV is calculated for
     * @param rectB the Rectangle that rectA is intersecting with
     * @return the Vector2f MTV
     */
    protected static Vector2f calculateMTV(Rectangle rectA, Rectangle rectB) {
        // AABB Separating Axis Theorem (Minimum Translation Vector
        // for collision)

        Vector2f centerA = new Vector2f(rectA.getCenterX(), rectA.getCenterY());
        Vector2f centerB = new Vector2f(rectB.getCenterX(), rectB.getCenterY());

        float halfWidthA = rectA.getWidth() / 2;
        float halfWidthB = rectB.getWidth() / 2;
        float halfHeightA = rectA.getHeight() / 2;
        float halfHeightB = rectB.getHeight() / 2;
        float diffX = centerB.x - centerA.x;
        float diffY = centerB.y - centerA.y;

        if (Math.abs(diffX) < Math.abs(halfWidthA + halfWidthB)) {
            // no gap on the x-axis
            float oX = (halfWidthA + halfWidthB) - Math.abs(diffX);

            if (Math.abs(diffY) < Math.abs(halfHeightA + halfHeightB)) {
                // no gap on the y-axis either,
                // so there is a collision
                float oY = (halfHeightA + halfHeightB) - Math.abs(diffY);

                if (oY < oX) {
                    // resolve using y-axis
                    if (diffY < 0) {
                        return new Vector2f(0.0f, oY);
                    } else {
                        return new Vector2f(0.0f, (-1.0f * oY));
                    }
                } else {
                    // resolve using x-axis
                    if (diffX < 0) {
                        return new Vector2f(oX, 0.0f);
                    } else {
                        return new Vector2f((-1.0f * oX), 0.0f);
                    }
                }
            }

        }
        // no collision, so no MTV
        return new Vector2f(0.0f, 0.0f);
    }
    
    /**
     * Calculates the Minimum Translation Vector(MTV) to resolve a collision 
     * between a Rectangle object, and an Ellipse object. The MTV returned is 
     * for rectA.
     * 
     * @param rectA the Rectangle that the MTV is translated for
     * @param ellipseB the Ellipse that rectA is intersecting with
     * @return the Vector2f MTV
     */
    protected static Vector2f calculateMTV(Rectangle rectA, Ellipse ellipseB) {
        float[] points = rectA.getPoints();

        // check to see which points collide 
        for (int i = 0; i < rectA.getPointCount() * 2; i+=2) {
            Vector2f point = new Vector2f(points[i], points[i+1]);
            if (ellipseB.contains(point.x, point.y)) {
                
                Vector2f center = new Vector2f(ellipseB.getCenterX(),
                                               ellipseB.getCenterY());
                // get the angle between center and our intersection point
                double theta = Math.atan2(center.y - point.y,
                                         center.x - point.x);
                
                // calculate the position on the circle in which we should have
                // stopped our intersection
                double xLength = Math.cos(theta) * ellipseB.getRadius1();
                double yLength = Math.sin(theta) * ellipseB.getRadius1();
                Vector2f dest = new Vector2f(center.x - (float)xLength, 
                                            center.y - (float)yLength);
                Vector2f mtv = dest.sub(point);
                return mtv;
            }
        }
        
        // no collision, so no MTV
        return new Vector2f(0.0f, 0.0f);
    }

}
