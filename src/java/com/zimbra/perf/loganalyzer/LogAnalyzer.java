/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite, Network Edition.
 * Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc.  All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.perf.loganalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pandurangd
 *
 */
public class LogAnalyzer {

    HashMap<String, Count> requests;
    ArrayList<String> timestamps;
    HashMap<String, Count> ua;
    HashMap<String, Count> clients;
    HashMap<String, Count> users;
    HashMap<String, Count> reqDetails;
    ArrayList<LogEntry> logEntries;
    HashMap<String, ArrayList<String>> devices;

    public static class Count {

        private long value;

        Count() {
            value = 1;
        }

        Count(long newVal) {
            value = newVal;
        }

        Count(int newVal) {
            value = newVal;
        }

        public long increment() {
            value++;
            return value;
        }

        public long value() {
            return value;
        }

        public int compareTo(Count arg0) {
            if (value < arg0.value())
                return -1;
            else if (value > arg0.value())
                return 1;
            else
                return 0;
        }

    }

    public class ValueThenKeyComparator implements Comparator<Map.Entry<String, Count>> {

        public int compare(Map.Entry<String, Count> a, Map.Entry<String, Count> b) {
            int cmp1 = b.getValue().compareTo(a.getValue());
            if (cmp1 != 0) {
                return cmp1;
            } else {
                return b.getKey().compareTo(a.getKey());
            }
        }
    }

    public class KeyThenValueComparator implements Comparator<Map.Entry<String, Count>> {

        public int compare(Map.Entry<String, Count> a, Map.Entry<String, Count> b) {
            int cmp1 = a.getKey().compareTo(b.getKey());
            if (cmp1 != 0) {
                return cmp1;
            } else {
                return a.getValue().compareTo(b.getValue());
            }
        }
    }

    public static class LogEntry {

        public String timestamp;
        public String request;
        public String user;
        public String clientip;
        public String useragent;
        public String Details;

        LogEntry(String line) {
            if (!line.contains("INFO"))
                return;

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
            secStart = secEnd + 1;// line.lastIndexOf("]")+1;
            Details = line.substring(secStart);
            if (Details.startsWith(" soap")) {
                int eind = Details.indexOf("elapsed=");
                if (eind > 0)
                    Details = Details.substring(0, eind);
                Details = Details.replace("(batch) ","");

            } else if (Details.startsWith(" mailop")) {
                String[] parts = Details.split("\\s+");
                Details = parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3] + " " + parts[4];
            } else if (Details.contains("activity")) {
                String[] parts = Details.split(",");
                String Action = null;
                for (String action : parts)
                    if (action.startsWith("\"action")) {
                        Action = action;
                        break;
                    }
                Details = " activity - " + Action;
            } else
                Details = null;
        }
    }

    LogAnalyzer(String logfile) {
        requests = new HashMap<String, Count>();
        timestamps = new ArrayList<String>();
        clients = new HashMap<String, Count>();
        ua = new HashMap<String, Count>();
        users = new HashMap<String, Count>();
        reqDetails = new HashMap<String, Count>();
        logEntries = new ArrayList<LogEntry>();
        devices = new HashMap<String, ArrayList<String>>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(logfile));
            String line = in.readLine();

            while (line != null) {
                if (line.startsWith("2012")) {
                    // System.out.println(line);
                    LogEntry le = new LogEntry(line);
                    String req = le.Details;
                    if (req != null) { // && !req.contains("home")) {
                        logEntries.add(le);
                        timestamps.add(le.timestamp);
                        Count count = requests.get(le.request);
                        if (count == null) {
                            count = new Count(0);
                            requests.put(le.request, count);
                        }
                        count.increment();

                        if (le.clientip != null) {
                            count = clients.get(le.clientip);
                            if (count == null) {
                                count = new Count(0);
                                clients.put(le.clientip, count);
                            }
                            count.increment();
                        }

                        if (le.useragent != null) {
                            //String DevName = le.useragent + "( " + le.clientip + ")";
                            String DevName = le.useragent;
                            ArrayList<String> devList = devices.get(le.user);
                            if (devList == null) {
                                devList = new ArrayList<String>();
                                devices.put(le.user, devList);
                            }
                            if (!devList.contains(DevName))
                                devList.add(DevName);

                            count = ua.get(le.useragent);
                            if (count == null) {
                                count = new Count(0);
                                ua.put(le.useragent, count);
                            }
                            count.increment();
                        }

                        if (le.Details != null) {
                            count = reqDetails.get(le.Details);
                            if (count == null) {
                                count = new Count(0);
                                reqDetails.put(le.Details, count);
                            }
                            count.increment();
                        }

                        if (le.user != null) {
                            // Count user request only if its coming as activity
                            // of Soap, to avoid duplication
                            if (le.Details != null && (le.Details.contains("soap") || le.Details.contains("activity"))) {
                                count = users.get(le.user);
                                if (count == null) {
                                    count = new Count(0);
                                    users.put(le.user, count);
                                }
                                count.increment();
                            }
                        }

                        if (le.useragent != null) {

                        }
                    }
                }
                line = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    void analyze() {

        System.out.println("Log Start at " + timestamps.get(0) + " and ended at " + timestamps.get(timestamps.size() - 1));
        System.out.println("Total Unique Users: " + users.size());
        System.out.println("Total Unique Requests: " + reqDetails.size());
        System.out.println("Total Requests: " + timestamps.size());
        System.out.println("Total Unique User Agents: " + ua.size());

        System.out.println("Total Clients: " + clients.size());

        // Unique request and their count
        System.out.println("==============================================================================");
        System.out.println("#Req\tUser");
        System.out.println("------------------------------------------------------------------------------");
        ArrayList<Map.Entry<String, Count>> list = new ArrayList<Map.Entry<String, Count>>(users.entrySet());
        Collections.sort(list, new ValueThenKeyComparator());
        for (Map.Entry<String, Count> e : list) {
            System.out.println(e.getValue().value() + "\t" + e.getKey());
        }
        System.out.println("==============================================================================");

        // # of devices and how many users
        System.out.println("==============================================================================");
        System.out.println("# Devices" + "\t" + "# Users have");
        System.out.println("------------------------------------------------------------------------------");

        int[] devs = new int[100];
        for (Map.Entry<String, ArrayList<String>> e : devices.entrySet())
            devs[e.getValue().size()]++;

        for (int i = 0; i < 100; i++)
            if (devs[i] > 0)
                System.out.println(i + "\t\t" + devs[i]);
        System.out.println("==============================================================================");

        //Get the Device Distribution
        HashMap<Integer, ArrayList<String>> devdist = new HashMap<Integer, ArrayList<String>>();
        for (Map.Entry<String, ArrayList<String>> e : devices.entrySet()) {
            ArrayList<String> currDev = e.getValue();
            Integer currCt = currDev.size();
            ArrayList<String> Alldevs = devdist.get(currCt);
            if (Alldevs == null) {
                Alldevs = new ArrayList<String>();
                devdist.put(currCt, Alldevs);
            }
            Alldevs.addAll(currDev);
        }
        for (int i = 0; i < 100; i++) {
            Integer ct = i;
            ArrayList<String> Alldevs = devdist.get(ct);
            if(Alldevs != null) {
                System.out.println("------------------------------------------------------------------------------");
                System.out.println( devs[i] + " Users have " + ct + " devices");
                System.out.println("------------------------------------------------------------------------------");
                HashMap<String, Count> dct = new HashMap<String, Count>();
                for( String dev:Alldevs) {
                    Count devct = dct.get(dev);
                    if( devct == null) {
                        devct = new Count(0);
                        dct.put(dev, devct);
                    }
                    devct.increment();
                }
                list = new ArrayList<Map.Entry<String, Count>>(dct.entrySet());
                Collections.sort(list, new ValueThenKeyComparator());
                System.out.println( "#users \tDevice Name");
                for (Map.Entry<String, Count> e : list) {
                    System.out.println(e.getValue().value() + "\t" + e.getKey());
                }
                System.out.println("------------------------------------------------------------------------------");
            }
        }


        // Uniq request and their count
        System.out.println("==============================================================================");
        System.out.println("#Req\tRequest");
        System.out.println("------------------------------------------------------------------------------");
        list = new ArrayList<Map.Entry<String, Count>>(reqDetails.entrySet());
        Collections.sort(list, new ValueThenKeyComparator());
        for (Map.Entry<String, Count> e : list) {
            System.out.println(e.getValue().value() + "\t" + e.getKey());
        }
        System.out.println("==============================================================================");

        // Unique User Agents
        System.out.println("==============================================================================");
        System.out.println("#Req\tFrom Device");
        System.out.println("------------------------------------------------------------------------------");
        list = new ArrayList<Map.Entry<String, Count>>(ua.entrySet());
        Collections.sort(list, new ValueThenKeyComparator());
        for (Map.Entry<String, Count> e : list) {
            System.out.println(e.getValue().value() + "\t" + e.getKey());
        }
        System.out.println("==============================================================================");
        printRequestsPerHr();
        printActiveUsersPerHr();

    }

    public void printRequestsPerHr() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
        try {
            long[] hrs = new long[24];
            for (String ts : timestamps) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(df.parse(ts));
                // System.out.println(cal.get(Calendar.MINUTE));
                hrs[cal.get(Calendar.HOUR_OF_DAY)]++;
            }
            System.out.println("Hr" + "\t" + "# Reqs");
            long sum = 0;
            for (int i = 0; i < 24; i++) {
                System.out.println(i + "\t" + hrs[i]);
                sum += hrs[i];
            }
            System.out.println("Avg Reqs per Hr" + "\t" + sum / 24);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void printActiveUsersPerHr() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
        try {

            ArrayList<ArrayList<String>> hrsUsers = new ArrayList<ArrayList<String>>();
            for (int i = 0; i < 24; i++)
                hrsUsers.add(new ArrayList<String>());

            for (LogEntry le : logEntries) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(df.parse(le.timestamp));
                int hr = cal.get(Calendar.HOUR_OF_DAY);
                ArrayList<String> hrUsers = hrsUsers.get(hr);
                String user = le.user;
                if (!hrUsers.contains(user))
                    hrUsers.add(user);
            }

            System.out.println("Hr" + "\t" + "# Active Users");
            long sum = 0;
            for (int i = 0; i < 24; i++) {
                int au = hrsUsers.get(i).size();
                System.out.println(i + "\t" + au);
                sum += au;
            }
            System.out.println("Avg active users per Hr" + "\t" + sum / 24);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: LogAnalyzer <Log file> ");
            return;
        }
        try {
            String inFileName = args[0];
            LogAnalyzer a = new LogAnalyzer(inFileName);
            a.analyze();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

}
