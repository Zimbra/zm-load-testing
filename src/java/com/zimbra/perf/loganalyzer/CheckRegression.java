/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite, Network Edition.
 * Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc.  All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.perf.loganalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author pandurangd
 * 
 */
public class CheckRegression {

    public static class SoapCallStats {

        public String CallName;
        public long Count;
        public long Average;
        public long StdDev;
        public long Min;
        public long Max;

        public SoapCallStats(String line) {
            CallName = "";
            Count = 0;
            Average = 0;
            StdDev = 0;
            Min = 0;
            Max = 0;

            if (line == null || !line.contains(","))
                return;

            String parts[] = line.split(",");
            if (parts.length != 6)
                return;

            CallName = parts[0];
            Count = Long.parseLong(parts[1]);
            Average = Long.parseLong(parts[2]);
            StdDev = Long.parseLong(parts[3]);
            Min = Long.parseLong(parts[4]);
            Max = Long.parseLong(parts[5]);

        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: CheckRegression <Report file path> <Log file1> <Log file2>");
            return;
        }

        try {
            String htmlFile = args[0] + File.separator + "RgressionResult.html";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(htmlFile)));

            BufferedReader in1 = new BufferedReader(new FileReader(args[1]));
            BufferedReader in2 = new BufferedReader(new FileReader(args[2]));
            HashMap<String, SoapCallStats> sc1 = new HashMap<String, SoapCallStats>();
            HashMap<String, SoapCallStats> sc2 = new HashMap<String, SoapCallStats>();

            String line = in1.readLine(); // skip header line
            line = in1.readLine();
            while (line != null) {
                SoapCallStats scs = new SoapCallStats(line);
                sc1.put(scs.CallName, scs);
                line = in1.readLine();
            }
            in1.close();

            line = in2.readLine();
            ;// skip header line
            line = in2.readLine();
            while (line != null) {
                SoapCallStats scs = new SoapCallStats(line);
                sc2.put(scs.CallName, scs);
                line = in2.readLine();
            }
            in2.close();

            boolean regressed = false;
            ArrayList<String> soapCalls = new ArrayList<String>(sc2.keySet());
            Collections.sort(soapCalls);
            for (String soapCall : soapCalls) {
                // Start with assuming regression
                String Regression = "Red";
                SoapCallStats scs2 = sc2.get(soapCall);
                SoapCallStats scs1 = sc1.get(soapCall);
                if (scs1 != null) {
                    if (scs1.Count == scs2.Count) {
                        // Atlest Call count matches
                        Regression = "Yellow";
                        if ((scs2.Average >= (scs1.Average - scs1.StdDev)) && (scs2.Average <= (scs1.Average + scs1.StdDev))) {
                            // Near perfect, average RT is in range of 2sigma
                            Regression = "Green";
                        }
                    }
                } else
                    scs1 = new SoapCallStats(null);

                if (!Regression.matches("Green")) {
                    if (!regressed) {
                        // Write header for the first time
                        out.println("<HTML>");
                        out.println("<h3> Soap Call Regression Report</h3>");
                        out.println("<Table border='1'>");
                        out.println("<tr><th rowspan='2'>Soap Call</th><th colspan='2'>Base</th><th colspan='2'>Current</th></tr>");
                        out.println("<tr><th>Call Count</th><th>Average RT</th><th>Call Count</th><th>Average RT</th></tr>");
                        regressed = true;
                    }
                    if (Regression.matches("Yellow"))
                        out.println("<tr><td>" + scs2.CallName + "</td><td>" + scs1.Count + "</td><td>" + scs1.Average + "</td><td>" + scs2.Count
                            + "</td><td bgcolor='" + Regression + "'>" + scs2.Average + "</td></tr>");
                    else
                        out.println("<tr><td>" + scs2.CallName + "</td><td>" + scs1.Count + "</td><td>" + scs1.Average + "</td><td bgcolor='"
                            + Regression + "'>" + scs2.Count + "</td><td>" + scs2.Average + "</td></tr>");
                }
            }

            if (regressed) {
                out.println("</Table>");
                out.println("</HTML>");
            } else
                out.println("No Regression");

            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

    }

}
