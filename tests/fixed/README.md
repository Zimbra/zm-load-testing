# Fixed

These tests are specific sequences of zimbra usage.

* ephemeral

This performs authentications to the web interface followed by a single SOAP request to validate CSRF value returned.

* imapbenchmarking

Login and list first 10 messages of inbox.

* imapsoapmixload

This combines the imapbenchmarking and soapbenchmarking into one test.

* smtpbenchmarking

Sends a messages from book.csv file.

* soapbenchmarking

Login check the inbox periodically until a message is availble then perform a specific set of additional steps.
