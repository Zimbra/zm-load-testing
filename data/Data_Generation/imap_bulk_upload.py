#!/usr/bin/env python3
# imap_bulk_upload.py
# Mirrors zm-load-testing/upload_unique_eml.sh using non-SSL IMAP
# Creates folders and uploads .eml files per user in parallel
import imaplib
import os
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from threading import Lock

CONNECTIONS_PER_USER = 10      # Parallel IMAP connections per user
MAX_USERS_PARALLEL = 4         # Parallel users
PRELOAD_FILES = True           # Read all .eml files into memory first

HOST = "proxy.xyz.com"
PASSWORD = "password"
PORT = 143
DOMAIN = "domain.xyz.com"
DATA_ROOT = "/opt/zimbra/dataCreation"   # Root directory with user folders
MAX_WORKERS = 4                # Parallel users (same as MAX_JOBS in shell script)
PRELOAD_FILES = True  
LOG_FILE = "/opt/zimbra/upload.log"

# ──────────────────────────────────────────────
lock = Lock()
def log(message):
    timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
    line = "[%s] %s" % (timestamp, message)
    print(line)
    with open(LOG_FILE, "a") as f:
        f.write(line + "\n")
def create_connection(user_email):
    """Create and return a single IMAP connection."""
    conn = imaplib.IMAP4(HOST, PORT)
    conn.login(user_email, PASSWORD)
    return conn
def upload_batch(user_email, batch, batch_id, counters):
    """
    Upload a batch of (filepath, folder, filename, preloaded_data) tuples
    using a single dedicated IMAP connection.
    """
    local_success = 0
    local_failed = 0
    try:
        conn = create_connection(user_email)
        for filepath, dest_folder, fname, msg_data in batch:
            try:
                # If not preloaded, read now
                if msg_data is None:
                    with open(filepath, "rb") as f:
                        msg_data = f.read()
                status, response = conn.append(dest_folder, None, None, msg_data)
                if status == "OK":
                    local_success += 1
                else:
                    local_failed += 1
            except imaplib.IMAP4.abort:
                # Connection dropped — reconnect and retry this message
                try:
                    conn = create_connection(user_email)
                    status, response = conn.append(dest_folder, None, None, msg_data)
                    if status == "OK":
                        local_success += 1
                    else:
                        local_failed += 1
                except Exception:
                    local_failed += 1
            except Exception as e:
                local_failed += 1
        try:
            conn.logout()
        except Exception:
            pass
    except Exception as e:
        local_failed += len(batch)
        log("  Batch %d connection error: %s" % (batch_id, e))
    with lock:
        counters["success"] += local_success
        counters["failed"] += local_failed
    return local_success, local_failed
def upload_user(username):
    """Process a single user with multiple parallel IMAP connections."""
    local_part = username.split("@")[0]
    user_email = "%s@%s" % (local_part, DOMAIN)
    user_dir = os.path.join(DATA_ROOT, username)
    if not os.path.isdir(user_dir):
        log("[%s] No data directory found, skipping" % username)
        return {"user": username, "success": 0, "failed": 0, "folders": 0, "total": 0}
    log("-> Starting upload for %s" % user_email)
    # ── Phase 1: Create folders (single connection) ──
    try:
        conn = create_connection(user_email)
    except Exception as e:
        log("[%s] CONNECTION FAILED: %s" % (username, e))
        return {"user": username, "success": 0, "failed": 0, "folders": 0, "error": str(e)}
    folder_list = []
    for dirpath, dirnames, filenames in os.walk(user_dir):
        if dirpath == user_dir:
            continue
        rel_folder = os.path.relpath(dirpath, user_dir)
        folder_list.append(rel_folder)
    folder_list.sort()
    folders_created = 0
    for folder_name in folder_list:
        try:
            status, response = conn.create(folder_name)
            folders_created += 1
        except Exception:
            folders_created += 1  # Already exists is OK
    try:
        conn.logout()
    except Exception:
        pass
    log("  [%s] Phase 1 complete: %d folders" % (username, folders_created))
    # ── Phase 2: Collect all .eml files ──
    eml_files = []
    for dirpath, dirnames, filenames in os.walk(user_dir):
        for fname in sorted(filenames):
            if fname.endswith(".eml"):
                full_path = os.path.join(dirpath, fname)
                rel_dir = os.path.relpath(dirpath, user_dir)
                # PRELOAD: Read file into memory to avoid disk I/O during upload
                if PRELOAD_FILES:
                    with open(full_path, "rb") as f:
                        msg_data = f.read()
                else:
                    msg_data = None
                eml_files.append((full_path, rel_dir, fname, msg_data))
    total_files = len(eml_files)
    log("  [%s] Phase 2: Uploading %d files using %d parallel connections..." % (
        username, total_files, CONNECTIONS_PER_USER))
    # ── Phase 2: Split files into batches, one per connection ──
    batches = []
    batch_size = (total_files + CONNECTIONS_PER_USER - 1) // CONNECTIONS_PER_USER
    for i in range(0, total_files, batch_size):
        batches.append(eml_files[i:i + batch_size])
    counters = {"success": 0, "failed": 0}
    upload_start = time.time()
    # Launch parallel connections
    with ThreadPoolExecutor(max_workers=CONNECTIONS_PER_USER) as executor:
        futures = {
            executor.submit(upload_batch, user_email, batch, idx, counters): idx
            for idx, batch in enumerate(batches)
        }
        for future in as_completed(futures):
            batch_idx = futures[future]
            s, f = future.result()
            elapsed = time.time() - upload_start
            rate = counters["success"] / elapsed if elapsed > 0 else 0
            log("  [%s] Batch %d done: +%d ok, +%d fail | Total: %d/%d (%.1f msgs/sec)" % (
                username, batch_idx, s, f, counters["success"], total_files, rate))
    elapsed = time.time() - upload_start
    rate = counters["success"] / elapsed if elapsed > 0 else 0
    log("  [%s] COMPLETE: %d/%d uploaded in %.1fs (%.1f msgs/sec)" % (
        username, counters["success"], total_files, elapsed, rate))
    return {
        "user": username,
        "success": counters["success"],
        "failed": counters["failed"],
        "folders": folders_created,
        "total": total_files,
        "time": elapsed,
        "rate": rate
    }
# ──────────────────────────────────────────────
# MAIN
# ──────────────────────────────────────────────
if __name__ == "__main__":
    log("=== FAST IMAP Bulk Upload ===")
    log("Host: %s:%d | Connections/user: %d | Preload: %s" % (
        HOST, PORT, CONNECTIONS_PER_USER, PRELOAD_FILES))
    users = sorted([
        d for d in os.listdir(DATA_ROOT)
        if os.path.isdir(os.path.join(DATA_ROOT, d))
    ])
    if not users:
        log("ERROR: No user directories in %s" % DATA_ROOT)
        sys.exit(1)
    log("Found %d users: %s" % (len(users), ", ".join(users)))
    overall_start = time.time()
    results = []
    with ThreadPoolExecutor(max_workers=MAX_USERS_PARALLEL) as executor:
        futures = {executor.submit(upload_user, u): u for u in users}
        for future in as_completed(futures):
            try:
                results.append(future.result())
            except Exception as e:
                log("UNEXPECTED ERROR: %s" % e)
    # ── Summary ──
    overall_elapsed = time.time() - overall_start
    total_success = sum(r.get("success", 0) for r in results)
    total_failed = sum(r.get("failed", 0) for r in results)
    total_messages = sum(r.get("total", 0) for r in results)
    log("")
    log("=" * 60)
    log("  UPLOAD SUMMARY")
    log("=" * 60)
    log("  Users:       %d" % len(results))
    log("  Uploaded:    %d / %d" % (total_success, total_messages))
    log("  Failed:      %d" % total_failed)
    log("  Total time:  %.1f seconds" % overall_elapsed)
    log("  Overall rate: %.1f msgs/sec" % (
        total_success / overall_elapsed if overall_elapsed > 0 else 0))
    log("=" * 60)
    for r in sorted(results, key=lambda x: x["user"]):
        log("  %-40s %6d/%6d  %.1f msgs/sec" % (
            r["user"], r.get("success", 0), r.get("total", 0), r.get("rate", 0)))
    if total_failed > 0:
        sys.exit(1)
