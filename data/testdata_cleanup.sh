#!/bin/bash

MAX_DELETE_LIMIT=10   # Prevents deleting too many messages at once
SAMPLE_SIZE=10         # Number of messages to estimate average size

# Check if user provided a target size
if [[ -n "$1" ]]; then
  TARGET_MB="$1"
else
  echo "Usage: $0 <target_mailbox_size_MB>"
  exit 1
fi
  
# Check for required arguments
if [[ $# -lt 3 ]]; then
  echo "Usage: $0 <TARGET_MB> <START> <END>"
  echo "Example: $0 85 100 103"
  exit 1
fi

TARGET_MB="$1"
START="$2"
END="$3"

# Function to get mailbox size in MB
get_mailbox_size_mb() {
  local output size unit mb
  output=$(zmmailbox -z -m "$1" gms 2>/dev/null | tr -cd '[:print:]' | tr -d '\n')
  size=$(echo "$output" | awk '{print $1}')
  unit=$(echo "$output" | awk '{print toupper($2)}')
  case "$unit" in
    B)  mb=$(echo "scale=4; $size / 1024 / 1024" | bc -l) ;;
    KB) mb=$(echo "scale=4; $size / 1024" | bc -l) ;;
    MB) mb="$size" ;;
    GB) mb=$(echo "scale=4; $size * 1024" | bc -l) ;;
    TB) mb=$(echo "scale=4; $size * 1024 * 1024" | bc -l) ;;
    PB) mb=$(echo "scale=4; $size * 1024 * 1024 * 1024" | bc -l) ;;
    *) echo "Unknown unit: '$unit' in output: $output"; mb="0.00" ;;
  esac
  printf "%.2f" "$mb"
}

# Function to get folders under Inbox dynamically
get_inbox_folders() {
  local user="$1"
  zmmailbox -z -m "$user" gaf 2>/dev/null | \
    awk '$2 == "mess" && $5 ~ /^\/Inbox/ {print substr($5, 2)}'
}

# Check if a folder contains at least 1 message
has_messages() {
  local user="$1"
  local folder="$2"
  zmmailbox -z -m "$user" search -t message --limit 1 "in:\"$folder\"" 2>/dev/null | grep -q '^[0-9]\+\.'
}

# Get message count in a folder
get_message_count() {
  local user="$1"
  local folder="$2"
  zmmailbox -z -m "$user" search -t message "in:\"$folder\"" 2>/dev/null | grep -c '^[0-9]\+\.'
}

# Estimate average message size in MB from a sample of recent messages
estimate_avg_msg_size_mb() {
  local user="$1"
  local sample_size=$SAMPLE_SIZE

  local msg_ids
  msg_ids=$(zmmailbox -z -m "$user" search -t message --limit $sample_size "*" \
            | awk '$1 ~ /^[0-9]+\./ { print $2 }' | paste -sd, -)

  if [[ -z "$msg_ids" ]]; then
    echo "0.05"  # Fallback
    return
  fi

  local total_size
  total_size=$(zmmailbox -z -m "$user" gs "$msg_ids" 2>/dev/null \
                | awk -F": " '/size/ { sum += $2 } END { print sum }')

  if [[ -z "$total_size" || "$total_size" -eq 0 ]]; then
    echo "0.05"
    return
  fi

  local avg
  avg=$(echo "scale=6; $total_size / $sample_size / 1024 / 1024" | bc -l)
  printf "%.6f\n" "$avg"
}

# Start processing users
for i in $(seq "$START" "$END"); do
  USER="user$i@domain.zimbra.com"
  echo "Checking mailbox for $USER..."

  CURRENT_MB=$(get_mailbox_size_mb "$USER")
  echo "Initial size: $CURRENT_MB MB"

  if (( $(echo "$CURRENT_MB <= $TARGET_MB" | bc -l) )); then
    echo "Mailbox already under target."
    echo "------------------------------------------"
    continue
  fi

  # Estimate average message size
  AVG_MSG_SIZE_MB=$(estimate_avg_msg_size_mb "$USER")
  echo "Estimated avg. message size: $AVG_MSG_SIZE_MB MB"

  echo "Gathering folders under Inbox for $USER..."
  declare -A FOLDER_COUNTS
  FOLDERS=($(get_inbox_folders "$USER"))
  FOLDERS+=("Inbox" "Sent")  # Include common top-level folders

  for FOLDER in "${FOLDERS[@]}"; do
    if has_messages "$USER" "$FOLDER"; then
      COUNT=$(get_message_count "$USER" "$FOLDER")
      FOLDER_COUNTS["$FOLDER"]=$COUNT
    fi
  done

  if [[ ${#FOLDER_COUNTS[@]} -eq 0 ]]; then
    echo "No folders with messages to clean."
    continue
  fi

  # Sort folders by message count descending
  SORTED_FOLDERS=($(for f in "${!FOLDER_COUNTS[@]}"; do
    echo "${FOLDER_COUNTS[$f]} $f"
  done | sort -rn | awk '{print $2}'))

  echo "Cleanup priority:"
  for f in "${SORTED_FOLDERS[@]}"; do
    echo "  $f (${FOLDER_COUNTS[$f]} messages)"
  done

  # Cleanup loop with global delete counter
  TOTAL_DELETED=0
  NEEDED_TO_DELETE=$(printf "%.0f" "$(echo "($CURRENT_MB - $TARGET_MB) / $AVG_MSG_SIZE_MB" | bc -l)")

  while (( $(echo "$CURRENT_MB > $TARGET_MB" | bc -l) )); do
    REMAINING_TO_DELETE=$((NEEDED_TO_DELETE - TOTAL_DELETED))
    (( REMAINING_TO_DELETE <= 0 )) && break

    DELETE_LIMIT=$REMAINING_TO_DELETE
    (( DELETE_LIMIT > MAX_DELETE_LIMIT )) && DELETE_LIMIT=$MAX_DELETE_LIMIT

    echo "Need to free ~$(echo "$CURRENT_MB - $TARGET_MB" | bc) MB — deleting up to $DELETE_LIMIT messages"

    for FOLDER in "${SORTED_FOLDERS[@]}"; do
      (( REMAINING_TO_DELETE <= 0 )) && break

      echo "Deleting from '$FOLDER'..."

      MSG_IDS=$(zmmailbox -z -m "$USER" search -t message --sort dateAsc --limit "$DELETE_LIMIT" "in:\"$FOLDER\"" \
        | awk '$1 ~ /^[0-9]+\./ { print $2 }' | paste -sd, -)

      if [[ -n "$MSG_IDS" ]]; then
        zmmailbox -z -m "$USER" dm "$MSG_IDS"
        MSG_COUNT=$(echo "$MSG_IDS" | tr -cd , | wc -c)
        MSG_COUNT=$((MSG_COUNT + 1))
        TOTAL_DELETED=$((TOTAL_DELETED + MSG_COUNT))
        echo "Deleted $MSG_COUNT messages from $FOLDER"
      else
        echo "No deletable messages in $FOLDER"
      fi

      CURRENT_MB=$(get_mailbox_size_mb "$USER")
      echo "Updated size: $CURRENT_MB MB"

      if (( $(echo "$CURRENT_MB <= $TARGET_MB" | bc -l) )); then
        echo "Target reached for $USER."
        break 2
      fi

      REMAINING_TO_DELETE=$((NEEDED_TO_DELETE - TOTAL_DELETED))
    done

    echo "🔁 Continuing cleanup loop..."
  done

  echo "Final mailbox size for $USER: $CURRENT_MB MB"
  echo "------------------------------------------"
done

echo "All users processed and reduced below $TARGET_MB MB."


