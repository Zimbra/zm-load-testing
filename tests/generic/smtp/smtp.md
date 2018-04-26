# SMTP Commands

| Command | RFC                                          | Arguments | Requires | Captures | Note |
| ------- | -------------------------------------------- | --------- | -------- | -------- | ---- |
| HELO    | [5321](https://www.ietf.org/rfc/rfc5321.txt) |           |          |          |      |
| MAIL    | [5321](https://www.ietf.org/rfc/rfc5321.txt) |           |          | MAIL     |      |
| RCPT    | [5321](https://www.ietf.org/rfc/rfc5321.txt) |           | MAIL     | RCPT     |      |
| DATA    | [5321](https://www.ietf.org/rfc/rfc5321.txt) |           | RCPT     |          |      |
| QUIT    | [5321](https://www.ietf.org/rfc/rfc5321.txt) |           |          |          |      |
