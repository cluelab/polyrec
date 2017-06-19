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

import it.unisa.di.cluelab.polyrec.geom.Point;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Class implementing Needleman-Wunsch algorithm.
 * 
 * @author Vittorio
 *
 */
public class NeedlemanWunsch {
    private static final double GAP_COST = 0.4;
    private static final double BALANCE = 0.6;
    private Polyline mSeqA;
    private Polyline mSeqB;
    private double[][] mD;
    private double mScore;
    private int insertions;
    private int deletions;
    private int matches;
    private String mAlignmentSeqA = "";
    private String mAlignmentSeqB = "";
    private ArrayList<Point> alignment;

    /**
     * @param mSeqA
     *            The first polyline
     * @param mSeqB
     *            The second polyline
     */
    public NeedlemanWunsch(Polyline mSeqA, Polyline mSeqB) {
        super();
        this.mSeqA = mSeqA;
        this.mSeqB = mSeqB;

        init();
        process();
        align();
    }

    private void init() {
        final int lengthA = mSeqA.getNumLines();
        final int lengthB = mSeqB.getNumLines();

        // System.out.println("mSeqA");
        // for(int i=0; i<lengthA; i++)
        // System.out.println(mSeqA.getLineIntensity(i)+" "+mSeqA.getSlopeChange(i));
        //
        // System.out.println("mSeqB");
        // for(int i=0; i<lengthB; i++)
        // System.out.println(mSeqB.getLineIntensity(i)+" "+mSeqB.getSlopeChange(i));

        mD = new double[lengthA + 1][lengthB + 1];
        for (int i = 0; i <= lengthA; i++) {
            for (int j = 0; j <= lengthB; j++) {
                if (i == 0) {
                    mD[i][j] = j * GAP_COST;
                    // -j;
                } else if (j == 0) {
                    mD[i][j] = i * GAP_COST;
                    // -i;
                } else {
                    mD[i][j] = 0;
                }
            }
        }
    }

    private void process() {
        for (int i = 1; i <= mSeqA.getNumLines(); i++) {
            for (int j = 1; j <= mSeqB.getNumLines(); j++) {
                final double scoreDiag = mD[i - 1][j - 1] + similarity(i, j);
                final double scoreLeft = mD[i][j - 1] + GAP_COST;
                final double scoreUp = mD[i - 1][j] + GAP_COST;
                mD[i][j] = Math.max(Math.max(scoreDiag, scoreLeft), scoreUp);
            }
        }
    }

    /**
     * @return An array of matched pairs. Each element of a pair is the index of a point in a polyline
     */
    public ArrayList<Point> getMatchedPoints() {
        final ArrayList<Point> matches = new ArrayList<Point>();
        for (Point p : getAlignment()) {
            if (p.x != -1 && p.y != -1) {
                matches.add(p);
            }
        }
        return matches;
    }

    /**
     * @return The alignment
     */
    public ArrayList<Point> getAlignment() {
        return alignment;
    }

    /* TODO */ @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    private void align() {

        alignment = new ArrayList<Point>();

        int i = mSeqA.getNumLines();
        int j = mSeqB.getNumLines();
        mScore = mD[i][j] / (i + j);
        alignment.add(0, new Point(i, j));
        while (i > 0 && j > 0) {
            Point p = null;
            if (mD[i][j] == mD[i - 1][j - 1] + similarity(i, j)) {
                p = new Point(i - 1, j - 1);
                alignment.add(0, p);
                // System.out.println("added "+p);
                matches++;
                i--;
                j--;
                continue;
            } else if (mD[i][j] == mD[i][j - 1] + GAP_COST) {
                p = new Point(-1, j - 1);
                alignment.add(0, p);
                // System.out.println("added "+p);
                insertions++;
                j--;
                continue;
            } else {
                p = new Point(i - 1, -1);
                alignment.add(0, p);
                // System.out.println("added "+p);
                deletions++;
                i--;
                continue;
            }
        }
        // printScoreAndAlignments();

    }

    /**
     * @return The number of insertions in the first polyline
     */
    public int getInsertions() {
        return insertions;
    }

    /**
     * @return The number of insertions in the second polyline
     */
    public int getDeletions() {
        return deletions;
    }

    /**
     * @return The number of matches between the two polylines
     */
    public int getMatches() {
        return matches;
    }

    private static double angleDiff(double angA, double angB) {
        double diff = Math.abs(angA - angB);
        if (diff > Math.PI) {
            diff = 2 * Math.PI - diff;
        }
        return diff / Math.PI;
        // return Math.abs(angA-angB) / Math.PI;
    }

    public static double weight(double length1, double angle1, double length2, double angle2) {
        return 1D - (BALANCE * Math.abs(length1 - length2) + (1 - BALANCE) * angleDiff(angle1, angle2));
    }

    private double similarity(int i, int j) {
        // TODO: le funzioni vengono chiamate con parametro 0 (i-1=0, j-1=0). Probabilmente ha senso per
        // getLengthAtAngle. Per getSlopeChange ha poco senso, ma probabilmente � ininfluente
        final double weight = 1D - (BALANCE * Math.abs(mSeqA.getLengthAtAngle(i - 1) - mSeqB.getLengthAtAngle(j - 1))
                + (1 - BALANCE) * angleDiff(mSeqA.getSlopeChange(i - 1), mSeqB.getSlopeChange(j - 1)));
        return weight;
    }

    /**
     * Prints the alignment matrix.
     */
    public void printMatrix() {
        final DecimalFormat df = new DecimalFormat("#.# ");
        System.out.println("D =");
        for (int i = 0; i < mSeqA.getNumLines() + 1; i++) {
            for (int j = 0; j < mSeqB.getNumLines() + 1; j++) {
                System.out.print(df.format(mD[i][j]));
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * @return The alignment score
     */
    public double getScore() {
        return mScore;
    }

    /**
     * Prints the alignment and its score.
     */
    void printScoreAndAlignments() {
        System.out.println("Score: " + mScore);
        System.out.println("Sequence A: " + mAlignmentSeqA);
        System.out.println("Sequence B: " + mAlignmentSeqB);
        System.out.println();
    }

}
