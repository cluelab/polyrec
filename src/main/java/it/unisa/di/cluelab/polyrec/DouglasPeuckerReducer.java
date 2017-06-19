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

/**
 * Class implementing Douglas-Peucker Reducer.
 * 
 * @author Vittorio
 *
 */
public class DouglasPeuckerReducer extends PolylineFinder {

    // private static int calcCount;
    private double tolerance;

    /**
     * @param gesture
     *            The gesture
     * @param params
     *            Parameters for reduction: slopeTreshold and tolerance
     */
    public DouglasPeuckerReducer(Gesture gesture, Double[] params) {
        super(gesture, params);
        this.slopeTreshold = params[0];
        this.tolerance = gesture.getDiagonal() / params[1];
    }

    /**
     * Reduce the number of points in a shape using the Douglas-Peucker algorithm.
     * 
     * @param shape
     *            The shape to reduce
     * @param tol
     *            The tolerance to decide whether or not to keep a point, in the coordinate system of the points
     *            (micro-degrees here)
     */
    public void reduceWithTolerance(ArrayList<TPoint> shape, double tol) {
        final int n = shape.size();
        // if a shape has 2 or less points it cannot be reduced
        if (tol <= 0 || n < 3) {
            return;
        }

        final boolean[] marked = new boolean[n];
        // vertex indexes to keep will be marked as "true"
        for (int i = 1; i < n - 1; i++) {
            marked[i] = false;
        }
        // automatically add the first and last point to the returned shape
        marked[0] = true;
        marked[n - 1] = true;

        // the first and last points in the original shape are
        // used as the entry point to the algorithm.
        douglasPeuckerReduction(shape, marked, tol, 0, n - 1);
        // shape: original shape
        // marked: reduced shape
        // tol: tolerance
        // 0: index of first point
        // n - 1: index of last point

        // all done, return the reduced shape
        for (int i = 0; i < n; i++) {
            if (marked[i]) {
                vertexes.add(i);
            }
        }
    }

    /**
     * Reduce the points in shape between the specified first and last index. Mark the points to keep in marked[]
     * 
     * @param shape
     *            The original shape
     * @param marked
     *            The points to keep (marked as true)
     * @param tol
     *            The tolerance to determine if a point is kept
     * @param firstIdx
     *            The index in original shape's point of the starting point for this line segment
     * @param lastIdx
     *            The index in original shape's point of the ending point for this line segment
     */
    private static void douglasPeuckerReduction(ArrayList<TPoint> shape, boolean[] marked, double tol, int firstIdx,
            int lastIdx) {
        if (lastIdx <= firstIdx + 1) {
            // overlapping indexes, just return
            return;
        }

        // loop over the points between the first and last points
        // and find the point that is the farthest away

        double maxDistance = 0.0;
        int indexFarthest = 0;

        final TPoint firstPoint = shape.get(firstIdx);
        final TPoint lastPoint = shape.get(lastIdx);

        for (int idx = firstIdx + 1; idx < lastIdx; idx++) {
            final TPoint point = shape.get(idx);

            final double distance = orthogonalDistance(point, firstPoint, lastPoint);

            // keep the point with the greatest distance
            if (distance > maxDistance) {
                maxDistance = distance;
                indexFarthest = idx;
            }
        }

        if (maxDistance > tol) {
            // The farthest point is outside the tolerance: it is marked and the algorithm continues.
            marked[indexFarthest] = true;

            // reduce the shape between the starting point to newly found point
            douglasPeuckerReduction(shape, marked, tol, firstIdx, indexFarthest);

            // reduce the shape between the newly found point and the finishing point
            douglasPeuckerReduction(shape, marked, tol, indexFarthest, lastIdx);
        }
        // else: the farthest point is within the tolerance, the whole segment is discarded.
    }

    /**
     * Calculate the orthogonal distance from the line joining the lineStart and lineEnd points to point.
     * 
     * @param point
     *            The point the distance is being calculated for
     * @param lineStart
     *            The point that starts the line
     * @param lineEnd
     *            The point that ends the line
     * @return The distance in points coordinate system
     */
    public static double orthogonalDistance(TPoint point, TPoint lineStart, TPoint lineEnd) {
        if (lineStart.x == lineEnd.x && lineStart.y == lineEnd.y) {
            return lineStart.distance(point);
        }

        final double area = Math
                .abs((1.0 * lineStart.y * lineEnd.x + 1.0 * lineEnd.y * point.x + 1.0 * point.y * lineStart.x
                        - 1.0 * lineEnd.y * lineStart.x - 1.0 * point.y * lineEnd.x - 1.0 * lineStart.y * point.x)
                        / 2.0);

        final double bottom = Math.hypot(lineStart.y - lineEnd.y, lineStart.x - lineEnd.x);

        return area / bottom * 2.0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.unisa.di.cluelab.polyrec.PolylineFinder#find()
     */
    @Override
    public Polyline find() {
        // calcCount++;
        reduceWithTolerance(gesture.points, tolerance);
        fusion2();
        return getPoly();
    }

}
