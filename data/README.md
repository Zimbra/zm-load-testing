# dynamic_testdata_generation.py

This script generates realistic `.eml` test data with optional attachments, calendar invites, and configurable folder structure and sizes — ideal for testing

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

## Install dependencies:
```
pip install faker
```

## Usage
```
python3 dynamic_testdata_generation.py -h
```

### Options
|Option	              | Description
|---------------------|----------------------------------------------------------------------------|
|--size	              | Total target mailbox size (e.g. 100MB, 1GB) (default: 30MB)                |
|--attachment-percent |	% of emails that should contain attachments (default: 2)                   |
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
## Examples
```
1. Generate 100MB of email data with 5% attachments and 2% calendar invites:
   python3 dynamic_testdata_generation.py --size 100MB --attachment-percent 5 --calendar-percent 2
```
```
2. Generate 500MB of emails with smaller average message size and clean the directory first:
   python3 dynamic_testdata_generation.py --size 500MB --avg-msg-size 40 --clean
```
```
3. Use a custom output directory:
   python3 dynamic_testdata_generation.py --output-dir testdata --size 50MB
```

## Output Summary
At the end, the script reports:
```
- Total emails generated
- Final size in MB/GB
- Output folder path
- Folder count
```

### Example
```
python3 dynamic_testdata_generation.py --size 10MB --attachment-percent 5 --calendar-percent 2 --avg-msg-size 80 --output-dir eml_test --clean
Generating .eml files targeting 10MB in 'eml_test'...
Attachments: 5.0% | Calendar Invites: 2.0% | Avg Email Size: 80 KB
Generated 123 emails (~10.07 MB / ~0.01 GB) in 'eml_test' across 6 folders.
```

## Notes
```
- File names follow the format: message_0000001.eml, message_0000002.eml, ...
- Calendar invites use basic iCalendar (VEVENT) format.
- Email attachment types include formats like pdf,jpg,png,docx and txt.
- Attachments are dummy binary data with realistic MIME types.
- This script will generate .eml files under a folder structure which can further be populated as data using zmlmtpinject
```



# testdata_cleanup.sh

This script helps manage mailbox sizes on a **Zimbra** email server by **automatically deleting old messages** from user mailboxes until they fall below a specified size threshold. It identifies the largest folders and selectively deletes the oldestmessages in a controlled manner.
---

##  Features
1. Estimates Current Mailbox Size
Uses **zmmailbox gms** to get the user's total mailbox size in MB.

2. Estimates Average Message Size
Pulls a sample of recent messages (default: 10) and calculates their average size.

3. Identifies Folders to Clean
Detects folders under /Inbox, as well as Inbox and Sent.

4. Sorts Folders by Message Count
Cleans folders with the most messages first to free space efficiently.

5. Deletes Messages in Batches
Deletes oldest messages (ascending by date) in batches of up to MAX_DELETE_LIMIT (default: 10).

6. Checks Size After Each Batch
Continues deleting until the target size is met or no more deletable messages remain.
---

## Requirements
- The script must be run as a zimbra user
- mailbox should be up in the Env
- user accounts must exist


## Usage
```
bash testdata_cleanup.sh <TARGET_MB> <START> <END>
```
- TARGET_MB — The maximum mailbox size (in MB) you want each user to have.
- START — Starting user number (e.g., 1 for user1@domain.zimbra.com)
- END — Ending user number (e.g., 100 for user100@domain.zimbra.com)


## Example
```
bash testdata_cleanup.sh 500 1 100
```
This command will reduce the mailboxes for user1@domain.zimbra.com to user100@domain.zimbra.com until each mailbox is under 500 MB.

## Script Output Example
```
bash /home/ubuntu/delete_5.sh 580 102 103'
 Checking mailbox for user102@domain.zimbra.com...
 Initial size: 584.57 MB
 Estimated avg. message size: 0.05 MB
 Gathering folders under Inbox for user102@domain..zimbra.com...
  Cleanup priority:
  Sent (16 messages)
  Inbox/Subfolder4 (16 messages)
  Inbox/Subfolder3 (16 messages)
  Inbox/Subfolder2 (16 messages)
  Inbox/Subfolder1 (16 messages)
  Inbox (16 messages)
  Need to free ~4.57 MB — deleting up to 10 messages
  Deleting from 'Sent'...
  Deleted 10 messages from Sent
  Updated size: 583.55 MB
  Deleting from 'Inbox/Subfolder4'...
  Deleted 10 messages from Inbox/Subfolder4
  Updated size: 582.53 MB
  Deleting from 'Inbox/Subfolder3'...
  Deleted 10 messages from Inbox/Subfolder3
  Updated size: 581.51 MB
  Deleting from 'Inbox/Subfolder2'...
  Deleted 10 messages from Inbox/Subfolder2
  Updated size: 580.49 MB
  Deleting from 'Inbox/Subfolder1'...
  Deleted 10 messages from Inbox/Subfolder1
  Updated size: 579.47 MB
  Target reached for user102@domain.zimbra.com.
  Final mailbox size for user102@domain.zimbra.com: 579.47 MB
  ------------------------------------------
  Checking mailbox for user103@domain.zimbra.com...
  Initial size: 10.25 MB
  Mailbox already under target.
  ------------------------------------------
  All users processed and reduced below 580 MB.
```
