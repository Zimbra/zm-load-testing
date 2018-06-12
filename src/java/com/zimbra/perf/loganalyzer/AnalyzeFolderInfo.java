/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite, Network Edition.
 * Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc.  All Rights Reserved.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.perf.loganalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pandurangd
 *
 */
public class AnalyzeFolderInfo {

    HashMap<String, ArrayList<FolderInfo>> mFolders;
    HashMap<String, HashMap<String, ArrayList<String>>> shares;

    public static class FolderInfo {

        public String id;
        public String view;
        public int unread;
        public int msgCount;
        public String path;
        public String rmOwner;
        public String rmId;

        FolderInfo(String line) {
            int si = 0;
            int ei = 10;

            // Id
            si = 0; ei = si + 10;
            id = line.substring(si, 10).trim();
            // View
            si = ei + 2; ei = si + 4;
            view = line.substring(si, ei).trim();
            // Unread
            si = ei + 2; ei = si + 10;
            unread = Integer.parseInt(line.substring(si, ei).trim());
            // Msg Count
            si = ei + 2; ei = si + 10;
            msgCount = Integer.parseInt(line.substring(si, ei).trim());
            // Path
            si = ei + 2;
            path = line.substring(si).trim();
            if (path.endsWith(")") && path.contains(".com:")) {
                // Its Mountpoint
                si = line.lastIndexOf("(") + 1;
                ei = line.indexOf(":", si);
                rmOwner = line.substring(si, ei);
                si = ei + 1;
                ei = line.indexOf(")", si);
                rmId = line.substring(si, ei);
            }
        }

        public String toString() {
            return "id=" + id + " view=" + view + " unread=" + unread + " msgCount=" + msgCount + " path=" + path + " rmOwner=" + rmOwner + " rmId="
                + rmId;
        }
    }

    public void updateSharesInfo(String uid, FolderInfo fi) {

        // Get the Owner's shared folder list
        HashMap<String, ArrayList<String>> userShares = shares.get(fi.rmOwner);
        if (userShares == null) {
            userShares = new HashMap<String, ArrayList<String>>();
            shares.put(fi.rmOwner, userShares);
        }
        // See if this folder is already in his list, or this is the first user
        ArrayList<String> sharedFolder = userShares.get(fi.rmId);
        if (sharedFolder == null) {
            sharedFolder = new ArrayList<String>();
            userShares.put(fi.rmId, sharedFolder);
        }

        // Add ourself to the list of users of shared folder of remoteOwner
        sharedFolder.add(uid);
    }

    AnalyzeFolderInfo(String logFile) {
        mFolders = new HashMap<String, ArrayList<FolderInfo>>();
        shares = new HashMap<String, HashMap<String, ArrayList<String>>>();

        String currUser = null;
        ArrayList<FolderInfo> currUserFolders = null;

        try {
            BufferedReader in = new BufferedReader(new FileReader(logFile));
            String line = in.readLine();

            while (line != null) {
                if (line.startsWith("User:")) {
                    currUser = line.substring(line.indexOf(":") + 1);
                    currUser = currUser.trim();
                    currUserFolders = mFolders.get(currUser);
                    if (currUserFolders == null) {
                        currUserFolders = new ArrayList<FolderInfo>();
                        mFolders.put(currUser, currUserFolders);
                    }
                    // Throw away header line
                    in.readLine();
                    in.readLine();
                } else if (line.contains("Briefcase")) {
                    FolderInfo fi = new FolderInfo(line);
                    if (fi.rmOwner != null) {
                        updateSharesInfo(currUser, fi);
                    }
                    //System.out.println(fi);
                    currUserFolders.add(fi);
                }
                line = in.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    public void analyze() {
        //Print Folders Info
        System.out.println("User,#folder,#Documents");
        for(Map.Entry<String, ArrayList<FolderInfo>> user:mFolders.entrySet()) {
            ArrayList<FolderInfo> folders = user.getValue();
            int TotalDocs = 0;
            for(FolderInfo folder:folders) {
                TotalDocs = TotalDocs + folder.msgCount;
            }
            System.out.println(user.getKey() + "," + folders.size() + "," + TotalDocs);
        }

        //Print Share Info
        System.out.println("User,#shared Folder,Shared with # users");
        for(Map.Entry<String, HashMap<String, ArrayList<String>>> user:shares.entrySet()) {
            HashMap<String, ArrayList<String>> usrfolders = user.getValue();
            String susers = "";
            for(Map.Entry<String, ArrayList<String>> folder:usrfolders.entrySet()) {
                ArrayList<String> sharedWith = folder.getValue();
                susers = susers + sharedWith.size() + ",";
            }
            System.out.println(user.getKey() + "," + usrfolders.size() + "," + susers);
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: LogAnalyzer <Log file> ");
            return;
        }
        try {
            String inFileName = args[0];
            AnalyzeFolderInfo a = new AnalyzeFolderInfo(inFileName);
            a.analyze();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

}
