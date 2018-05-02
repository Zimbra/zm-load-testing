# Fixed

These tests are specific sequences of zimbra usage.

* ephemeral
  
  This performs authentications to the web interface followed by a single SOAP request to validate CSRF value returned.
  
  See ephemeral [README.md](ephemeral/README.md) for execution example.
  
* imapbenchmarking
  
  Login and list first 10 messages of inbox.
  
  See imapbenchmarking [README.md](imapbenchmarking/README.md) for execution example.
  
* imapsoapmixload
  
  This combines the imapbenchmarking and soapbenchmarking into one test.
  
  See imapsoapmixload [README.md](imapsoapmixload/README.md) for execution example.
  
* smtpbenchmarking
  
  Sends a messages from book.csv file.
  
  See smtpbenchmarking [README.md](smtpbenchmarking/README.md) for execution example.
  
* soapbenchmarking
  
  Login check the inbox periodically until a message is availble then perform a specific set of additional steps.
  
  See soapbenchmarking [README.md](soapbenchmarking/README.md) for execution example.
