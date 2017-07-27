package com.sandbox.topdown.framework.utils;

/**
 *
 * @author Maarten
 */
public class MathUtil {

    public static float translateX(float len, float dir) {
        return (float) (len * Math.cos(-dir / 180D * Math.PI));
    }

    public static float translateY(float len, float dir) {
        return (float) (len * -Math.sin(-dir / 180D * Math.PI));
    }

    public static float directionToPoint(float srcX, float srcY, float dstX, float dstY) {
        return (float) (Math.atan2(dstY - srcY, dstX - srcX) * 180.0d / Math.PI);
    }

    public static float radiansToDegrees(float radians) {
        return (float) (radians * 180 / Math.PI);
    }

    public static float degreesToRadians(float degrees) {
        return (float) (degrees * Math.PI / 180);
    }

    public static float distanceToPoint(float srcX, float srcY, float dstX, float dstY) {
        return (float) Math.hypot(srcX - dstX, srcY - dstY);
    }
}
