package com.sandbox.topdown.framework.utils;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Maarten
 */
public class MathUtilTest {

    private static final float EPSILON = 0.000001F;

    @Test
    public void testDirectionToPoint() {
        assertEquals(0, MathUtil.directionToPoint(0, 0, 0, 0), EPSILON);
        assertEquals(90, MathUtil.directionToPoint(0, 0, 0, 100), EPSILON);
        assertEquals(-90, MathUtil.directionToPoint(0, 100, 0, 0), EPSILON);
        assertEquals(180, MathUtil.directionToPoint(100, 0, 0, 0), EPSILON);
        assertEquals(0, MathUtil.directionToPoint(0, 0, 100, 0), EPSILON);
    }

    @Test
    public void testDistanceToPoint() {
        assertEquals(0, MathUtil.distanceToPoint(0, 0, 0, 0), EPSILON);
        assertEquals(1, MathUtil.distanceToPoint(0, 0, 1, 0), EPSILON);
        assertEquals(1, MathUtil.distanceToPoint(0, 0, 0, 1), EPSILON);
        assertEquals(1, MathUtil.distanceToPoint(1, 0, 0, 0), EPSILON);
        assertEquals(1, MathUtil.distanceToPoint(0, 1, 0, 0), EPSILON);
        
        //pythagorean
        assertEquals(1.414213, MathUtil.distanceToPoint(0, 0, 1, 1), EPSILON);
        assertEquals(141.421356, MathUtil.distanceToPoint(0, 0, 100, 100), EPSILON);
    }

}
