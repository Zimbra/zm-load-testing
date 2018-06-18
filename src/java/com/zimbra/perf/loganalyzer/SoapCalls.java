/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite, Network Edition.
 * Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc.  All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.perf.loganalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

/**
 * @author pandurangd
 * 
 */
public class SoapCalls {

    public static class RTStats {

        public long min;
        public long max;
        public long medium;
        public double mean;
        public double sd;
        public long count;

        public String toString() {
            return "\nAgerage: " + mean + "\nMedium: " + medium + "\nMin: " + min + "\nMax: " + max + "\nSigma: " + sd + "\n2 Sigma: " + (mean - sd)
                + " - " + (mean + sd) + "\n";
        }
    }

    HashMap<String, ArrayList<Long>> soapRTs;

    public class CountComparator implements Comparator<Map.Entry<String, ArrayList<Long>>> {

        public int compare(Map.Entry<String, ArrayList<Long>> a, Map.Entry<String, ArrayList<Long>> b) {
            Integer ct1 = b.getValue().size();
            Integer ct2 = a.getValue().size();
            return ct1.compareTo(ct2);
        }
    }

    SoapCalls() {
        soapRTs = new HashMap<String, ArrayList<Long>>();
    }

    public RTStats getStats(ArrayList<Long> rts) {
        RTStats stats = new RTStats();
        stats.count = rts.size();
        Collections.sort(rts);
        System.out.println(rts);
        int size = rts.size();
        if (size == 1) {
            stats.min = rts.get(0);
            stats.max = stats.min;
            stats.medium = stats.min;
            stats.mean = stats.min;
            stats.sd = 0;
            return stats;
        }

        stats.min = rts.get(0);
        stats.max = rts.get(size - 1);
        stats.medium = rts.get(size / 2);

        // Calculate Mean
        double sum = 0;
        for (Long rt : rts)
            sum += rt;
        double mean = sum / size;
        stats.mean = mean;

        // Calculate SD
        double sumx2 = 0;
        for (Long rt : rts) {
            double x2 = (rt - mean) * (rt - mean);
            sumx2 += x2;
        }
        double sigma = Math.sqrt(sumx2 / size);
        stats.sd = sigma;

        ArrayList<Long> rts2 = new ArrayList<Long>();
        rts2.addAll(rts);
        Collections.sort(rts2);

        //Anything falls out of 3sigma range is outlier and will be discarded.
        double r1 = mean + 1.5 * sigma;
        
        for (int i = rts2.size() - 1; i > 0; i--)
            if (rts2.get(i) > r1)
                rts2.remove(i);

        if (rts2.size() != rts.size()) {
            size = rts2.size();
            System.out.println(rts2);
            // Calculate Mean
            sum = 0;
            for (Long rt : rts2)
                sum += rt;
            mean = sum / size;

            // Calculate SD
            sumx2 = 0;
            for (Long rt : rts2) {
                double x2 = (rt - mean) * (rt - mean);
                sumx2 += x2;
            }
            sigma = Math.sqrt(sumx2 / size);
            stats.mean = mean;
            stats.sd = sigma;
            stats.medium = rts2.get(size / 2);
        }
        return stats;
    }

    public void analyze(String logfile) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(logfile));
            String line = in.readLine();

            while (line != null) {
                int sstart = line.indexOf("soap - ");
                int cip = line.indexOf("ip=127");
                if (sstart > 0 && cip < 0) {
                    // soap - (batch) GetInfoRequest [elapsed=48]
                    int start = sstart + 7;
                    int estart = line.indexOf("elapsed=");
                    int end = estart;
                    if (estart < 0)
                        end = line.length();

                    String soapcall = line.substring(start, end);
                    soapcall = soapcall.trim();
                    long mrt = 0;
                    if (estart > 0) {
                        start = estart + 8; // "elapsed="
                        String rt = line.substring(start);
                        mrt = Long.parseLong(rt);
                    }
                    ArrayList<Long> callrts = soapRTs.get(soapcall);
                    if (callrts == null) {
                        callrts = new ArrayList<Long>();
                        soapRTs.put(soapcall, callrts);
                    }
                    callrts.add(mrt);
                }
                line = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    void writeStyle(PrintWriter out) {
        out.println("<head><style type=\"text/css\"> body {" + "font-family:arial,sans-serif;\n" + "}\n" + "table {\n" + "border:1px solid #000;\n"
            + "border-collapse:collapse;\n" + "font-family:arial,sans-serif;\n" + "font-size:80%;\n" + "}\n" + "td,th{\n"
            + "border:1px solid #000;\n" + "border-collapse:collapse;\n" + "padding:5px;\n" + "} \n" + "caption{\n" + "background:#ccc;\n"
            + "font-size:120%;\n" + "border:1px solid #000;\n" + "border-bottom:none;\n" + "padding:5px;\n" + "text-align:left;\n" + "}\n"
            + "thead th{\n" + "background:#9cf;\n" + "text-align:left;\n" + "}\n" + "tbody th{\n" + "text-align:right;\n" + "background:#69c;\n"
            + "}\n" + "tfoot td{\n" + "text-align:right;\n" + "font-weight:bold;\n" + "background:#369;\n" + "}\n" + "tbody td{\n"
            + "background:#999;\n" + "}\n" + "tbody tr.odd td{\n" + "background:#ccc;\n"
            + "}\n</style>\n <script language=\"javascript\" type=\"text/javascript\">\n" + "function showHide(shID) {\n"
            + "   if (document.getElementById(shID)) {\n" + "      if (document.getElementById(shID+'-show').style.display != 'none') {\n"
            + "         document.getElementById(shID+'-show').style.display = 'none';\n"
            + "         document.getElementById(shID).style.display = 'block';\n" + "      }\n" + "      else {\n"
            + "         document.getElementById(shID+'-show').style.display = 'inline';\n"
            + "         document.getElementById(shID).style.display = 'none';\n" + "      }\n" + "   }\n" + "}\n" + "</script>\n" + "</head>");
    }

    String flipRowTag(String tag) {
        String newTag = "";
        if (tag.contains("class='odd'"))
            newTag = tag.replace(" class='odd'", "");
        else
            newTag = tag.replace("tr", "tr class='odd'");
        return newTag;
    }

    void reportHTML(PrintWriter out) {
        out.println("<HTML>");
        writeStyle(out);

        out.println("<Table style='border:1px solid #000; border-collapse:collapse;'>");
        out.println("<thead><tr><th align='right' rowspan='2'>Soap Call</th><th rowspan='2'>Count</th><th colspan='4'>Response Time (ms)</th></tr>");
        out.println("<tr><th>Average</th><th>Std Dev</th><th>Min</th><th>Max</th></tr></thead>");

        String TrTag = "<tr>";

        ArrayList<String> soapCalls = new ArrayList<String>(soapRTs.keySet());
        Collections.sort(soapCalls);
        for (String soapCall : soapCalls) {
            RTStats s = getStats(soapRTs.get(soapCall));
            out.printf(
                "%s<td>%s</td><td align='right'>%d</td><td align='right'>%.0f</td><td align='right'>%.0f</td><td align='right'>%d</td><td align='right'>%d</td></tr>\n",
                TrTag, soapCall, s.count, s.mean, s.sd, s.min, s.max);
            TrTag = flipRowTag(TrTag);
        }
        out.println("</table>");
        out.println("</HTML>");

    }
    
    void reportText(PrintWriter out) {
        ArrayList<String> soapCalls = new ArrayList<String>(soapRTs.keySet());
        Collections.sort(soapCalls);
        out.printf("-------------------------------------------------------------------------------------\n");
        out.printf("Soap Call                                   Count  Average  Std Dev      Min      Max\n");
        out.printf("-------------------------------------------------------------------------------------\n");

        for (String soapCall : soapCalls) {
            RTStats s = getStats(soapRTs.get(soapCall));
            out.printf("%-40s%9d%9.0f%9.0f%9d%9d\n", soapCall, s.count, s.mean, s.sd, s.min, s.max);
        }
        out.printf("-------------------------------------------------------------------------------------\n");
    }
    
    void reportCsv(PrintWriter out) {
        ArrayList<String> soapCalls = new ArrayList<String>(soapRTs.keySet());
        Collections.sort(soapCalls);
        out.println("Soap Call,Count,Average,Std Dev,Min,Max");

        for (String soapCall : soapCalls) {
            RTStats s = getStats(soapRTs.get(soapCall));
            out.println(soapCall + "," + s.count + "," + (int)s.mean + "," + (int)s.sd + "," + s.min + "," + s.max);
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: SoapCalls <Report file path> <Log file1> [Log file 2] ... ");
            return;
        }
        try {
            String outFileName = args[0];

            SoapCalls a = new SoapCalls();
            for (int i = 1; i < args.length; i++)
                a.analyze(args[i]);

            String htmlFile = outFileName + ".html";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(htmlFile)));
            a.reportHTML(out);
            out.flush();
            out.close();

            String csvFile = outFileName + ".csv";
            out = new PrintWriter(new BufferedWriter(new FileWriter(csvFile)));
            a.reportCsv(out);
            out.flush();
            out.close();
/*
            String txtFile = outFileName + ".txt";
            out = new PrintWriter(new BufferedWriter(new FileWriter(txtFile)));
            a.reportText(out);
            out.flush();
            out.close();
*/
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
