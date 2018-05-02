# IMAP Commands

| Command          | RFC                                         | Arguments | Requires          | Captures      | Note                                |
| ---------------- | ------------------------------------------- | --------- | ----------------- | ------------- | ----------------------------------- |
| APPEND           | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               | Size of actual message              |
| ~~AUTHENTICATE~~ | [3501](https://tools.ietf.org/html/rfc3501) |           | Not Authenticated |               |                                     |
| CAPABILITY       | [3501](https://tools.ietf.org/html/rfc3501) |           |                   |               |                                     |
| CHECK            | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| CLOSE            | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| COPY             | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| CREATE           | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| DELTE            | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| EXAMINE          | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| EXPUNGE          | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| FETCH            | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| ID               | [2971](https://tools.ietf.org/html/rfc2971) |           |                   |               | ID values and stats                 |
| IDLE             | [2177](https://tools.ietf.org/html/rfc2177) |           | Authenticated     |               | Metrics on time in total idle state |
| LIST             | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| LOGIN            | [3501](https://tools.ietf.org/html/rfc3501) |           | Not Authenticated | Authenticated |                                     |
| LOGOUT           | [3501](https://tools.ietf.org/html/rfc3501) |           |                   |               |                                     |
| LSUB             | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| NAMESPACE        | [2342](https://tools.ietf.org/html/rfc2342) |           | Authenticated     |               |                                     |
| NOOP             | [3501](https://tools.ietf.org/html/rfc3501) |           |                   |               |                                     |
| RENAME           | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| SEARCH           | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| SELECT           | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     | Selected      |                                     |
| ~~STARTTLS~~     | [3501](https://tools.ietf.org/html/rfc3501) |           | Not Authenticated |               |                                     |
| STATUS           | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| STORE            | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| SUBSCRIBE        | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |
| UID COPY         | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| UID FETCH        | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| UID SEARCH       | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| UID STORE        | [3501](https://tools.ietf.org/html/rfc3501) |           | Selected          |               |                                     |
| UNSELECT         | [3691](https://tools.ietf.org/html/rfc3691) |           | Selected          |               |                                     |
| UNSUBSCRIBE      | [3501](https://tools.ietf.org/html/rfc3501) |           | Authenticated     |               |                                     |

| States            |
| ----------------- |
| Not Authenticated |
| Authenticated     |
| Selected          |
| Logout            |
