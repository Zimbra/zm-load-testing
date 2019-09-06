#!/bin/sh

usage=$(
cat <<EOF
$0 [OPTION]
-u VALUE    set "u" argument to specify type of customer
EOF
)

userType=""

while getopts "u:i" OPTION; do
  case "$OPTION" in
    u)
      userType="$OPTARG"
      ;;
    *)
      echo "unrecognized option"
      echo "$usage"
      ;;
  esac
done

  if [ "$userType" = "BSP" ]; then {
    echo "Executing test for BSP customer" 
    setsid ant BSP-business-zsoap > BSP-business-zsoap.out 2>&1 &
    setsid ant BSP-businessplus-zsoap > BSP-businessplus-zsoap.out 2>&1 &
    setsid ant BSP-standard-zsoap > BSP-standard-zsoap.out 2>&1 &
    setsid ant BSP-professional-zsoap > BSP-professional-zsoap.out 2>&1 &
   # setsid ant BSP-pop >   BSP-pop.out 2>&1 &
   # setsid ant BSP-imap >  BSP-imap.out 2>&1 &
  }

  else {
       if [ "$userType" = 'CSP' ]; then {
        echo "Executing test for CSP customer"
        setsid ant CSP-zsoap > CSP-zsoap.out 2>&1 &
        #setsid ant CSP-pop >   CSP-pop.out 2>&1 &
        #setsid ant CSP-imap >  CSP-imap.out 2>&1 &
      }
      else {
        echo "Customer type not mentioned"
      }
      fi
  }
  fi
