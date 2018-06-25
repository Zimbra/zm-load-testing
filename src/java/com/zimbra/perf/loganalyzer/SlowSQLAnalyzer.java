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

public class SlowSQLAnalyzer {

    public static class EntryCount {

        public String entry;
        public long count;

        EntryCount(String name) {
            entry = name;
        }

        long increment() {
            return count++;
        }
    }

    HashMap<String, ArrayList<LogEntry>> sqlTexts;

    public class CountComparator implements Comparator<Map.Entry<String, ArrayList<LogEntry>>> {

        public int compare(Map.Entry<String, ArrayList<LogEntry>> a, Map.Entry<String, ArrayList<LogEntry>> b) {
            Integer ct1 = b.getValue().size();
            Integer ct2 = a.getValue().size();
            return ct1.compareTo(ct2);
        }
    }

    public class RTComparator implements Comparator<LogEntry> {

        public int compare(LogEntry a, LogEntry b) {
            Long art = Long.parseLong(a.sqlRT);
            Long brt = Long.parseLong(b.sqlRT);
            return brt.compareTo(art);
        }
    }

    public static class LogEntry {

        public String timestamp;
        public String request;
        public String user;
        public String clientip;
        public String useragent;
        public String Details;
        public String sqlRT;
        public String FullSQL;
        public String sqlText;
        public String fullLine;

        public String getSQLText(String fulltext) {
            String sqlText = fulltext.replaceAll("\\d", "");
            sqlText = sqlText.replaceAll("'.*'", ""); // oc.hash =
                                                      // '4tNVg5IAlgG7nF4WTrLzh7mqzmA='
            sqlText = sqlText.replaceAll("(, )+", ",");
            sqlText = sqlText.replaceAll(",+", ",");

            return sqlText;

        }

        LogEntry(String line) {
            // 2012-06-04 11:54:04,971 INFO
            // [qtp713879274-18158:https://10.113.63.59:443/service/soap/SearchRequest]
            // [name=eshen@zimbra.com;mid=164;ip=10.33.31.202;ua=ZimbraWebClient
            // - SAF5.1 (Mac)/8.0.0_BETA4_5202;] sqltrace - Slow execution (
            // 7056 ms): SELECT COUNT(*) FROM mboxgroup64.mail_item AS mi WHERE
            // mi.mailbox_id = 164 AND ((mi.type = 5 OR mi.type = 16) AND
            // mi.folder_id = 2)

            if (!line.contains("INFO"))
                return;
            fullLine = line;
            int start = 0;
            int index = 0;
            timestamp = line.substring(0, line.indexOf("INFO"));
            index = timestamp.length() + 5; // for "INFO"
            // System.out.println("TimeStamp: "+timestamp);

            int secStart = line.indexOf("[", index) + 1;
            int secEnd = line.indexOf("]", index);
            String reqSection = line.substring(secStart, secEnd);
            // request = reqSection.substring(reqSection.indexOf("http"));

            request = reqSection.replaceAll("qtp.*:htt", "htt");
            index = secEnd + 1;
            secStart = line.indexOf("[", index) + 1;
            secEnd = line.indexOf("]", secStart);
            if (secEnd > secStart) {
                String uas = line.substring(secStart, secEnd);
                String[] ua = uas.split(";");
                for (String s : ua) {
                    if (s.startsWith("name"))
                        user = s.replace("name=", "");
                    else if (s.startsWith("ua="))
                        useragent = s.replaceAll("ua=", "");
                    else if (s.startsWith("ip="))
                        clientip = s.replaceAll("ip=", "");
                }
            }
            // clientip = clientip + " Device: " +useragent;
            secStart = line.indexOf("sqltrace -");
            Details = line.substring(secStart);
            secEnd = Details.indexOf(":");
            String rtPart = Details.substring(0, secEnd);
            secStart = secEnd + 1;
            FullSQL = Details.substring(secEnd + 2);

            // Extract RT from sqltrace - Slow execution (7056ms)
            start = rtPart.indexOf("(");
            index = rtPart.indexOf(")");
            String rt = rtPart.substring(start + 1, index);
            sqlRT = rt.replace("ms", "");

            sqlText = getSQLText(FullSQL);
        }
    }

    SlowSQLAnalyzer() {
        sqlTexts = new HashMap<String, ArrayList<LogEntry>>();
    }
    
    public void analyze(String logfile) {

        try {
            BufferedReader in = new BufferedReader(new FileReader(logfile));
            String line = in.readLine();
            line = in.readLine();

            while (line != null) {
                if (line.contains("sqltrace - Slow execution")) {
                    // Skip for these tags
                    if (line.contains("Setting slow SQL threshold") || line.contains("Backup") || line.contains("MailboxPurge")) {
                        line = in.readLine();
                        continue;
                    }
                    LogEntry logentry = new LogEntry(line);
                    ArrayList<LogEntry> lst = sqlTexts.get(logentry.sqlText);
                    if (lst == null) {
                        lst = new ArrayList<LogEntry>();
                        sqlTexts.put(logentry.sqlText, lst);
                    }

                    lst.add(logentry);
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
            + "background:#999;\n" + "}\n" + "tbody tr.odd td{\n" + "background:#ccc;\n" + "}\n</style></head>");
    }

    String flipRowTag(String tag) {
        String newTag = "";
        if (tag.contains("class='odd'"))
            newTag = tag.replace("class='odd'", "");
        else
            newTag = tag.replace("tr", "tr class='odd'");
        return newTag;
    }

    void reportText(PrintWriter out) {

        out.println("Summary");
        out.println("-------");
        out.println("");
        ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
        for (Map.Entry<String, ArrayList<LogEntry>> sqlct : sqlTexts.entrySet())
            entries.addAll(sqlct.getValue());
        out.println("Total number of Slow Queries found: " + entries.size() + "\n");
        out.println("");

        ArrayList<Map.Entry<String, ArrayList<LogEntry>>> sqlcounts = new ArrayList<Map.Entry<String, ArrayList<LogEntry>>>(sqlTexts.entrySet());
        Collections.sort(sqlcounts, new CountComparator());
        out.printf("%9s   %s\n", "Count", "SQL");
        for (Map.Entry<String, ArrayList<LogEntry>> sqlct : sqlcounts) {
            out.printf("%9d   %s\n", sqlct.getValue().size(), sqlct.getKey());
        }

        //Count per timestamp
        HashMap<String, EntryCount> tsCount = new HashMap<String, EntryCount>();
        for (Map.Entry<String, ArrayList<LogEntry>> sqlct : sqlcounts) {
            for (LogEntry slow : sqlct.getValue()) {
                String ts = slow.timestamp;
                ts = ts.substring(0, ts.indexOf(","));
                EntryCount ct = tsCount.get(ts);
                if (ct == null) {
                    ct = new EntryCount(ts);
                    tsCount.put(ts, ct);
                }
                ct.increment();
            }
        }

        ArrayList<String> tss = new ArrayList<String>(tsCount.keySet());
        Collections.sort(tss);
        out.printf("\n\n%9s   %s\n", "Count", "Time Stamp");
        for (String ts : tss) {
            out.printf("%9d   %s\n", tsCount.get(ts).count, ts);
        }
    }

    void reportHTML(PrintWriter out) {

        String currTrTag = "<tr>\n";
        out.println("<HTML>");
        writeStyle(out);
        out.println("<H3>Summary</H3>");
        ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
        for (Map.Entry<String, ArrayList<LogEntry>> sqlct : sqlTexts.entrySet())
            entries.addAll(sqlct.getValue());
        out.println("<p> Total number of Slow Queries found: " + entries.size() + "</p>\n");

        ArrayList<Map.Entry<String, ArrayList<LogEntry>>> sqlcounts = new ArrayList<Map.Entry<String, ArrayList<LogEntry>>>(sqlTexts.entrySet());
        Collections.sort(sqlcounts, new CountComparator());
        out.println("<Table style='border:1px solid #000; border-collapse:collapse;'><thead><tr><th align='right'>Count</th><th>Time<br/>sec-count</th><th>SQL Text</th></tr></thead>");
        for (Map.Entry<String, ArrayList<LogEntry>> sqlct : sqlcounts) {
            String rts = "";
            int[] irts = new int[200];
            for (LogEntry le : sqlct.getValue()) {
                int irt = Integer.parseInt(le.sqlRT) / 1000;
                if (irt > 10) {
                    if (irt % 10 > 5)
                        irt = irt + (10 - irt % 10);
                    else
                        irt = irt - irt % 10;
                    if (irt > 200)
                        irt = 199;
                }
                irts[irt]++;
            }

            if (irts[199] != 0)
                rts = rts + "200+ " + " - " + irts[199] + "<br/>";

            for (int i = 198; i >= 0; i--) {
                if (irts[i] != 0)
                    rts = rts + i + " - " + irts[i] + "<br/>";
            }
            out.println(currTrTag + "<th>" + sqlct.getValue().size() + "</th><td>" + rts + "</td><td>" + sqlct.getKey() + "</td></tr>");
            currTrTag = flipRowTag(currTrTag);
        }
        out.println("</Table>");

        out.println("<H3>Counts per timestamps</H3>");
        out.println("<Table style='border:1px solid #000; border-collapse:collapse;'><thead><tr><th align='right'>Count</th><th>Time Stamp</th></tr></thead>");
        //Count per timestamp
        HashMap<String, EntryCount> tsCount = new HashMap<String, EntryCount>();
        for (Map.Entry<String, ArrayList<LogEntry>> sqlct : sqlcounts) {
            for (LogEntry slow : sqlct.getValue()) {
                String ts = slow.timestamp;
                ts = ts.substring(0, ts.indexOf(","));
                EntryCount ct = tsCount.get(ts);
                if (ct == null) {
                    ct = new EntryCount(ts);
                    tsCount.put(ts, ct);
                }
                ct.increment();
            }
        }

        ArrayList<String> tss = new ArrayList<String>(tsCount.keySet());
        Collections.sort(tss);
        currTrTag = "<tr>\n";
        for (String ts : tss) {
            out.println(currTrTag + "<th>" + tsCount.get(ts).count + "</th><td>" + ts + "</td></tr>");
            currTrTag = flipRowTag(currTrTag);
        }
        out.println("</Table>");
        
        out.println("<H3>Details</H3>");
        Collections.sort(entries, new RTComparator());
        out.println("<Table border='1'><thead><tr><th>Time Taken(ms)</th><th>TimeStamp</th><th>Request</th><th>SQL Log</th></tr></thead>\n");
        currTrTag = "<tr>";
        for (LogEntry entry : entries) {
            out.println(currTrTag + "<td align=\"right\" vlaign=\"top\">" + entry.sqlRT + "</td>" + "<td>" + entry.timestamp + "</td>" + "<td>"
                + entry.request + "</td>" + "<td>" + entry.FullSQL + "</td></tr>\n");
            currTrTag = flipRowTag(currTrTag);
        }
        out.println("</Table>");
        out.println("</HTML>");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: SlowSQLAnalyzer <Report file path> <Log file1> [Log file 2] ... ");
            return;
        }

        try {
            String outFileName = args[0];
            SlowSQLAnalyzer a = new SlowSQLAnalyzer();
            for (int i = 1; i<args.length ; i++)
                a.analyze(args[i]);

            String htmlFile = outFileName + ".html";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(htmlFile)));
            a.reportHTML(out);
            out.flush();
            out.close();

            String txtFile = outFileName + ".txt";
            out = new PrintWriter(new BufferedWriter(new FileWriter(txtFile)));
            a.reportText(out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
