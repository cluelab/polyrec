# polyrec

[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)
[![Build Status](http://img.shields.io/travis/cluelab/polyrec.svg?style=flat&branch=master)](https://travis-ci.org/cluelab/polyrec)
[![Coverage Status](https://img.shields.io/coveralls/cluelab/polyrec.svg?style=flat)](https://coveralls.io/r/cluelab/polyrec?branch=master)

### About

PolyRec is a unistroke gesture recognizer suitable for fast prototyping of gesture-based applications. The recognizer uses a nearest neighbor approach, and requires a small number of samples for each class.

The similarity between two gestures is calculated through a three steps procedure:
- firstly, each gesture is approximated to a polyline, in order to extract its main movements;
- then, the two polylines are aligned to obtain an equal number of segments from both of them;
- lastly, the distance is found by summing the contribution of each pair of segments.

This implementation is a prototype developed for scientific purposes. More technical details on PolyRec can be found in the following scientific paper:
V Fuccella, G. Costagliola; Unistroke Gesture Recognition Through Polyline Approximation and Alignment; in Proceedings of CHI 2015; pp. 3351-3354
http://dl.acm.org/citation.cfm?id=2702505 

### Setup
 
Releases are published to [bintray jcenter](https://bintray.com/cluelab/polyrec/polyrec/)
[![JCenter](https://img.shields.io/bintray/v/cluelab/polyrec/polyrec.svg?label=jcenter)](https://bintray.com/cluelab/polyrec/polyrec/_latestVersion)

Maven:

```xml
<dependency>
  <groupId>it.unisa.di.cluelab</groupId>
  <artifactId>polyrec</artifactId>
  <version>0.2.0</version>
</dependency>
```

Gradle:

```groovy
compile 'it.unisa.di.cluelab:polyrec:0.2.0'
```

##### Snapshots

You can use snapshot versions through [JitPack](https://jitpack.io):

* Go to [JitPack project page](https://jitpack.io/#cluelab/polyrec)
* Select `Commits` section and click `Get it` on commit you want to use (top one - the most recent)
* Follow displayed instruction: add repository and change dependency (NOTE: due to JitPack convention artifact group will be different)

### Usage

```java
// recognizer setup
Recognizer recognizer = new PolyRecognizerGSS(true);
Gesture trCw = new Gesture();
trCw.addPoint(new TPoint(0, 0, 0));
trCw.addPoint(new TPoint(100, 0, 0));
trCw.addPoint(new TPoint(0, 100, 0));
trCw.addPoint(new TPoint(0, 0, 0));
recognizer.addTemplate("triangle-clockwise", trCw);
Gesture trCcw = new Gesture();
trCcw.addPoint(new TPoint(0, 0, 0));
trCcw.addPoint(new TPoint(0, 100, 0));
trCcw.addPoint(new TPoint(100, 0, 0));
trCcw.addPoint(new TPoint(0, 0, 0));
recognizer.addTemplate("triangle-counterclockwise", trCcw);
Gesture rectCw = new Gesture();
rectCw.addPoint(new TPoint(0, 0, 0));
rectCw.addPoint(new TPoint(100, 0, 0));
rectCw.addPoint(new TPoint(100, 200, 0));
rectCw.addPoint(new TPoint(0, 200, 0));
rectCw.addPoint(new TPoint(0, 0, 0));
recognizer.addTemplate("rectangle", rectCw);
Gesture rectCcw = new Gesture();
rectCcw.addPoint(new TPoint(0, 0, 0));
rectCcw.addPoint(new TPoint(0, 200, 0));
rectCcw.addPoint(new TPoint(100, 200, 0));
rectCcw.addPoint(new TPoint(100, 0, 0));
rectCcw.addPoint(new TPoint(0, 0, 0));
recognizer.addTemplate("rectangle", rectCcw);

// gesture recognition
Gesture drawnGesture = ...;
Result r = recognizer.recognize(drawnGesture);
System.out.println("Result: " + r.getName() + ", score: " + r.getScore());
```
