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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * The main recognizer class.
 * 
 * @author Vittorio
 * 
 */
public class PolyRecognizerGSS extends Recognizer {
    private static final Double[] DPR_PARAMS = new Double[] {26d, 22d};
    private static final boolean GSS = true;
    private static final Integer ANGLE_INVARIANT = 45;
    private static final Integer ANGLE_SENSITIVE = 25;
    private static final Integer ANGLE_STEP = 2;
    private static final boolean VERBOSE = false;
    protected Integer angle;
    protected Integer angleStep;
    protected final Double phi;
    // rInvariant is false by default
    private boolean rInvariant;

    /**
     * @param rInvariant
     *            Rotation invariant (=true) or sensitive (=false)
     */
    public PolyRecognizerGSS() {
        this(ANGLE_SENSITIVE, ANGLE_STEP);
    }

    /**
     * @param angle
     *            Angle for Golden Section Search
     * @param angleStep
     *            Angle step for Golden Section Search
     * @param rInvariant
     *            Rotation invariant (=true) or sensitive (=false)
     */
    public PolyRecognizerGSS(Integer angle, Integer angleStep) {
        this.angle = angle;
        this.setRotationAngle(angleStep);
        this.phi = 0.5f * (-1.0f + Math.sqrt(5.0f));
        templates = new TreeMap<String, ArrayList<Polyline>>();
        if (GSS) {
            method = "PolyRec-GSS";
        } else {
            method = "PolyRec";
        }
    }

    public static Double[] getDprParams() {
        return Arrays.copyOf(DPR_PARAMS, DPR_PARAMS.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.unisa.di.cluelab.polyrec.Recognizer#addTemplate(java.lang.String, it.unisa.di.cluelab.polyrec.Gesture)
     */
    @Override
    public int addTemplate(String name, Gesture gesture) {
        if (!templates.containsKey(name)) {
            templates.put(name, new ArrayList<Polyline>());
        }
        final ArrayList<Polyline> templateClass = templates.get(name);
        final PolylineFinder tpf = new DouglasPeuckerReducer(gesture, PolyRecognizerGSS.DPR_PARAMS);
        templateClass.add(tpf.find());
        return templateClass.size();
    }

    // CHECKSTYLE:OFF
    /*
     * (non-Javadoc)
     * 
     * @see it.unisa.di.cluelab.polyrec.Recognizer#recognize(it.unisa.di.cluelab.polyrec.Gesture)
     */
    @Override
    public synchronized Result recognize(Gesture gesture) {
        // this.rInvariant = _gesture.isRotInv(); if (this.rInvariant)
        // this.angle = ANGLE_INVARIANT; else this.angle = ANGLE_SENSITIVE;

        final PolylineFinder pf = new DouglasPeuckerReducer(gesture, DPR_PARAMS);
        // polyline del gesto da riconoscere
        final Polyline u = pf.find();

        Double a = Double.POSITIVE_INFINITY;
        String templateName = null;
        Polyline t = null;

        final TreeMap<String, double[]> ranking = new TreeMap<String, double[]>();

        for (Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
            final ArrayList<Polyline> tempTemplates = e.getValue();
            double classdistance = Double.POSITIVE_INFINITY;
            for (int i = 0; i < tempTemplates.size(); i++) {
                Double distance = 2.0;
                t = tempTemplates.get(i);

                if (tempTemplates.get(i).getGesture().getPointers() == gesture.getPointers()) {
                    final PolylineAligner aligner = new PolylineAligner(u, t);
                    final AbstractMap.SimpleEntry<Polyline, Polyline> polyPair = aligner.align();

                    final int addedAngles = aligner.getAddedAngles();
                    final double penalty = 1 + (double) addedAngles / (double) (addedAngles + aligner.getMatches());
                    // da riconoscere
                    final Polyline unknown = polyPair.getKey();
                    // confrontato con
                    final Polyline template = polyPair.getValue();

                    // aggiunto da roberto
                    this.rInvariant = template.getGesture().isRotInv();
                    if (template.getGesture().isRotInv()) {
                        this.angle = ANGLE_INVARIANT;
                    }
                    // ------------------

                    final List<Vector> vectorsU = unknown.getVectors();
                    if (VERBOSE) {
                        System.out.println(vectorsU);
                    }
                    final List<Vector> vectorsT = template.getVectors();
                    if (VERBOSE) {
                        System.out.println(vectorsT);
                    }
                    Double bestDist = null;
                    if (!GSS) {
                        final double uAngle = unknown.getGesture().getIndicativeAngle(!unknown.getGesture().isRotInv());
                        if (VERBOSE) {
                            System.out.println("Indicative angle = " + uAngle);
                        }
                        final double tAngle = template.getGesture()
                                .getIndicativeAngle(!template.getGesture().isRotInv());
                        if (VERBOSE) {
                            System.out.println("Indicative angleT = " + tAngle);
                        }
                        bestDist = getDistanceAtAngle(vectorsU, vectorsT, -uAngle, -tAngle);
                        if (VERBOSE) {
                            System.out.println("Distance at = " + (-uAngle) + "; dist = " + bestDist);
                        }
                    } else {
                        bestDist = getDistanceAtBestAngle(unknown, template);
                    }
                    distance = penalty * bestDist;
                }

                if (distance < classdistance) {
                    classdistance = distance;
                }
                if (distance < a) {
                    a = distance;
                    templateName = e.getKey();
                }
            }

            if (classdistance != Double.POSITIVE_INFINITY) {
                final Double classscore = (2.0f - classdistance) / 2;

                ranking.put(e.getKey(), new double[] {classdistance, Math.round(classscore * 10000) / 100.});
            }
        }

        if (templateName != null) {
            final Double score = (2.0f - a) / 2;

            return new Result(templateName, score, ranking);
        }

        // System.out.println(" null distance ");
        return null;
    }
    // CHECKSTYLE:ON

    private Double getDistanceAtAngle(List<Vector> v1, List<Vector> v2, double theta1, double theta2) {
        double cost = 0;
        if (VERBOSE) {
            System.out.println(v1);
        }
        if (VERBOSE) {
            System.out.println(v2);
        }
        if (v1.size() != v2.size()) {
            System.out.println("distance at angle " + v1.size() + " " + v2.size());
        }
        for (int i = 0; i < v1.size(); i++) {
            final double diff = v1.get(i).difference(v2.get(i), theta1, theta2);
            if (VERBOSE) {
                System.out.print(diff + " + ");
            }
            cost += diff;
        }
        if (VERBOSE) {
            System.out.println();
        }
        return cost;
    }

    /**
     * @param degree
     *            Angle step for Golden Section Search
     */
    public void setRotationAngle(Integer degree) {
        if (degree > 0) {
            this.angleStep = degree;
        } else {
            this.angleStep = 2;
        }
    }

    /**
     * @return Angle step for Golden Section Search
     */
    public Integer getRotationAngle() {
        return this.angleStep;
    }

    // TODO CHECKSTYLE:OFF
    /**
     * @param u
     *            First polyline
     * @param t
     *            Second polyline
     * @return The distance at the best angle
     */
    public Double getDistanceAtBestAngle(Polyline u, Polyline t) {
        Double a = Math.toRadians(-this.angle);
        Double b = Math.toRadians(this.angle);
        final Double treshold = Math.toRadians(this.angleStep);

        double uAngle = u.getGesture().getIndicativeAngle(!rInvariant);
        // System.out.println("Indicative angle = "+uAngle);
        double tAngle = t.getGesture().getIndicativeAngle(!rInvariant);
        // System.out.println("Indicative angleT = "+tAngle);

        // NON EFFETTUA L'ALLINEAMENTO INIZIALE
        if (!rInvariant) {
            uAngle = 0;
            tAngle = 0;
        }

        final List<Vector> vectorsU = u.getVectors();
        // System.out.println(vectorsU);
        final List<Vector> vectorsT = t.getVectors();
        // System.out.println(vectorsT);

        Double alpha = (phi * a) + (1.0f - phi) * b;
        Double beta = (1.0f - phi) * a + (phi * b);
        Double pathA = getDistanceAtAngle(vectorsU, vectorsT, -uAngle + alpha, -tAngle);

        if (VERBOSE) {
            System.out.println("Testing at = " + alpha + "; dist = " + pathA);
        }
        Double pathB = getDistanceAtAngle(vectorsU, vectorsT, -uAngle + beta, -tAngle);
        if (VERBOSE) {
            System.out.println("Testing at = " + (-uAngle + beta) + "; dist = " + pathB);
        }

        if (pathA != Double.POSITIVE_INFINITY && pathB != Double.POSITIVE_INFINITY) {
            while (Math.abs(b - a) > treshold) {
                if (pathA < pathB) {
                    b = beta;
                    beta = alpha;
                    pathB = pathA;
                    alpha = phi * a + (1.0f - phi) * b;
                    pathA = getDistanceAtAngle(vectorsU, vectorsT, -uAngle + alpha, -tAngle);
                    if (VERBOSE) {
                        System.out.println("Testing at = " + (-uAngle + alpha) + "; dist = " + pathA);
                    }
                } else {
                    a = alpha;
                    alpha = beta;
                    pathA = pathB;
                    beta = (1.0f - phi) * a + phi * b;
                    pathB = getDistanceAtAngle(vectorsU, vectorsT, -uAngle + beta, -tAngle);
                    if (VERBOSE) {
                        System.out.println("Testing at = " + (-uAngle + beta) + "; dist = " + pathB);
                    }
                }
            }

            final Double finalDist = Math.min(pathA, pathB);
            // System.out.println("Final distance = "+finalDist);
            return finalDist;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
    // CHECKSTYLE:ON

    // TODO CHECKSTYLE:OFF
    /**
     * Aggiunto da Roberto B.
     * 
     * Verifica similarità tra un template di una classe e i template di tutte le altre classi
     * 
     * @param u
     *            Template da veriticare
     * @param className
     *            Classe del template da verificare
     * @param scorelimit
     *            Score al di sopra del cui i template sono troppo simili
     * @return
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings
    public synchronized ArrayList<Result> verifyTemplate(Polyline u, String className, int scorelimit) {
        // this.rInvariant = _gesture.isRotInv(); if (this.rInvariant)
        // this.angle = ANGLE_INVARIANT; else this.angle = ANGLE_SENSITIVE;

        // PolylineFinder pf = new DouglasPeuckerReducer(_gesture, params);
        // Polyline u = pf.find();

        Double a = Double.POSITIVE_INFINITY;
        String templateName = null;
        Polyline t = null;

        ArrayList<Result> results = new ArrayList<Result>();

        // per tutte le classi
        for (String key : templates.keySet()) {
            // classe diversa da quella del template da controllare
            if (key != className) {
                ArrayList<Polyline> tempTemplates = templates.get(key);
                for (int i = 0; i < tempTemplates.size(); i++) {
                    t = tempTemplates.get(i);

                    PolylineAligner aligner = new PolylineAligner(u, t);
                    AbstractMap.SimpleEntry<Polyline, Polyline> polyPair = aligner.align();

                    int addedAngles = aligner.getAddedAngles();
                    double penalty = 1 + (double) addedAngles / (double) (addedAngles + aligner.getMatches());
                    Polyline unknown = polyPair.getKey();// da riconoscere
                    Polyline template = polyPair.getValue();// confrontato con

                    // aggiunto da roberto
                    if (template.getGesture().isRotInv() || unknown.getGesture().isRotInv()) {
                        this.rInvariant = true;
                        this.angle = ANGLE_INVARIANT;
                    }
                    // ------------------

                    List<Vector> vectorsU = unknown.getVectors();
                    if (VERBOSE)
                        System.out.println(vectorsU);
                    List<Vector> vectorsT = template.getVectors();
                    if (VERBOSE)
                        System.out.println(vectorsT);
                    Double bestDist = null;
                    if (!GSS) {
                        double uAngle = unknown.getGesture().getIndicativeAngle(!unknown.getGesture().isRotInv());
                        if (VERBOSE)
                            System.out.println("Indicative angle = " + uAngle);
                        double tAngle = template.getGesture().getIndicativeAngle(!template.getGesture().isRotInv());
                        if (VERBOSE)
                            System.out.println("Indicative angleT = " + tAngle);
                        bestDist = getDistanceAtAngle(vectorsU, vectorsT, -uAngle, -tAngle);
                        if (VERBOSE)
                            System.out.println("Distance at = " + (-uAngle) + "; dist = " + bestDist);
                    } else
                        bestDist = getDistanceAtBestAngle(unknown, template);
                    Double distance = penalty * bestDist;
                    if (VERBOSE)
                        System.out.println("Confronto con Gesture " + key + " ROTINV:"
                                + template.getGesture().isRotInv() + " SCORE:" + (2.0f - distance) / 2);

                    Double score = (2.0f - distance) / 2;
                    Result result = new Result(key, i, score);

                    // template troppo simili
                    if (result.getScore() > scorelimit) {
                        results.add(result);

                    }
                    if (VERBOSE)
                        System.out.println(
                                "Template troppo simile a polyline " + i + " ROTINV:" + template.getGesture().isRotInv()
                                        + " della classe " + key + "  (SCORE:" + (2.0f - distance) / 2 + ")");
                    if (distance < a) {
                        a = distance;
                        templateName = key;
                    }

                }
            }

        }

        return results;
    }

    /**
     * Aggiunta di Roberto B.
     * 
     * Confronto tra due template
     * 
     * @param u
     *            Primo dei template da confrontare
     * @param t
     *            Secondo dei template da confrontare
     * @return Distanza tra i due template
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings
    public synchronized Double checkTemplate(Polyline u, Polyline t) {
        PolylineAligner aligner = new PolylineAligner(u, t);
        AbstractMap.SimpleEntry<Polyline, Polyline> polyPair = aligner.align();

        int addedAngles = aligner.getAddedAngles();
        double penalty = 1 + (double) addedAngles / (double) (addedAngles + aligner.getMatches());
        Polyline unknown = polyPair.getKey();// da riconoscere
        Polyline template = polyPair.getValue();// confrontato con

        // modifica roberto

        if (template.getGesture().isRotInv() || unknown.getGesture().isRotInv()) {
            this.rInvariant = true;
            this.angle = ANGLE_INVARIANT;
        }
        // ------------------

        List<Vector> vectorsU = unknown.getVectors();
        if (VERBOSE)
            System.out.println(vectorsU);
        List<Vector> vectorsT = template.getVectors();
        if (VERBOSE)
            System.out.println(vectorsT);
        Double bestDist = null;
        if (!GSS) {
            double uAngle = unknown.getGesture().getIndicativeAngle(!unknown.getGesture().isRotInv());
            if (VERBOSE)
                System.out.println("Indicative angle = " + uAngle);
            double tAngle = template.getGesture().getIndicativeAngle(!template.getGesture().isRotInv());
            if (VERBOSE)
                System.out.println("Indicative angleT = " + tAngle);
            bestDist = getDistanceAtAngle(vectorsU, vectorsT, -uAngle, -tAngle);
            if (VERBOSE)
                System.out.println("Distance at = " + (-uAngle) + "; dist = " + bestDist);
        } else
            bestDist = getDistanceAtBestAngle(unknown, template);
        Double distance = penalty * bestDist;
        return distance;

    }

}
