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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.DefaultRowSorter;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Class representing the result of gesture recognition.
 * 
 * @author Vittorio
 *
 */
public class Result {

    private String name;
    private Double score;
    // aggiunto da roberto
    private int templateIndex = -1;
    private TreeMap<String, double[]> ranking = new TreeMap<String, double[]>();

    public Result(String name, Double score, TreeMap<String, double[]> ranking) {
        this.name = name;
        this.score = Math.round(score * 10000) / 100.;
        this.ranking = ranking;
    }

    protected Result(String name, Double score) {
        this.name = name;
        this.score = Math.round(score * 10000) / 100.;
        this.templateIndex = -1;
    }

    // aggiunto da roberto
    protected Result(String name, int templateIndex, Double score) {
        this.name = name;
        this.templateIndex = templateIndex;
        this.score = Math.round(score * 10000) / 100.;
    }

    /**
     * @return The name of the recognized class
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The recognition score
     */
    public Double getScore() {
        return this.score;
    }

    public int getTemplateIndex() {
        return this.templateIndex;
    }

    // aggiunto da roberto
    public String toString() {
        return (this.templateIndex >= 0 ? "Template " + (this.templateIndex) + " of Class " + this.name.toUpperCase()
                : "Class " + this.name) + " (score: " + this.score + ")";
    }

    public TreeMap<String, double[]> getRanking() {
        return this.ranking;
    }

    // TODO CHECKSTYLE:OFF

    public JTable getRankingTable() {

        Set<Entry<String, double[]>> entries = this.ranking.entrySet();

        // String[] columnHeaders = {"<html><font color='white' >CLASS</font></html>", "<html><font color='white'
        // >DISTANCE</font></html>", "<html><font color='white' >SCORE</font></html>"};
        String[] columnHeaders = { "CLASS", "DISTANCE", "SCORE" };

        String[][] rowData = new String[entries.size()][columnHeaders.length];

        int i = 0;
        for (Entry<String, double[]> e : entries) {

            rowData[i][0] = e.getKey();

            rowData[i][1] = String.valueOf(round(e.getValue()[0], 5));
            rowData[i][2] = String.valueOf(round(e.getValue()[1], 5));
            i++;
        }
        JTable table = new JTable(rowData, columnHeaders);

        table.setEnabled(false);

        // headers
        // table.getTableHeader().setBackground(Color.gray);
        // table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.BLACK));
        // table.getTableHeader().setOpaque(false);

        // sorting
        table.setAutoCreateRowSorter(true);
        DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
        ArrayList list = new ArrayList();

        list.add(new RowSorter.SortKey(2, SortOrder.DESCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        // column alignment
        // DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        // rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        // table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        // table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        // ((JLabel) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.getColumn(table.getColumnName(0)).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                setText(value.toString());
                setBackground(new Color(00, 00, 00, 10));
                return this;
            }
        });

        // table.getColumn(table.getColumnName(2)).setCellRenderer(
        // new DefaultTableCellRenderer() {
        // @Override
        // public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean
        // hasFocus, int row, int column) {
        // setText(value.toString());
        // System.out.println("classname "+classname+" name "+name+" row"+row+" col"+column);
        // System.out.println(classname.equals(name));
        //
        //
        // Double scorevalue = Math.round(Double.parseDouble(value.toString()) * 10000) / 100.;
        //
        // System.out.println("valore "+scorevalue+" setting "+
        // java.lang.Double.parseDouble(Settings.applicationProps.getProperty("scorelimit")));
        // System.out.println(scorevalue > Double.parseDouble(Settings.applicationProps.getProperty("scorelimit")));
        // if (!classname.equals(name) && scorevalue >
        // Double.parseDouble(Settings.applicationProps.getProperty("scorelimit")))
        // setBackground(Color.red);
        // return this;
        // }
        // }
        // );

        return table;

    }

    // round 'n' to 'd' decimals
    private double round(double n, double d) {
        d = Math.pow(10, d);
        return Math.round(n * d) / d;
    }

}
