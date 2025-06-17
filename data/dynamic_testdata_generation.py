import os
import shutil
import random
import argparse
import re
from email.message import EmailMessage
from email.utils import make_msgid, formatdate
from faker import Faker
from pathlib import Path
from datetime import datetime, timedelta

fake = Faker()

# -----------------------------
# Argument Parsing
# -----------------------------
def parse_size(size_str):
    size_str = size_str.strip().upper()
    match = re.match(r'^([\d.]+)\s*(MB|GB)$', size_str)
    if not match:
        raise ValueError("Invalid size format. Use like '100MB' or '2GB'.")
    
    size, unit = match.groups()
    size = float(size)
    return int(size * 1024 * 1024) if unit == 'MB' else int(size * 1024 * 1024 * 1024)

parser = argparse.ArgumentParser(
    description="Generate realistic .eml test data with attachments and calendar invites.\n"
                "Supports size limits, folder structure, and message size control.",
    epilog="Example:\n"
           "  python3 dynamic_testdata_generation.py --size 100MB --attachment-percent 5 \\\n"
           "      --calendar-percent 2 --avg-msg-size 80 --output-dir eml_test --clean",
    formatter_class=argparse.RawTextHelpFormatter
)

parser.add_argument("--size", type=str, default="30MB",
                    help="Target total mailbox size (e.g., '100MB', '1GB')")
parser.add_argument("--attachment-percent", type=float, default=2.0,
                    help="Percentage of messages with attachments (default: 2)")
parser.add_argument("--calendar-percent", type=float, default=1.0,
                    help="Percentage of messages as calendar invites (default: 1)")
parser.add_argument("--avg-msg-size", type=int, default=100,
                    help="Average email size in KB (default: 60)")
parser.add_argument("--output-dir", type=str, default="~/eml_output_testdata",
                    help="Output directory for generated emails")
parser.add_argument("--clean", action="store_true",
                    help="Clean the output directory before generating")

args = parser.parse_args()

# -----------------------------
# Configuration
# -----------------------------
TARGET_SIZE_BYTES = parse_size(args.size)
TARGET_AVG_SIZE_KB = args.avg_msg_size
ATTACHMENT_PERCENT = args.attachment_percent / 100.0
CALENDAR_PERCENT = args.calendar_percent / 100.0
OUTPUT_DIR = args.output_dir

INBOX_SUBFOLDERS = ['Inbox/Subfolder1', 'Inbox/Subfolder2', 'Inbox/Subfolder3', 'Inbox/Subfolder4']
FOLDERS = INBOX_SUBFOLDERS + ['Inbox', 'Sent']

ATTACHMENT_TYPES = {
    'pdf': ('application', 'pdf'),
    'jpg': ('image', 'jpeg'),
    'png': ('image', 'png'),
    'docx': ('application', 'vnd.openxmlformats-officedocument.wordprocessingml.document'),
    'txt': ('text', 'plain'),
}
ATTACHMENT_SIZE_RANGE = (10 * 1024, 200 * 1024)

if args.clean and os.path.exists(OUTPUT_DIR):
    shutil.rmtree(OUTPUT_DIR)

for folder in FOLDERS:
    os.makedirs(os.path.join(OUTPUT_DIR, folder), exist_ok=True)

# -----------------------------
# Email Generators
# -----------------------------
def generate_dummy_attachment(size_bytes: int):
    return os.urandom(size_bytes)

def create_email(from_addr, to_addr, subject, body, msg_id, in_reply_to=None, add_attachment=False):
    msg = EmailMessage()
    msg['From'] = from_addr
    msg['To'] = to_addr
    msg['Subject'] = subject
    msg['Message-ID'] = msg_id
    msg['Date'] = formatdate(localtime=True)

    if in_reply_to:
        msg['In-Reply-To'] = in_reply_to
        msg['References'] = in_reply_to

    msg.set_content(body)

    if add_attachment:
        ext = random.choice(list(ATTACHMENT_TYPES.keys()))
        maintype, subtype = ATTACHMENT_TYPES[ext]
        size = random.randint(*ATTACHMENT_SIZE_RANGE)
        data = generate_dummy_attachment(size)
        filename = f"attachment_{random.randint(1000,9999)}.{ext}"
        msg.add_attachment(data, maintype=maintype, subtype=subtype, filename=filename)

    return msg

def create_calendar_invite(from_addr, to_addr, subject, start_time, end_time, msg_id, in_reply_to=None):
    msg = EmailMessage()
    msg['From'] = from_addr
    msg['To'] = to_addr
    msg['Subject'] = subject
    msg['Message-ID'] = msg_id
    msg['Date'] = formatdate(localtime=True)

    if in_reply_to:
        msg['In-Reply-To'] = in_reply_to
        msg['References'] = in_reply_to

    ical = f"""BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Your Organization//Your Product//EN
METHOD:REQUEST
BEGIN:VEVENT
UID:{msg_id.strip('<>')}
DTSTAMP:{datetime.utcnow().strftime('%Y%m%dT%H%M%SZ')}
DTSTART:{start_time.strftime('%Y%m%dT%H%M%SZ')}
DTEND:{end_time.strftime('%Y%m%dT%H%M%SZ')}
SUMMARY:{subject}
DESCRIPTION:{subject}
END:VEVENT
END:VCALENDAR
"""
    msg.set_content("This is a calendar invite for the event.")
    msg.add_alternative(ical, subtype='calendar;method=REQUEST')
    return msg

# -----------------------------
# Main Generation Loop
# -----------------------------
total_bytes_written = 0
email_count = 0
conversation_id = 0
attachment_interval = max(1, int(1 / ATTACHMENT_PERCENT)) if ATTACHMENT_PERCENT > 0 else float('inf')
calendar_interval = max(1, int(1 / CALENDAR_PERCENT)) if CALENDAR_PERCENT > 0 else float('inf')

print(f"Generating .eml files targeting {args.size} in '{OUTPUT_DIR}'...")
print(f"Attachments: {args.attachment_percent}% | 📅 Calendar Invites: {args.calendar_percent}% | "
      f"Avg Email Size: {TARGET_AVG_SIZE_KB} KB")

folder_cycle = iter(FOLDERS)

while total_bytes_written < TARGET_SIZE_BYTES:
    conversation_id += 1
    thread_size = random.randint(5, 30)
    thread_subject = fake.sentence(nb_words=6)
    thread_msg_id = None

    try:
        folder = next(folder_cycle)
    except StopIteration:
        folder_cycle = iter(FOLDERS)
        folder = next(folder_cycle)
    folder_path = os.path.join(OUTPUT_DIR, folder)

    for i in range(thread_size):
        if total_bytes_written >= TARGET_SIZE_BYTES:
            break

        from_addr = fake.email()
        to_addr = fake.email()
        subject = "Re: " + thread_subject if i > 0 else thread_subject
        base_body = ""
        target_msg_size = TARGET_AVG_SIZE_KB * 1024

        is_calendar = (email_count % calendar_interval == 0)
        add_attachment = (not is_calendar) and (email_count % attachment_interval == 0)
        attachment_size = random.randint(*ATTACHMENT_SIZE_RANGE) if add_attachment else 0

        min_body_bytes = max(100, target_msg_size - attachment_size)
        while len(base_body.encode('utf-8')) < min_body_bytes:
            base_body += fake.paragraph(nb_sentences=10) + "\n"

        msg_id = make_msgid()

        if is_calendar:
            start_time = datetime.now() + timedelta(days=random.randint(0, 30))
            end_time = start_time + timedelta(hours=1)
            msg = create_calendar_invite(from_addr, to_addr, subject, start_time, end_time, msg_id, thread_msg_id)
        else:
            msg = create_email(from_addr, to_addr, subject, base_body, msg_id, thread_msg_id, add_attachment)

        filename = os.path.join(folder_path, f"message_{email_count+1:07d}.eml")
        with open(filename, 'wb') as f:
            raw_msg = bytes(msg)
            f.write(raw_msg)
            total_bytes_written += len(raw_msg)

        if i == 0:
            thread_msg_id = msg['Message-ID']

        email_count += 1

# -----------------------------
# Summary
# -----------------------------
final_size_mb = total_bytes_written / (1024 * 1024)
final_size_gb = total_bytes_written / (1024 * 1024 * 1024)

print(f"\n Generated {email_count} emails "
      f"(~{final_size_mb:.2f} MB / ~{final_size_gb:.2f} GB) "
      f"in '{OUTPUT_DIR}' across {len(FOLDERS)} folders.")

