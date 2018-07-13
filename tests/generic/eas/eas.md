# Version

## Zimbra 8.7.9_GA_1794

MS_Server-ActiveSync = 8.1
MS_ASProtocolVersions = 2.0,2.1,2.5,12.0,12.1

## Zimbra 8.8.8_GA_2009

MS_Server-ActiveSync = 15.0
MS_ASProtocolVersions = 2.5,12.0,12.1,14.0,14.1

# EAS Commands

| Command      | Arguments | Requires               | Captures                | Note |
| ------------ | --------- | ---------------------- | ----------------------- | ---- |
| FolderCreate |           | FOLDERSYNCKEY          | FOLDERSYNCKEY, FOLDERID |      |
| FolderDelete |           | FOLDERSYNCKEY,FOLDERID | FOLDERSYNCKEY           |      |
| FolderSync   |           | FOLDERSYNCKEY          | FOLDERSYNCKEY           |      |
| Search       |           |                        |                         |      |
| SendMail     |           |                        |                         |      |
| Sync         |           | SYNCSYNCKEY            | SYNCSYNCKEY             |      |
