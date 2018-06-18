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

/**
 * @author pandurangd
 * 
 */
public class ExceptionsAnalyzer {

    public static class CallStacks {

        public String key;
        public long count;
        public ArrayList<String> stack;

        CallStacks(String n, ArrayList<String> cs) {
            stack = cs;
            key = n;
            count = 1;
        }

        void incrementCount() {
            count = count + 1;
        }
    }

    ArrayList<CallStacks> exclude;

    void addExcludeException(ArrayList<String> cs) {
        trimCallStack(cs);
        if (exclude == null)
            exclude = new ArrayList<CallStacks>();
        exclude.add(new CallStacks(cs.get(0), cs));
    }

    void initExclude(String fileName) {
        boolean inException = false;
        ArrayList<String> CurrCallstack = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String line = in.readLine();
            while (line != null) {

                if (line.startsWith("\tat ") && inException) {
                    CurrCallstack.add(line);
                } else {
                    if (inException) {
                        addExcludeException(CurrCallstack);
                        CurrCallstack = null;
                    }
                    inException = false;
                    if (line.contains("Exception")) {
                        inException = true;
                        line = removeLastNumber(line);
                        CurrCallstack = new ArrayList<String>();
                        CurrCallstack.add(line);
                    }
                }

                line = in.readLine();
            }
            if (CurrCallstack != null) {
                addExcludeException(CurrCallstack);
                CurrCallstack = null;
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        if (exclude != null) {
            for (CallStacks cs : exclude) {
                System.out.println("Exclude has " + cs.stack.get(0));
            }

        } else
            System.out.println("Exclude call stack is empty");

    }

    boolean isExcluded(ArrayList<String> callstack) {
        // Exclude call stack is empty
        if (exclude == null || exclude.size() == 0)
            return false;
        int exlSize = exclude.size();
        // Check all call stacks
        for (int i = 0; i < exlSize; i++) {
            ArrayList<String> cs1 = exclude.get(i).stack;
            ArrayList<String> cs2 = callstack;
            int s1 = cs1.size();
            int s2 = callstack.size();
            if (s1 != s2)
                continue;
            // reset matched for every call stack
            boolean matched = true;
            for (int j = 0; j < s1; j++) {
                if (cs1.get(j).compareTo(cs2.get(j)) != 0) {
                    matched = false;
                    break;
                } else {
                    System.out.println("matched one line");
                }
            }
            // Found exact match, stop searching
            if (matched) {
                System.out.println("Excluded " + callstack.get(0));
                return true;
            }
        }
        return false;
    }

    public static class zException {

        public String name;
        public Integer count;
        HashMap<String, CallStacks> callstacks;
        ArrayList<Integer> csCount;

        zException(String n) {
            name = n;
            count = new Integer(0);
            callstacks = new HashMap<String, CallStacks>();
            csCount = new ArrayList<Integer>();
        }

        int matchCallStack(ArrayList<String> c1, ArrayList<String> c2) {
            int s1 = c1.size();
            int s2 = c2.size();
            int cmpFrame = (s1 < s2) ? s1 : s2;
            for (int i = 0; i < cmpFrame; i++)
                if (c1.get(i).compareTo(c2.get(i)) != 0)
                    return i;
            return 0;
        }

        void addCallstack(ArrayList<String> callstack) {
            if (callstacks == null) {
                callstacks = new HashMap<String, CallStacks>();
            }

            ArrayList<CallStacks> cs = new ArrayList<CallStacks>(callstacks.values());
            int numCallstacks = cs.size();
            int matchExp = -1;

            for (int i = 0; i < numCallstacks; i++) {
                if (matchCallStack(callstack, cs.get(i).stack) == 0) {
                    matchExp = i;
                    break;
                }
            }

            if (matchExp == -1) {
                String key = callstack.get(0);
                if (cs.size() > 0) {
                    for (int i = 0; i < numCallstacks; i++) {
                        int misindex = matchCallStack(callstack, cs.get(i).stack);
                        key = callstack.get(misindex);
                        if (!callstacks.containsKey(key))
                            break;
                    }
                }

                CallStacks newCS = new CallStacks(key, callstack);
                callstacks.put(key, newCS);

            } else {
                CallStacks matchCS = cs.get(matchExp);
                matchCS.incrementCount();
            }
            count++;
        }

        public String toString() {
            String str = null;
            str = "<Exception name=\"" + name + "\" count = \"" + count + "\"> <Callstacks>";
            // int numCS = callstacks.size();
            for (Map.Entry<String, CallStacks> cs : callstacks.entrySet()) {
                CallStacks callstack = cs.getValue();
                str = str + "<Callstack name=\"" + callstack.stack.get(0) + "--" + callstack.key + "\" count = \"" + callstack.count + "\" >\n";
                int numFrm = callstack.stack.size();
                for (int j = 0; j < numFrm; j++) {
                    str = str + "<Frame><![CDATA[" + callstack.stack.get(j) + "]]></Frame>\n";
                }
                str = str + "</Callstack>\n";
            }
            str = str + "</Callstacks>\n</Exception>";
            return str;

        }
    }

    public class CallstackSortByCount implements Comparator<CallStacks> {

        public int compare(CallStacks o1, CallStacks o2) {
            if (o2.count == o1.count)
                return 0;
            else if (o2.count < o1.count)
                return -1;
            else
                return 1;
        }
    }

    public class ExpSortByCount implements Comparator<zException> {

        public int compare(zException o1, zException o2) {
            return o2.count.compareTo(o1.count);
        }
    }

    HashMap<String, zException> exceptions;
    ArrayList<Integer> count;
    ArrayList<Integer> sortedcount;

    void trimCallStack(ArrayList<String> cs) {
        String ignore = "\tat org.eclipse.jetty";
        int numFrm = cs.size();
        cs.remove(numFrm - 1);
        for (int i = numFrm - 2; i > 0; i--) {
            if (cs.get(i).startsWith(ignore))
                cs.remove(i);
            else
                break;
        }
    }

    void addException(ArrayList<String> callstack) {
        trimCallStack(callstack);
        if (isExcluded(callstack))
            return;

        if (callstack == null || callstack.size() == 0) {
            System.out.println("addException got called on empty call stack");
            return;
        }

        String expName = callstack.get(0);
        zException exp = exceptions.get(expName);
        if (exp == null) {
            exp = new zException(expName);
            exceptions.put(expName, exp);
        }
        exp.addCallstack(callstack);
    }

    ExceptionsAnalyzer() {
        exceptions = new HashMap<String, zException>();
    }
    
    public void analyze (String logfile) {

        boolean inException = false;
        ArrayList<String> CurrCallstack = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(logfile));
            String line = in.readLine();
            while (line != null) {

                if (line.startsWith("201")) {
                    inException = false;
                    if (CurrCallstack != null) {
                        addException(CurrCallstack);
                        CurrCallstack = null;
                    }
                } else {
                    if (inException) {
                        if (!(line.startsWith("ExceptionId") || line.startsWith("Code:")))
                            CurrCallstack.add(line);
                    } else {
                        if (line.contains("Exception") && !(line.startsWith("ExceptionId"))) {
                            inException = true;
                            line = removeLastNumber(line);
                            CurrCallstack = new ArrayList<String>();
                            CurrCallstack.add(line);
                        }
                    }
                }
                line = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    String removeLastNumber(String str) {
        int l = str.lastIndexOf(" ");
        if (l == -1)
            return str;
        String lastWord = str.substring(l);
        if (lastWord.matches(".*[0-9].*"))
            return str.substring(0, l);
        else
            return str;
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
            newTag = tag.replace("class='odd'", "");
        else
            newTag = tag.replace("tr", "tr class='odd'");
        return newTag;
    }

    void reportHTML(PrintWriter out) {

        String summary = "";
        String details = "";
        summary = "<Table style='border:1px solid #000; border-collapse:collapse;'><thead><tr><th align='right'>Count</th><th>Name</th></tr></thead>";
        details = "<Table border='1'><thead><tr><th>Count</th><th>Callstack</th></tr></thead>\n";
        int detailsCount = 1;
        ArrayList<zException> expList = new ArrayList<zException>(exceptions.values());
        Collections.sort(expList, new ExpSortByCount());
        out.println("<Exceptions>");
        String currSummTrTag = "<tr>\n";
        String currDetTrTag = "<tr>\n";
        int idiffCSTable = 1;
        for (zException expCount : expList) {
            int numCallstacks = expCount.callstacks.size();
            String diffcsTID = "diffCS_" + idiffCSTable++;

            if (numCallstacks > 1) {
                currSummTrTag = flipRowTag(currSummTrTag);
                summary = summary + currSummTrTag + "\t<th scope=\"row\" align='right' valign='top' >" + expCount.count + "</td>\n" + "\t<td>"
                    + expCount.name + "&nbsp;&nbsp;<a href=\"#\" id=\"" + diffcsTID + "-show\" onclick=\"showHide('" + diffcsTID
                    + "');return false;\">Different Call stacks</a>";
                ArrayList<CallStacks> lCS = new ArrayList<CallStacks>(expCount.callstacks.values());
                Collections.sort(lCS, new CallstackSortByCount());

                String diffCSTable = "<div  style='display:none;' id=\"" + diffcsTID + "\">" + "<a href=\"#\" id=\"" + diffcsTID
                    + "-show\" onclick=\"showHide('" + diffcsTID + "');return false;\">Hide Details</a><br/>" + "<table>";
                for (CallStacks callstack : lCS) {
                    currSummTrTag = flipRowTag(currSummTrTag);
                    diffCSTable = diffCSTable + currSummTrTag + "\t<td align='right'><a href='#Details_" + detailsCount + "'>" + callstack.count
                        + "</a></td>\n" + "\t<td >" + callstack.stack.get(0) + "--" + callstack.key + "</td>\n" + "</tr>";
                    ArrayList<String> stack = callstack.stack;
                    int numFrm = stack.size();
                    currDetTrTag = flipRowTag(currDetTrTag);
                    details = details + currDetTrTag + "<td align='right' valign='top'>" + callstack.count + "</td>\n<td>";
                    String frms = "<a name='Details_" + detailsCount++ + "'/>";
                    for (int i = 0; i < numFrm; i++) {
                        frms = frms + stack.get(i) + "</br>";
                    }
                    frms = frms + "</div>";
                    details = details + frms + "</td>\n</tr>\n";
                    diffCSTable = diffCSTable + "</tr>";
                }
                diffCSTable = diffCSTable + "</table>";
                summary = summary + "<div> " + diffCSTable + "</div></td>\n" + "</tr>";

            } else {
                currSummTrTag = flipRowTag(currSummTrTag);
                summary = summary + currSummTrTag + "\t<td align='right' valign='top' ><a href='#Details_" + detailsCount + "'>" + expCount.count
                    + "</a></td>\n" + "\t<td colspan='2'>" + expCount.name + "</td>\n" + "</tr>";
                for (Map.Entry<String, CallStacks> cs : expCount.callstacks.entrySet()) {
                    CallStacks callstack = cs.getValue();
                    ArrayList<String> stack = callstack.stack;
                    int numFrm = stack.size();
                    currDetTrTag = flipRowTag(currDetTrTag);
                    details = details + currDetTrTag + "<td align='right' valign='top'>" + callstack.count + "</td>\n<td>";
                    String frms = "<a name='Details_" + detailsCount++ + "'/>";
                    for (int i = 0; i < numFrm; i++) {
                        frms = frms + stack.get(i) + "</br>";
                    }
                    frms = frms + "</div>";
                    details = details + frms + "</td>\n</tr>\n";
                }
            }
        }
        out.println("</Exceptions>");
        summary = summary + "</Table>";
/*
        for (zException expCount : expList) {
            System.out.println(expCount.count + "\t" + expCount.name);
        }
*/
        out.println("<HTML>");
        writeStyle(out);
        out.println("<H3>Summary</H3>");

        out.println(summary);

        out.println("<H3>Details</H3>");

        out.println(details);
        out.println("</HTML>");
    }

    void reportText(PrintWriter out) {

        out.println("Summary");
        out.println("-------");
        out.println("");
        out.printf("%9s   %s\n", "Count", "Name");
        ArrayList<zException> expList = new ArrayList<zException>(exceptions.values());
        Collections.sort(expList, new ExpSortByCount());
        for (zException expCount : expList) {
            out.printf("%9d   %s\n", expCount.count ,expCount.name);
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ExceptionAnalyzer <Report file path> <Log file1> [Log file 2] ... ");
            return;
        }
        try {
            String outFileName = args[0];
            
            ExceptionsAnalyzer a = new ExceptionsAnalyzer();
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
