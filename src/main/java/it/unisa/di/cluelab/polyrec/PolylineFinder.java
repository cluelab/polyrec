/*
PolyRec Project
Copyright (c) 2015-2017, Vittorio Fuccella - CLUE Lab - http://cluelab.di.unisa.it
All rights reserved. Includes a reference implementation of the following:

* Vittorio Fuccella, Gennaro Costagliola. "Unistroke Gesture Recognition
  Through Polyline Approximation and Alignment". In Proceedings of the 33rd
  annual ACM conference on Human factors in computing systems (CHI '15).
  April 18-23, 2015, Seoul, Republic of Korea.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the PolyRec Project nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package it.unisa.di.cluelab.polyrec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Abstract superclass for polyline approximation.
 * 
 * @author Vittorio
 *
 */
public abstract class PolylineFinder {
    protected Gesture gesture;
    protected TreeSet<Integer> vertexes;
    protected double slopeTreshold;
    // protected Polyline poly;

    /**
     * @param gesture
     *            The gesture
     * @param params
     *            List of parameters for approximation
     */
    public PolylineFinder(Gesture gesture, Double[] params) {
        super();
        this.gesture = gesture;
        vertexes = new TreeSet<Integer>();
    }

    /**
     * @return The polyline found
     */
    public abstract Polyline find();

    /**
     * @return The polyline
     */
    public Polyline getPoly() {
        return new Polyline(gesture, new ArrayList<Integer>(vertexes));
    }

    /**
     * @return The gesture
     */
    public Gesture getGesture() {
        return gesture;
    }

    /**
     * @param n
     *            The index of the vertex
     * @return The point corresponding to the n-th vertex
     */
    public TPoint getVertex(int n) {
        int seq = 0;
        for (Integer v : vertexes) {
            if (seq == n) {
                return gesture.points.get(v);
            }
            seq++;
        }
        return null;
    }

    /**
     * @param n
     *            The index of the vertex
     * @return The index of the point in the gesture corresponding to the n-th vertex
     */
    public Integer getVertexIndex(int n) {
        int seq = 0;
        for (Integer v : vertexes) {
            if (seq == n) {
                return v;
            }
            seq++;
        }
        return null;
    }

    /**
     * @return The index of the vertex with small curvature variation
     */
    private int smallestAngle() {
        if (vertexes.size() <= 2) {
            return -1;
        }

        int min = 0;
        double smallestMeasure = 360;

        final Iterator<Integer> iter = vertexes.iterator();
        int first = iter.next();
        int median = iter.next();
        while (iter.hasNext()) {
            final int last = iter.next();
            final double angle = Math.abs(180 - Polyline.angle(gesture.points.get(first), gesture.points.get(last),
                    gesture.points.get(median), true));
            if (angle < smallestMeasure) {
                min = median;
                smallestMeasure = angle;
            }
            first = median;
            median = last;
        }
        return smallestMeasure > slopeTreshold ? -1 : min;
    }

    /**
     * At each iteration, the point between the segments having the smallest direction change is removed.
     */
    public void fusion2() {

        int smallest = smallestAngle();
        while (smallest != -1) {
            vertexes.remove(smallest);
            smallest = smallestAngle();
        }
    }
}
