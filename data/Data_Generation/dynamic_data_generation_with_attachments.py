#!/usr/bin/env python3
"""
dynamic_data_generation_with_attachements.py

Generates multi-user mailbox test data (.eml files) with weighted attachments,
calendar invites, and configurable mailbox size.

Usage example:
  python3 dynamic_data_generation_with_attachments.py --users 10 --size 100MB --attachment-percent 5 \
    --attachment-type-dist "pdf=20,jpg=10,png=10,docx=20,txt=40" --calendar-percent 2 \/Users/murali.chunduri/Desktop/Screenshots/Screenshot 2026-04-03 at 11.49.43 AM.png
    --avg-msg-size 60 --output-base ./eml_testdata --clean
"""

import os
import shutil
import random
import argparse
import re
from email.message import EmailMessage
from email.utils import make_msgid, formatdate
from faker import Faker
from datetime import datetime, timedelta

fake = Faker()


# -----------------------------
# Helpers
# -----------------------------
def parse_size(size_str):
    size_str = size_str.strip().upper()
    match = re.match(r'^([\d.]+)\s*(MB|GB)$', size_str)
    if not match:
        raise ValueError("Invalid size format. Use like '100MB' or '2GB'.")
    size, unit = match.groups()
    size = float(size)
    return int(size * 1024 * 1024) if unit == 'MB' else int(size * 1024 * 1024 * 1024)


def parse_attachment_distribution(dist_str):
    """
    Parse "pdf=20,jpg=10,png=10,docx=20,txt=40" -> dict of floats
    (You can also use ppt, xlsx if you include them and keep total = 100)
    """
    dist = {}
    if not dist_str:
        raise ValueError("Empty attachment-type-dist string")
    parts = [p.strip() for p in dist_str.split(',') if p.strip()]
    for part in parts:
        if '=' not in part:
            raise ValueError(f"Invalid distribution part: '{part}'")
        k, v = part.split('=', 1)
        k = k.strip().lower()
        try:
            pct = float(v.strip())
        except Exception:
            raise ValueError(f"Invalid percent for {k}: '{v}'")
        dist[k] = pct
    total = sum(dist.values())
    if abs(total - 100.0) > 0.01:
        raise ValueError(f"Attachment distribution must sum to 100. Got {total}")
    return dist


def build_cumulative(dist_dict, order=None):
    """Return list of (cum_threshold, key) using either provided order or insertion order."""
    items = list(dist_dict.items())
    if order:
        items = [(k, dist_dict.get(k, 0.0)) for k in order]
    cum = 0.0
    out = []
    for k, v in items:
        cum += float(v)
        out.append((cum, k))
    return out


def weighted_choice_from_cumulative(cumulative_list):
    """Given cumulative list returned by build_cumulative, choose a key."""
    r = random.uniform(0, 100)
    for threshold, key in cumulative_list:
        if r <= threshold:
            return key
    return cumulative_list[-1][1]


# -----------------------------
# Argument parsing
# -----------------------------
parser = argparse.ArgumentParser(
    description="Generate multiple unique user mailboxes with realistic .eml test data.",
    epilog="Example:\n"
           "  python3 dynamic_data_generation_with_attachements.py --users 100 --size 30MB --output-base /tmp/eml --clean\n",
    formatter_class=argparse.RawTextHelpFormatter
)

# multi-user
parser.add_argument("--users", type=int, default=1, help="Number of users/mailboxes to generate")
parser.add_argument("--user-prefix", type=str, default="user", help="Prefix for generated usernames")
parser.add_argument("--domain", type=str, default="qa-u20-perf-all-except-store.eng.zimbra.com", help="Domain for generated users")
parser.add_argument("--output-base", type=str, default="~/eml_output_testdata", help="Base output directory")
parser.add_argument("--clean", action="store_true", help="Clean the output base directory before generating")

# per-mailbox parameters
parser.add_argument("--size", type=str, default="30MB", help="Target total mailbox size per user (e.g., '100MB')")
parser.add_argument("--attachment-percent", type=float, default=2.0,
                    help="Percent of messages that have attachments (Option B probabilistic)")
parser.add_argument("--attachment-type-dist", type=str,
                    default="pdf=30,jpg=10,png=10,docx=20,txt=10,ppt=10,xlsx=10",
                    help=('Weighted distribution for attachment types. '
                          'Example: "pdf=30,jpg=10,png=10,docx=20,txt=10,ppt=10,xlsx=10" '
                          '(you may also use ppt,xlsx as long as total = 100)'))
parser.add_argument("--calendar-percent", type=float, default=1.0,
                    help="Percent of messages that are calendar invites")
parser.add_argument("--avg-msg-size", type=int, default=60,
                    help="Average message size in KB")
parser.add_argument("--min-attachments", type=int, default=0,
                    help="(Optional) Ensure at least this many attachments per mailbox")

args = parser.parse_args()

# Expand base path
BASE_OUTPUT = os.path.expanduser(args.output_base)

# Validate and parse attachment distribution
attachment_type_percents = parse_attachment_distribution(args.attachment_type_dist)

# Fixed MIME mapping (make sure keys match distribution keys)
MIME_MAP = {
    'pdf':  ('application', 'pdf'),
    'jpg':  ('image', 'jpeg'),
    'png':  ('image', 'png'),
    'docx': ('application', 'vnd.openxmlformats-officedocument.wordprocessingml.document'),
    'txt':  ('text', 'plain'),
    'ppt':  ('application', 'vnd.ms-powerpoint'),
    'xlsx': ('application', 'vnd.openxmlformats-officedocument.spreadsheetml.sheet'),
}

# Ensure distribution keys are valid
for k in attachment_type_percents.keys():
    if k not in MIME_MAP:
        raise ValueError(f"Unknown attachment type '{k}' in distribution. Allowed: {list(MIME_MAP.keys())}")

# Build cumulative distribution for weighted selection (keep ordering predictable)
cumulative_attachment = build_cumulative(attachment_type_percents, order=list(MIME_MAP.keys()))

# Clamp probabilities into 0..1
ATTACHMENT_PROB = max(0.0, min(1.0, args.attachment_percent / 100.0))
CALENDAR_PROB = max(0.0, min(1.0, args.calendar_percent / 100.0))
AVG_MSG_SIZE_KB = max(1, args.avg_msg_size)
ATTACHMENT_SIZE_RANGE = (10 * 1024, 200 * 1024)  # 10KB - 200KB

INBOX_SUBFOLDERS = ['Inbox/Subfolder1', 'Inbox/Subfolder2', 'Inbox/Subfolder3', 'Inbox/Subfolder4']
FOLDERS = INBOX_SUBFOLDERS + ['Inbox', 'Sent']

# Clean base if requested
if args.clean and os.path.exists(BASE_OUTPUT):
    print(f"Cleaning base output directory: {BASE_OUTPUT}")
    shutil.rmtree(BASE_OUTPUT)
os.makedirs(BASE_OUTPUT, exist_ok=True)

# Write userlist
USERLIST_FILE = os.path.join(BASE_OUTPUT, "userlist.txt")
with open(USERLIST_FILE, "w") as ul_f:
    for i in range(1, args.users + 1):
        ul_f.write(f"{args.user_prefix}{i}@{args.domain}\n")

print(f"\nGenerating {args.users} user mailboxes in '{BASE_OUTPUT}'")
print(f"Per-user mailbox target size: {args.size}, attachment probability: {args.attachment_percent}%, "
      f"calendar probability: {args.calendar_percent}%, avg-msg-size: {AVG_MSG_SIZE_KB} KB")
print(f"Attachment distribution: {args.attachment_type_dist}")
print("MIME map:", MIME_MAP)
print("--------------------------------------------------\n")


# -----------------------------
# Mailbox generator
# -----------------------------
def generate_mailbox_data(output_dir, target_size_str, attachment_prob, calendar_prob,
                          avg_msg_kb, min_attachments=0):
    TARGET_SIZE_BYTES = parse_size(target_size_str)
    os.makedirs(output_dir, exist_ok=True)

    # prepare folders
    for folder in FOLDERS:
        os.makedirs(os.path.join(output_dir, folder), exist_ok=True)

    # attachments dump folder per user
    attachments_root = os.path.join(output_dir, "attachments")
    os.makedirs(attachments_root, exist_ok=True)
    for ext in MIME_MAP.keys():
        os.makedirs(os.path.join(attachments_root, ext), exist_ok=True)

    total_bytes_written = 0
    email_count = 0
    total_attachments_written = 0

    folder_cycle = iter(FOLDERS)
    conversation_id = 0

    # keep generating until reach size
    while total_bytes_written < TARGET_SIZE_BYTES:
        conversation_id += 1
        thread_size = random.randint(1, 12)
        thread_subject = fake.sentence(nb_words=6)
        thread_msg_id = None

        # round-robin choose folder
        try:
            folder = next(folder_cycle)
        except StopIteration:
            folder_cycle = iter(FOLDERS)
            folder = next(folder_cycle)
        folder_path = os.path.join(output_dir, folder)

        for i in range(thread_size):
            if total_bytes_written >= TARGET_SIZE_BYTES:
                break

            from_addr = fake.email()
            to_addr = fake.email()
            subject = thread_subject if i == 0 else "Re: " + thread_subject
            msg_id = make_msgid()

            # Decide calendar and attachment
            is_calendar = (random.random() < calendar_prob)
            add_attachment = (random.random() < attachment_prob)

            # Build body with approximate target message size minus attachment guess
            attachment_guess = random.randint(*ATTACHMENT_SIZE_RANGE) if add_attachment else 0
            target_bytes = avg_msg_kb * 1024
            min_body_bytes = max(100, target_bytes - attachment_guess)
            body = ""
            while len(body.encode('utf-8')) < min_body_bytes:
                body += fake.paragraph(nb_sentences=6) + "\n"

            if is_calendar:
                # create calendar invite message
                start_time = datetime.now() + timedelta(days=random.randint(0, 30))
                end_time = start_time + timedelta(hours=1)
                ical_text = f"""BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Test Data Generator//EN
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
                msg = EmailMessage()
                msg['From'] = from_addr
                msg['To'] = to_addr
                msg['Subject'] = subject
                msg['Message-ID'] = msg_id
                msg['Date'] = formatdate(localtime=True)
                if thread_msg_id:
                    msg['In-Reply-To'] = thread_msg_id
                    msg['References'] = thread_msg_id
                msg.set_content(body)
                # attach calendar alternative
                msg.add_alternative(ical_text, subtype='calendar;method=REQUEST')
            else:
                # normal email
                msg = EmailMessage()
                msg['From'] = from_addr
                msg['To'] = to_addr
                msg['Subject'] = subject
                msg['Message-ID'] = msg_id
                msg['Date'] = formatdate(localtime=True)
                if thread_msg_id:
                    msg['In-Reply-To'] = thread_msg_id
                    msg['References'] = thread_msg_id
                msg.set_content(body)

            # If add_attachment True, pick weighted type and attach + save copy
            if add_attachment:
                chosen_ext = weighted_choice_from_cumulative(cumulative_attachment)
                maintype, subtype = MIME_MAP[chosen_ext]

                size = random.randint(*ATTACHMENT_SIZE_RANGE)
                # generate content bytes (txt -> readable, others binary random)
                if chosen_ext == 'txt':
                    # create readable text approximately size bytes
                    words = []
                    approx_words = max(1, size // 6)
                    for _ in range(approx_words):
                        words.append(fake.word())
                    data = (" ".join(words)).encode('utf-8')
                    if len(data) < size:
                        data += os.urandom(size - len(data))
                    else:
                        data = data[:size]
                else:
                    data = os.urandom(size)

                filename = f"attachment_{random.randint(1000,9999)}.{chosen_ext}"
                # save copy under attachments_root
                try:
                    with open(os.path.join(attachments_root, chosen_ext, filename), 'wb') as af:
                        af.write(data)
                except Exception as e:
                    print(f"Warning: failed to save attachment copy: {e}")

                # add attachment to message
                try:
                    msg.add_attachment(data, maintype=maintype, subtype=subtype, filename=filename)
                except Exception as e:
                    print(f"Warning: failed to add attachment to message: {e}")

                total_attachments_written += 1

            # write email to .eml
            filepath = os.path.join(folder_path, f"message_{email_count+1:07d}.eml")
            try:
                with open(filepath, 'wb') as f:
                    raw = bytes(msg)
                    f.write(raw)
                    total_bytes_written += len(raw)
            except Exception as e:
                print(f"Warning: failed to write .eml file {filepath}: {e}")

            if thread_msg_id is None:
                thread_msg_id = msg['Message-ID']

            email_count += 1

    final_mb = total_bytes_written / (1024 * 1024)
    print(f"   Generated {email_count} emails (~{final_mb:.2f} MB) in {output_dir}")
    print(f"   Attachments saved: {total_attachments_written} (in {attachments_root})")

    # Ensure minimum attachments if requested (hybrid safeguard)
    if min_attachments > 0 and total_attachments_written < min_attachments:
        need = min_attachments - total_attachments_written
        print(f"   Adding {need} extra attachments to meet --min-attachments {min_attachments}")
        added = 0
        for folder in FOLDERS:
            folder_path = os.path.join(output_dir, folder)
            if not os.path.isdir(folder_path):
                continue
            for fname in sorted(os.listdir(folder_path), reverse=True):
                if not fname.endswith('.eml'):
                    continue
                if added >= need:
                    break
                fpath = os.path.join(folder_path, fname)
                try:
                    with open(fpath, 'rb') as rf:
                        _ = rf.read()
                    msg = EmailMessage()
                    chosen_ext = weighted_choice_from_cumulative(cumulative_attachment)
                    maintype, subtype = MIME_MAP[chosen_ext]
                    small_data = os.urandom(3000)
                    attach_name = f"extra_{random.randint(1000,9999)}.{chosen_ext}"
                    msg.set_content(f"(Original message saved as raw.)\n\n[Added attachment: {attach_name}]")
                    msg.add_attachment(small_data, maintype=maintype, subtype=subtype, filename=attach_name)
                    with open(fpath, 'wb') as wf:
                        wf.write(bytes(msg))
                    added += 1
                except Exception:
                    continue
        print(f"   Added {added} attachments to meet minimum requirement.")


# -----------------------------
# Generate each user's mailbox
# -----------------------------
for idx in range(1, args.users + 1):
    user_email = f"{args.user_prefix}{idx}@{args.domain}"
    user_dir = os.path.join(BASE_OUTPUT, user_email)

    # seed random & Faker per-user for uniqueness
    seed = random.randint(1000, 9999999)
    Faker.seed(seed)
    random.seed(seed)

    print(f"[{idx}/{args.users}] Generating mailbox for {user_email} (seed={seed}) ...")
    generate_mailbox_data(
        output_dir=user_dir,
        target_size_str=args.size,
        attachment_prob=ATTACHMENT_PROB,
        calendar_prob=CALENDAR_PROB,
        avg_msg_kb=AVG_MSG_SIZE_KB,
        min_attachments=args.min_attachments
    )

print("\nGeneration complete.")
print(f"User list: {USERLIST_FILE}")
