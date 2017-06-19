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

import java.util.List;
import java.util.ListIterator;

/**
 * Class representing a polyline.
 * 
 * @author Vittorio
 *
 */
public class Polyline {
    private Gesture gesture;
    private List<Integer> indexes;
    private List<Double> lengths;

    /**
     * @param gesture
     *            The gesture
     * @param indexes
     *            The indexes of the polyline points
     */
    public Polyline(Gesture gesture, List<Integer> indexes) {
        this.gesture = gesture;
        this.indexes = indexes;
        calculateLengths();
    }

    private void calculateLengths() {
        Double length = 0.0d;
        lengths = new ArrayList<Double>();
        lengths.add(length);
        TPoint temTPoint = null;
        final ListIterator<Integer> iterator = indexes.listIterator();

        while (iterator.hasNext()) {
            final TPoint point = gesture.points.get(iterator.next());
            if (temTPoint != null) {
                length += temTPoint.distance(point);
                // TPoint.dist( temTPoint, point );
                lengths.add(length);
            }
            temTPoint = point;
        }
    }

    /**
     * @return List of the points of the polyline
     */
    public List<TPoint> getPoints() {
        final List<TPoint> lp = new ArrayList<TPoint>();
        for (int i = 0; i < getNumVertexes(); i++) {
            lp.add(gesture.points.get(indexes.get(i)));
        }
        return lp;
    }

    /**
     * @param index
     *            The index of the point in the polyline
     * @return The index of the point in the gesture
     */
    public int getPoint(int index) {
        return indexes.get(index);
    }

    /**
     * @param index
     *            The index of the point in the polyline
     * @return The point
     */
    public TPoint getTPoint(int index) {
        return gesture.points.get(getPoint(index));
    }

    /**
     * @return The number of vertexes in the polyline
     */
    public int getNumVertexes() {
        return indexes.size();
    }

    /**
     * @return The number of segments in the polyline
     */
    public int getNumLines() {
        return indexes.size() - 1;
    }

    /**
     * @param first
     *            The index of the first point
     * @param last
     *            The index of the last point
     * @param median
     *            The index of the median point
     * @return The ratio of the length from first to median by the length from first to last
     */
    public double getLengthProportion(int first, int last, int median) {
        final double all = gesture.getLength(indexes.get(first), indexes.get(last));
        final double toMedian = gesture.getLength(indexes.get(first), indexes.get(median));
        return toMedian / all;
    }

    /**
     * @param lineNum
     *            The index of the segment (zero-based)
     * @return The intensity of the vector associated to the segment
     */
    public double getLineIntensity(int lineNum) {
        final double length = lengths.get(lineNum + 1) - lengths.get(lineNum);
        return length / (lengths.get(getNumLines()) + gesture.getEndpointsDistance());
    }

    /**
     * @return The intensity of the vector associated to the segment connecting the two endpoints
     */
    public Double getInvisibleLineIntensity() {
        return gesture.getEndpointsDistance() / (lengths.get(getNumLines()) + gesture.getEndpointsDistance());
    }

    /**
     * @param first
     *            First endpoint of the segment
     * @param last
     *            Last endpoint of the segment
     * @return Slope of a segment with respect to horizontal axis
     */
    public static double getLineAngle(TPoint first, TPoint last) {
        final double xDiff = last.x - first.x;
        final double yDiff = first.y - last.y;
        final double angle = Math.atan2(yDiff, xDiff);
        return angle >= 0 ? angle : (2 * Math.PI + angle);
        // return Math.atan2(yDiff, xDiff);

    }

    /**
     * @return The slope of the segment connecting the two endpoints
     */
    public Double getInvisibleLineSlope() {
        final TPoint first = gesture.points.get(0);
        final TPoint last = gesture.points.get(gesture.points.size() - 1);
        return getLineAngle(first, last);
    }

    /**
     * @param lineNum
     *            The number of the segment
     * @return The slope of the n-th segment
     */
    public double getLineSlope(int lineNum) {
        final TPoint first = getTPoint(lineNum);
        final TPoint last = getTPoint(lineNum + 1);

        return getLineAngle(first, last);
    }

    /**
     * @param p0
     *            First point
     * @param p1
     *            Last point
     * @param c
     *            Central point
     * @param deg
     *            Value in degrees or in radians
     * @return The magnitude of the angle identified by three points
     */
    public static double angle(TPoint p0, TPoint p1, TPoint c, boolean deg) {
        // p0->c (b)
        final double p0c = Math.sqrt(Math.pow(c.x - p0.x, 2) + Math.pow(c.y - p0.y, 2));
        // p1->c (a)
        final double p1c = Math.sqrt(Math.pow(c.x - p1.x, 2) + Math.pow(c.y - p1.y, 2));
        // p0->p1 (c)
        final double p0p1 = Math.sqrt(Math.pow(p1.x - p0.x, 2) + Math.pow(p1.y - p0.y, 2));
        final double angle = Math.acos((p1c * p1c + p0c * p0c - p0p1 * p0p1) / (2 * p1c * p0c));
        if (deg) {
            return Math.toDegrees(angle);
        } else {
            return angle;
        }
    }

    private double mod(double a, double n) {
        return a - Math.floor(a / n) * n;
    }

    /**
     * @param index
     *            The vertex index
     * @return The slope change at vertex index in radians
     */
    public double getSlopeChange(int index) {
        if (index <= 0 || index >= indexes.size() - 1) {
            return .0;
        }
        final double previous = getLineSlope(index - 1);
        final double next = getLineSlope(index);

        final double diff = previous - next;
        return mod(diff + Math.PI, 2 * Math.PI) - Math.PI;
        // return (diff + Math.PI) % (2*Math.PI) - Math.PI;
        // return diff;

        // double diff = Math.abs(previous-next) % (2*Math.PI);
        // return diff > Math.PI ? 2*Math.PI - diff : diff;
    }

    /**
     * @param index
     *            The vertex index
     * @return The length of the polyline at the given vertex
     */
    public double getLengthAtAngle(int index) {
        return gesture.getLength(indexes.get(index)) / gesture.getLength();
    }

    /**
     * @return The polyline under the form of a list of vectors
     */
    public List<Vector> getVectors() {
        final ArrayList<Vector> vectors = new ArrayList<Vector>();
        for (int i = 0; i < getNumLines(); i++) {
            vectors.add(new Vector(getLineIntensity(i), getLineSlope(i)));
        }
        vectors.add(new Vector(getInvisibleLineIntensity(), getInvisibleLineSlope()));
        return vectors;
    }

    /**
     * @return The gesture
     */
    public Gesture getGesture() {
        return gesture;
    }

    /**
     * @return The points in the gesture samp0led for the polyline
     */
    public List<Integer> getIndexes() {
        return indexes;
    }

}
