# POP Commands

| Command | RFC                                          | Arguments | Requires      | Captures    | Note |
| ------- | -------------------------------------------- | --------- | ------------- | ----------- | ---- |
| LIST    | [1939](https://www.ietf.org/rfc/rfc1939.txt) |           | TRANSACTION   |             |      |
| PASS    | [1939](https://www.ietf.org/rfc/rfc1939.txt) |           | USER          | TRANSACTION |      |
| QUIT    | [1939](https://www.ietf.org/rfc/rfc1939.txt) |           |               | UPDATE      |      |
| USER    | [1939](https://www.ietf.org/rfc/rfc1939.txt) |           | AUTHROIZATION | USER        |      |

| States        | Note          |
| ------------- | ------------- |
| AUTHORIZATION | Initial State |
| TRANSACTION   |               |
| UPDATE        |               |
