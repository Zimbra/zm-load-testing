# Setup

EWS must be enable with custom license.

EWS must be enabled for each account or through an account class of service.

* GUI
  *  COS: Configure -> Class of Service ->  default -> Features -> General Features -> EWS (Exchange) client access
  *  Account: Manage -> Accounts -> user1 -> Features -> General Features -> EWS (Exchange) client access

* Command Line
  *  COS: zmprov mc default zimbraFeatureEwsEnabled TRUE  
          zmprov gc default zimbraFeatureEwsEnabled
  *  Acccount: zmprov ma user1 zimbraFeatureEwsEnabled TRUE  
               zmprov ga user1 zimbraFeatureEwsEnabled

# Zimbra SOAP API Commands

| Command         | Arguments     | Requires       | Captures    | Note     |
| --------------- | ------------- | -------------- | ----------- | -------- |
| CreateFolder    |               |                | FOLDERID    |          |
| CreateItem      |               |                | ITEMID      |          |
| DeleteFolder    |               | FOLDERID       |             |          |
| FindItem        | FINDITEMTYPE  |                |             |          |
| FindFolder      |               |                |             |          |
| GetItem         |               | ITEMID         |             |          |
