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

import it.unisa.di.cluelab.polyrec.geom.Point2D;

import java.util.Locale;

/**
 * Class representing a sampled point.
 * 
 * @author Vittorio
 *
 */
public class TPoint extends Point2D.Double {
    private static final long serialVersionUID = 39657487229097308L;
    // CHECKSTYLE:OFF
    public long time;
    // CHECKSTYLE:ON

    /**
     * @param x
     *            The x
     * @param y
     *            The y
     * @param time
     *            The timestamp
     */
    public TPoint(double x, double y, long time) {
        super(x, y);
        this.time = time;
    }

    /**
     * @param point
     *            Constructor for cloning an existing point
     */
    public TPoint(TPoint point) {
        super(point.x, point.y);
        this.time = point.time;
    }

    /**
     * @return The timestamp
     */
    public long getTime() {
        return time;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + (int) (time ^ (time >>> 32));
    }

    @Override
    public boolean equals(Object oth) {
        if (oth instanceof TPoint) {
            return super.equals(oth) && time == ((TPoint) oth).time;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "(%.1f,%.1f,%d)", x, y, time);
    }

}
