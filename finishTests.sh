#!/bin/sh

usage=$(
cat <<EOF
$0 [OPTION]
-u VALUE    set "u" argument to specify type of test to stop. Test types: CSP/BSP/smtp 
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

  if [ ! -z "$userType" ]; then {
    mkdir log_$(date +'%m-%d_%H-%M')
    echo "creating log_$(date +'%m-%d_%H-%M') for backup directory"
    mv logs/* log_$(date +'%m-%d_%H-%M')
    }
  else {
       echo "Please specify -u option with type of test to stop (CSP or BSP or smtp).\nExample: \" sh finishTests.sh -u CSP\" "
     } 
    fi

  if [ "$userType" = "BSP" ]; then {
    for i in `ps -fx|grep jmeter |awk -F " " {'print$1'}`; do kill -9 $i; done
    [ ! -d BSP-imap.out ] && mv BSP-imap.out log_$(date +'%m-%d_%H-%M')
    [ ! -d BSP-pop.out ] &&  mv BSP-pop.out log_$(date +'%m-%d_%H-%M')
    [ ! -d BSP-zsoap.out ] && mv BSP-zsoap.out log_$(date +'%m-%d_%H-%M')
    }
  else {
      if [ "$userType" = 'CSP' ]; then {
         for i in `ps -fx|grep jmeter |awk -F " " {'print$1'}`; do kill -9 $i; done
         [ ! -d CSP-imap.out ] && mv CSP-imap.out log_$(date +'%m-%d_%H-%M')
         [ ! -d CSP-pop.out ] &&  mv CSP-pop.out log_$(date +'%m-%d_%H-%M')
         [ ! -d CSP-zsoap.out ] && mv CSP-zsoap.out log_$(date +'%m-%d_%H-%M')
      }
      else {
        if [ "$userType" = 'smtp' ]; then {
           for i in `ps -fx|grep jmeter |awk -F " " {'print$1'}`; do kill -9 $i; done
           [ ! -d generic-smtp.out ] && mv generic-smtp.out log_$(date +'%m-%d_%H-%M')
        }
        fi
    }
    fi
  }
  fi

