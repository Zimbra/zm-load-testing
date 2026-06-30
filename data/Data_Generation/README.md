# dynamic_data_generation_with_attachments.py

 This script generates multiple unique user mailboxes with realistic .eml test data 
 with attachments, calendar invites, and configurable folder structure and sizes — ideal for testing

---

##  Features
- Generates `.eml` files to simulate realistic mailboxes
- Supports a configurable percentage of attachments
- Simulates calendar invites (iCalendar format)
- Automatically creates folder hierarchy (Inbox, Sent, Subfolders)
- Specify total mailbox size and average email size
- Optional cleanup of output directory before generation

---

## Requirements
- Python 3.6+
- `faker` Python package
- Make sure to have zimbra user accounts created before running script.
- Make sure to edit below values(highlighted in bold) in the script before running it:
  - parser.add_argument("--user-prefix", type=str, default="user", help="Prefix for generated usernames")
  - parser.add_argument("--domain", type=str, default="example.com ", help="Domain for generated users")

## Install dependencies:
```
pip install faker
```

## Usage
```
python3 dynamic_data_generation_with_attachments.py -h
usage: dynamic_data_generation_with_attachments.py [-h] [--users USERS] [--user-prefix USER_PREFIX] [--domain DOMAIN] [--output-base OUTPUT_BASE] [--clean] [--size SIZE] [--attachment-percent ATTACHMENT_PERCENT]
                                                   [--attachment-type-dist ATTACHMENT_TYPE_DIST] [--calendar-percent CALENDAR_PERCENT] [--avg-msg-size AVG_MSG_SIZE] [--min-attachments MIN_ATTACHMENTS]

Generate multiple unique user mailboxes with realistic .eml test data.

options:
  -h, --help            show this help message and exit
  --users USERS         Number of users/mailboxes to generate
  --user-prefix USER_PREFIX
                        Prefix for generated usernames
  --domain DOMAIN       Domain for generated users
  --output-base OUTPUT_BASE
                        Base output directory
  --clean               Clean the output base directory before generating
  --size SIZE           Target total mailbox size per user (e.g., '100MB')
  --attachment-percent ATTACHMENT_PERCENT
                        Percent of messages that have attachments (Option B probabilistic)
  --attachment-type-dist ATTACHMENT_TYPE_DIST
                        Weighted distribution for attachment types. Example: "pdf=30,jpg=10,png=10,docx=20,txt=10,ppt=10,xlsx=10" (you may also use ppt,xlsx as long as total = 100)
  --calendar-percent CALENDAR_PERCENT
                        Percent of messages that are calendar invites
  --avg-msg-size AVG_MSG_SIZE
                        Average message size in KB
  --min-attachments MIN_ATTACHMENTS
                        (Optional) Ensure at least this many attachments per mailbox

Example:
  python3 dynamic_data_generation_with_attachements.py --users 100 --size 30MB --output-base /tmp/eml --clean
```

### Options
|Option	              | Description
|---------------------|----------------------------------------------------------------------------|
|--size	              | Total target mailbox size (e.g. 100MB, 1GB) (default: 30MB)                |
|--attachment-percent |	% of emails that should contain attachments (default: 2)     
|--attachment-type-dist| % of types of attachments like pdf,jpg,png,docx,txt,ppt,xlsx
|--calendar-percent   |	% of emails that should be calendar invites (default: 1)                   |
|--avg-msg-size	      | Average size of each email in KB (default: 60)                             |
|--output-dir	      | Output directory for generated .eml files (default: ~/eml_output_testdata) |
|--clean	      | Clean output directory before generation (optional flag)                   |

## Folder Structure
Generated emails are distributed across:
```
Inbox/
Inbox/Subfolder1/
Inbox/Subfolder2/
Inbox/Subfolder3/
Inbox/Subfolder4/
Sent/
```

## Output Summary
At the end, the script reports:
```
- Total emails generated
- Final size in MB/GB
- Output folder path
- Folder count
```


## Notes
```
- File names follow the format: message_0000001.eml, message_0000002.eml, ...
- Calendar invites use basic iCalendar (VEVENT) format.
- Email attachment types include formats like pdf,jpg,png,docx and txt.
- Attachments are with realistic MIME types.
- This script will generate .eml files under a folder structure which can further be populated 
  with script upload_unique_eml.sh
```


# upload_unique_eml.sh

This script uploads data created in previous script to respective user mailboxes

---

## Requirements
- Change the path in script to point to folder generated in Script#1
  - DATA_ROOT="/opt/zimbra/dataCreation"
- Create below folders/files if there is any permission issues:
  - /opt/zimbra/upload.log
  - /opt/zimbra/tmp


## Usage
```
bash upload_unique_eml.sh
```

# duplicate_mail_cal_data.py

This will generate and upload duplicate data on user mailbox accounts.

---


## Requirements
- Make sure to have from_users.txt and to_users.txt files created having sender and recipient user mail addresses.


## Usage
```
python3 duplicate_mail_cal_data.py -h
usage: duplicate_mail_cal_data.py [-h] --from-users FROM_USERS --to-users TO_USERS --dup-mail-mb DUP_MAIL_MB [--dup-cal-mb DUP_CAL_MB] [--attachment-percent ATTACHMENT_PERCENT] [--attachment-type-dist ATTACHMENT_TYPE_DIST]
                                  [--attachment-kb ATTACHMENT_KB] [--avg-msg-size AVG_MSG_SIZE] [--smtp-host SMTP_HOST] [--smtp-port SMTP_PORT]

Deterministic duplicate sender (Mode A1: each sender -> ALL recipients)

options:
  -h, --help            show this help message and exit
  --from-users FROM_USERS
                        Path to from_users.txt (one sender per line)
  --to-users TO_USERS   Path to to_users.txt (one recipient per line)
  --dup-mail-mb DUP_MAIL_MB
                        Per-sender target duplicate mail MB
  --dup-cal-mb DUP_CAL_MB
                        Per-sender target duplicate calendar MB
  --attachment-percent ATTACHMENT_PERCENT
                        Percent of messages that include attachment
  --attachment-type-dist ATTACHMENT_TYPE_DIST
                        Weighted distribution for attachment types (csv: ext=weight,...)
  --attachment-kb ATTACHMENT_KB
                        Fixed size (KB) for generated attachments (default 10KB)
  --avg-msg-size AVG_MSG_SIZE
                        Fixed message body size in KB (default 50KB)
  --smtp-host SMTP_HOST
                        SMTP host to send messages (optional)
  --smtp-port SMTP_PORT
                        SMTP port (default 25)
```

### Options
|Option	             | Description
|--------------------|----------------------------------------------------------------------------|
|--from-users	       | Path to from_users.txt (one sender per line)              |
|--to-users |	Path to to_users.txt (one recipient per line)    
|--dup-mail-mb| Per-sender target duplicate mail MB
|--dup-cal-mb   |	Per-sender target duplicate calendar MB               |
|--attachment-percent   | Weighted distribution for attachment types (csv: ext=weight,...)                          |
|---attachment-type-dist	      | Output directory for generated .eml files (default: ~/eml_output_testdata) |
|--avg-msg-size	      | Fixed size (KB) for generated attachments (default 10KB)            
|smtp-host | SMTP host to send messages|
smtp-port |  SMTP port (default 25)|



## Output Summary
At the end, the script reports:
```
- Total emails generated
- Final size in MB/GB
- Output folder path
```

# generate_briefcase_data.py

This script generates unique files per user with briefcase data.
The following briefcase types are in scope:
- pptx 
- txt 
- docx 
- xlsx 
- pdf 
- jpg

---

## Requirements
- Update below highlighted values before running the script:
  - MIN_SIZE_KB = <Value>  --- Min size of file
  - MAX_SIZE_KB = <Value>  --- Max size of file
  - TARGET_MB = <Value> --- Target for overall files per user in MB


## Usage
```
python3 generate_briefcase_data.py
```
# IMAP Bulk Upload Tool (`imap_bulk_upload.py`)

Fast, parallel IMAP APPEND-based bulk `.eml` uploader for Zimbra — a Python replacement for `zm-load-testing/upload_unique_eml.sh` with **~276% faster** upload times.

## Overview

This script uploads `.eml` files into Zimbra mailboxes via non-SSL IMAP APPEND. It supports:

- **Multi-user parallelism** — processes multiple users concurrently
- **Multi-connection parallelism** — opens multiple IMAP connections per user
- **Folder hierarchy preservation** — mirrors the on-disk directory structure as IMAP folders
- **File preloading** — optionally reads all `.eml` files into memory before uploading to eliminate disk I/O bottlenecks
- **Automatic reconnection** — retries on `IMAP4.abort` (connection drops)
- **Detailed logging** — per-batch progress with real-time upload rate (msgs/sec)

## Performance

| Upload Method       | Data Size                          | Time Elapsed | Storage |
|---------------------|------------------------------------|--------------|---------|
| **IMAP APPEND (this tool)** | 20 GB — 5 GB × 4 accounts, 10 conn | **30 min**   | OCI S3  |
| AddMessage (SOAP)   | 20 GB — 5 GB × 4 accounts          | 1 hr 53 min  | OCI S3      |

> ~276% improvement over AddMessage-based upload.

## Prerequisites

- **Python 3.6+** (uses `concurrent.futures`, `imaplib`, standard library only — no pip dependencies)
- Network access to the Zimbra IMAP proxy on port **143** (non-SSL)
- Zimbra User accounts created and IMAP login enabled for target accounts
- `.eml` files created using https://github.com/Zimbra/zm-load-testing/blob/master/data/Data_Generation/dynamic_data_generation_with_attachments.py with organized in a directory structure:
  ```
  <DATA_ROOT>/
  ├── user1@domain.com/
  │   ├── Inbox/
  │   │   ├── msg001.eml
  │   │   └── msg002.eml
  │   ├── Sent/
  │   │   └── msg003.eml
  │   └── Drafts/
  │       └── msg004.eml
  └── user2@domain.com/
      └── ...
  ```

## Configuration

Edit the constants at the top of `imap_bulk_upload.py`:

| Variable              | Default                                         | Description                                      |
|-----------------------|-------------------------------------------------|--------------------------------------------------|
| `HOST`                | `proxy.xyz.com`          | IMAP proxy hostname                              |
| `PORT`                | `143`                                           | IMAP port (non-SSL)                              |
| `PASSWORD`            | `password`                                    | Common password for all test accounts            |
| `DOMAIN`              | `domain.xyz.com`          | Email domain for constructing user addresses     |
| `DATA_ROOT`           | `/opt/zimbra/dataCreation`                 | Root directory containing per-user `.eml` folders |
| `CONNECTIONS_PER_USER`| `10`                                            | Parallel IMAP connections per user               |
| `MAX_USERS_PARALLEL`  | `4`                                             | Number of users processed concurrently           |
| `PRELOAD_FILES`       | `True`                                          | Read all `.eml` files into memory before upload  |
| `LOG_FILE`            | `/opt/zimbra/upload.log`                        | Path to the log file                             |



## Usage

```bash
# Basic run
python3 imap_bulk_upload.py

# Run in background with nohup
nohup python3 imap_bulk_upload.py &

# Monitor progress
tail -f /opt/zimbra/upload.log
```

## How It Works

### Phase 1 — Folder Creation
For each user, a single IMAP connection walks the user's data directory and creates all IMAP folders to mirror the on-disk hierarchy.

### Phase 2 — Parallel Upload
1. All `.eml` files are collected (and optionally preloaded into memory).
2. Files are split into equal-sized batches — one batch per IMAP connection.
3. Each batch is uploaded on its own dedicated IMAP connection using `IMAP APPEND`.
4. If a connection drops (`IMAP4.abort`), the script reconnects and retries the current message.

### Parallelism Model
```
Main Process
├── User 1  (ThreadPoolExecutor: MAX_USERS_PARALLEL)
│   ├── Connection 1  (ThreadPoolExecutor: CONNECTIONS_PER_USER)
│   ├── Connection 2
│   └── ... up to 10
├── User 2
│   ├── Connection 1
│   └── ...
└── ...
```

## Output

The script logs real-time progress and prints a summary at the end:

```
[2026-06-29 09:00:00] === FAST IMAP Bulk Upload ===
[2026-06-29 09:00:00] Host: proxy.xyz.com:143 | Connections/user: 10 | Preload: True
[2026-06-29 09:00:00] Found 4 users: user1, user2, user3, user4
...
[2026-06-29 09:30:12] ============================================================
[2026-06-29 09:30:12]   UPLOAD SUMMARY
[2026-06-29 09:30:12] ============================================================
[2026-06-29 09:30:12]   Users:       4
[2026-06-29 09:30:12]   Uploaded:    82400 / 82400
[2026-06-29 09:30:12]   Failed:      0
[2026-06-29 09:30:12]   Total time:  1812.3 seconds
[2026-06-29 09:30:12]   Overall rate: 45.5 msgs/sec
[2026-06-29 09:30:12] ============================================================
```

Exit code is **1** if any uploads failed, **0** otherwise.

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| `CONNECTION FAILED` | IMAP proxy unreachable | Verify `HOST`/`PORT`, check firewall, ensure `zmproxyctl` is running |
| `LOGIN failed` | Wrong credentials or IMAP login disabled | Verify password; run `zmprov ma user@domain zimbraImapEnabled TRUE` |
| `IMAP4.abort` during upload | Connection timeout or server-side limit | Reduce `CONNECTIONS_PER_USER`; check `zimbraImapMaxConnections` |
| `MemoryError` with `PRELOAD_FILES=True` | Not enough RAM to hold all `.eml` data | Set `PRELOAD_FILES = False` |
| Slow upload rate | Disk I/O bottleneck | Set `PRELOAD_FILES = True`; use SSD storage |

## Related

- **Jira:** [ZCS-19551](https://synacor.atlassian.net/browse/ZCS-19551)
- **Shell equivalent:** `zm-load-testing/upload_unique_eml.sh`
