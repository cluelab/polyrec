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

/**
 * Represents a vector with intensity and angle.
 * 
 * @author Vittorio
 *
 */
public class Vector {
    private double intensity;
    private double angle;

    /**
     * @param intensity
     *            The intensity
     * @param angle
     *            The angle
     */
    public Vector(double intensity, double angle) {
        this.intensity = intensity;
        this.angle = angle;
    }

    /**
     * @param rotation
     *            The rotation
     * @return The horizontal component after rotation
     */
    public double getHorz(double rotation) {
        return intensity * Math.cos(angle + rotation);
    }

    /**
     * @param rotation
     *            The rotation
     * @return The vertical component after rotation
     */
    public double getVert(double rotation) {
        return intensity * Math.sin(angle + rotation);
    }

    /**
     * @param other
     *            The other vector
     * @param rotation1
     *            Rotation of this vector
     * @param rotation2
     *            Rotation of the other vector
     * @return Difference with another vector
     */
    public double difference(Vector other, double rotation1, double rotation2) {
        final double x = getHorz(rotation1) - other.getHorz(rotation2);
        final double y = getVert(rotation1) - other.getVert(rotation2);
        final double difference = Math.sqrt(x * x + y * y) / 2;
        return difference;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String sp = " ";
        return intensity + sp + angle + sp;
    }

}
