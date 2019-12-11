#!/bin/sh
 {
    echo "Executing test for BSP customer" 
    setsid ant BSP-business-zsoap > BSP-business-zsoap.out 2>&1 &
    setsid ant BSP-businessplus-zsoap > BSP-businessplus-zsoap.out 2>&1 &
    setsid ant BSP-standard-zsoap > BSP-standard-zsoap.out 2>&1 &
    setsid ant BSP-professional-zsoap > BSP-professional-zsoap.out 2>&1 &
    setsid ant BSP-pop > BSP-pop.out 2>&1 &
    setsid ant BSP-imap > BSP-imap.out 2>&1 &
  }
