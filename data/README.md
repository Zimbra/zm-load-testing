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
