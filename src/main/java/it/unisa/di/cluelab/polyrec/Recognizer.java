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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recognizer abstract class.
 * 
 * @author Vittorio
 *
 */
public abstract class Recognizer implements Serializable {
    protected Map<String, ArrayList<Polyline>> templates;
    protected String method;

    /**
     * Adds a new template.
     * 
     * @param name
     *            class name
     * @param gesture
     *            the gesture
     * @return size of list of templates of the class
     */
    public abstract int addTemplate(String name, Gesture gesture);

    /**
     * @param gesture
     *            the gesture
     * @return The recognition result
     */
    public abstract Result recognize(Gesture gesture);

    /**
     * @param name
     *            class name
     * @param templates
     *            list of template gestures
     */
    public void addTemplates(String name, List<Gesture> templates) {
        for (Gesture g : templates) {
            addTemplate(name, g);
        }
    }

    /**
     * @return The name of the recognizer
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return The names of the classes
     */
    public Set<String> getClassNames() {
        return templates.keySet();
    }

}
