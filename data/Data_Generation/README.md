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
