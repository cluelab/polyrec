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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Recognizer abstract class.
 * 
 * @author Vittorio
 *
 */
public abstract class Recognizer {
    private static final String ELEM_ROOT = "set";
    private static final String ELEM_CLASS = "class";
    private static final String ATTR_CLASS_NAME = "name";
    private static final String ELEM_TEMPLATE = "template";
    private static final String ATTR_POINTERS = "pointers";
    private static final String ATTR_ROTATION_INVARIANT = "rotinv";
    private static final String ELEM_POINT = "point";
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";
    private static final String ATTR_T = "t";

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
     * @return The templates currently used by the recognizer
     */
    public Map<String, List<Gesture>> getTemplates() {
        final LinkedHashMap<String, List<Gesture>> res = new LinkedHashMap<String, List<Gesture>>();
        for (Map.Entry<String, ArrayList<Polyline>> e : templates.entrySet()) {
            final ArrayList<Gesture> gests = new ArrayList<Gesture>();
            for (Polyline p : e.getValue()) {
                gests.add(p.getGesture());
            }
            res.put(e.getKey(), gests);
        }
        return res;
    }

    /**
     * Remove all of the templates.
     */
    public void clear() {
        templates.clear();
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

    /**
     * Load gestures from a .xml file. Any existing gestures will be removed.
     * 
     * @param file
     *            The file to be loaded
     */
    public void loadTemplatesXML(File file) throws Exception {
        loadTemplatesXML(file, true);
    }

    /**
     * Load template gestures from a .xml file.
     * 
     * @param file
     *            The file to be loaded
     * @param removeExistent
     *            whether to remove any existing gesture
     */
    public void loadTemplatesXML(File file, boolean removeExistent) throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();

        final Document doc = db.parse(file);

        final XPathFactory xPathfactory = XPathFactory.newInstance();
        final XPath xpath = xPathfactory.newXPath();

        final XPathExpression expr = xpath.compile("/set/class");
        final NodeList classList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        final ArrayList<Gesture> gestures = new ArrayList<Gesture>();
        for (int i = 0; i < classList.getLength(); i++) {
            final Node classNode = classList.item(i);
            final Node nName = classNode.getAttributes().getNamedItem(ATTR_CLASS_NAME);

            if (nName != null) {
                final String className = nName.getNodeValue();
                final NodeList templateList = (NodeList) classNode.getChildNodes();

                for (int j = 0; j < templateList.getLength(); j++) {
                    final Node templateNode = templateList.item(j);
                    final NodeList pointList = (NodeList) templateNode.getChildNodes();

                    final Gesture gesture = new Gesture();
                    gesture.setPointers(
                            Integer.parseInt(templateNode.getAttributes().getNamedItem(ATTR_POINTERS).getNodeValue()));
                    gesture.setRotInv(Boolean.valueOf(
                            templateNode.getAttributes().getNamedItem(ATTR_ROTATION_INVARIANT).getNodeValue()));
                    for (int p = 0; p < pointList.getLength(); p++) {
                        final Node point = pointList.item(p);
                        final TPoint tpoint = new TPoint(
                                Double.parseDouble(point.getAttributes().getNamedItem(ATTR_X).getNodeValue()),
                                Double.parseDouble(point.getAttributes().getNamedItem(ATTR_Y).getNodeValue()),
                                Long.parseLong(point.getAttributes().getNamedItem(ATTR_T).getNodeValue()));
                        gesture.addPoint(tpoint);
                        gesture.setInfo(new GestureInfo(0, null, className, 0));

                    }
                    gestures.add(gesture);
                }
            }
        }
        if (removeExistent) {
            clear();
        }
        for (Gesture g : gestures) {
            addTemplate(g.getInfo().getName(), g);
        }
    }

    /**
     * Save the template gestures as a .psg binary file.
     */
    public void saveTemplatesXML(File file) throws Exception {
        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        // root elements
        final Document doc = docBuilder.newDocument();
        final Element rootElement = doc.createElement(ELEM_ROOT);
        doc.appendChild(rootElement);
        for (Map.Entry<String, List<Gesture>> e : getTemplates().entrySet()) {
            final Element classElement = doc.createElement(ELEM_CLASS);
            classElement.setAttribute(ATTR_CLASS_NAME, e.getKey());
            rootElement.appendChild(classElement);

            for (Gesture g : e.getValue()) {
                final Element templateElement = doc.createElement(ELEM_TEMPLATE);
                classElement.appendChild(templateElement);
                templateElement.setAttribute(ATTR_POINTERS, Integer.toString(g.getPointers()));
                templateElement.setAttribute(ATTR_ROTATION_INVARIANT, String.valueOf(g.isRotInv()));
                final ArrayList<TPoint> points = g.getPoints();
                for (int p = 0; p < points.size(); p++) {
                    final Element pointElement = doc.createElement(ELEM_POINT);
                    pointElement.setAttribute(ATTR_X, String.valueOf(points.get(p).getX()));
                    pointElement.setAttribute(ATTR_Y, String.valueOf(points.get(p).getY()));
                    pointElement.setAttribute(ATTR_T, String.valueOf(points.get(p).getTime()));
                    templateElement.appendChild(pointElement);
                }
            }

            rootElement.appendChild(classElement);
        }

        final StreamResult result = new StreamResult(file);

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
    }

}
