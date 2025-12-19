#!/bin/bash
# Fast .eml uploader for Zimbra using a single zmmailbox session per user

DATA_ROOT="/opt/zimbra/dataCreation"
USERLIST="$DATA_ROOT/userlist.txt"
LOGFILE="/opt/zimbra/upload.log"
TMP_DIR="/opt/zimbra/tmp"       # temporary files directory
MAX_JOBS=4                      # number of users processed in parallel

# Ensure tmp directory exists
mkdir -p "$TMP_DIR"
chown zimbra:zimbra "$TMP_DIR"
chmod 700 "$TMP_DIR"

echo "=== Starting upload process at $(date) ===" | tee -a "$LOGFILE"

upload_user() {
    USER="$1"
    USER_DIR="${DATA_ROOT}/${USER}"
    [[ ! -d "$USER_DIR" ]] && echo "[!] No data directory for $USER" | tee -a "$LOGFILE" && return

    echo "[$(date)] -> Starting upload for $USER" | tee -a "$LOGFILE"

    TMP_CMD=$(mktemp "$TMP_DIR/tmp.${USER}.XXXXXX")
    chown zimbra:zimbra "$TMP_CMD"
    chmod 600 "$TMP_CMD"

    # ---- Phase 1: Create folders ----
    find "$USER_DIR" -type d ! -path "$USER_DIR" | sort | while read -r FOLDER_PATH; do
        REL_FOLDER="${FOLDER_PATH#$USER_DIR/}"
        FOLDER_NAME="/${REL_FOLDER#/}"
        echo "createFolder \"$FOLDER_NAME\"" >> "$TMP_CMD"
    done

    # ---- Phase 2: Add messages ----
    find "$USER_DIR" -type f -name "*.eml" | sort | while read -r MAILFILE; do
        REL_PATH="${MAILFILE#$USER_DIR/}"
        DEST_FOLDER="/$(dirname "$REL_PATH")"
        echo "addMessage \"$DEST_FOLDER\" \"$MAILFILE\"" >> "$TMP_CMD"
    done

    echo "exit" >> "$TMP_CMD"

    echo "--- zmmailbox commands for $USER ---" >> "$LOGFILE"
    cat "$TMP_CMD" >> "$LOGFILE"

    # Run the commands as zimbra
    su - zimbra -c "zmmailbox -z -m \"$USER\" < \"$TMP_CMD\"" >> "$LOGFILE" 2>&1
    RC=$?

    rm -f "$TMP_CMD"

    if [[ $RC -eq 0 ]]; then
        echo "[$(date)] Finished $USER successfully" | tee -a "$LOGFILE"
    else
        echo "[$(date)] Failed uploading for $USER (code $RC)" | tee -a "$LOGFILE"
    fi
}
# ---- Parallel processing (up to MAX_JOBS) ----
CURRENT=0
while read -r USER; do
    [[ -z "$USER" ]] && continue
    upload_user "$USER" &
    ((CURRENT++))
    if (( CURRENT >= MAX_JOBS )); then
        wait
        CURRENT=0
    fi
done < "$USERLIST"

# Wait for remaining jobs
wait

echo "=== Completed all uploads at $(date) ===" | tee -a "$LOGFILE"
