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

| Command                               | Service       | Arguments         | Requires                       | Captures              | Note                                                                                               |
| ------------------------------------- | ------------- | ----------------- | ------------------------------ | --------------------- | -------------------------------------------------------------------------------------------------- |
| ApplyFilterRulesRequest               | zimbraMail    |                   | AUTHTOKEN,FILTER               |                       | query "test"                                                                                       |
| AuthRequest                           | zimbraAdmin   | AUTHTYPE          |                                | ADMINAUTHTOKEN        | AUTHTYPE=admin                                                                                     |
| AuthRequest                           | zimbraAccount | AUTHTYPE          |                                | AUTHTOKEN             | AUTHTYPE=user                                                                                      |
| AutoCompleteRequest                   | zimbraMail    |                   | AUTHTOKEN                      |                       | name "user"                                                                                        |
| BrowseRequest                         | zimbraMail    |                   | AUTHTOKEN                      |                       | browseBy "domains"                                                                                 |
| CheckSpellingRequest                  | zimbraMail    |                   | AUTHTOKEN                      |                       | text "This is a spelling chck tert."                                                               |
| ContactActionRequest                  | zimbraMail    |                   | AUTHTOKEN,CONTACT              |                       | action "tag" "test"                                                                                |
| ConvActionRequest                     | zimbraMail    |                   | AUTHTOKEN,CONVERSATION         |                       | action "read"                                                                                      |
| CreateAppointmentRequest              | zimbraMail    |                   | AUTHTOKEN                      |                       | name "soaptest appointment" content "just a test" s "20170628T110000" e "20170628T120000"          |
| CreateContactRequest                  | zimbraMail    |                   | AUTHTOKEN                      |                       | lastName "Testing"                                                                                 |
| CreateFolderRequest                   | zimbraMail    |                   | AUTHTOKEN                      |                       | name soaptest                                                                                      |
| CreateSearchFolderRequest             | zimbraMail    |                   | AUTHTOKEN                      |                       | name soapsearchtest query "Testing" l "1"                                                          |
| CreateTagRequest                      | zimbraMail    |                   | AUTHTOKEN                      |                       | creates soaptest tag                                                                               |
| CreateWaitSetRequest                  | zimbraMail    |                   | AUTHTOKEN                      | WAITSETID, WAITSETSEQ |                                                                                                    |
| DestroyWaitSetRequest                 | zimbraMail    |                   | AUTHTOKEN,WAITSETID            |                       |                                                                                                    |
| DiscoverRightsRequest                 | zimbraAccount |                   | AUTHTOKEN                      |                       | right sendAs ???                                                                                   |
| DismissCalendarItemAlarmRequest       | zimbraMail    |                   | AUTHTOKEN                      |                       |                                                                                                    |
| EndSessionRequest                     | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| FlushCacheRequest                     | zimbraAdmin   |                   | ADMINAUTHTOKEN                 |                       |                                                                                                    |
| FolderActionRequest                   | zimbraMail    | FOLDERACTIONTYPE  | AUTHTOKEN,FOLDER               |                       | FOLDERACTIONTYPE read,delete supported                                                             |
| GetAccountRequest                     | zimbraAdmin   |                   | ADMINAUTHTOKEN                 | ACCOUNTID             |                                                                                                    |
| GetAccountInfoRequest                 | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetAggregateQuotaUsageOnServerRequest | zimbraAdmin   |                   | ADMINAUTHTOKEN                 |                       |                                                                                                    |
| GetAppointmentRequest                 | zimbraMail    |                   | AUTHTOKEN,APPOINTMENT          |                       |                                                                                                    |
| GetAvailableCsvFormatsRequest         | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetAvailableLocalesRequest            | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetAvailableSkinsRequest              | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetContactsRequest                    | zimbraMail    |                   | AUTHTOKEN                      | CONTACT               |                                                                                                    |
| GetConvRequest                        | zimbraMail    |                   | AUTHTOKEN,CONVERSATION         |                       |                                                                                                    |
| GetCosRequest                         | zimbraAdmin   |                   | ADMINAUTHTOKEN                 |                       |                                                                                                    |
| GetDataSourcesRequest                 | zimbraMail    |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetDomainInfoRequest                  | zimbraAdmin   |                   |                                |                       | domain name "zimbra07.loadatest.synacor.com" \# need to fix                                        |
| GetFilterRulesRequest                 | zimbraMail    |                   | AUTHTOKEN                      | FILTER                |                                                                                                    |
| GetFolderRequest                      | zimbraMail    | PATH              | AUTHTOKEN                      | FOLDER                | PATH defaults to "/inbox"                                                                          |
| GetFreeBusyRequest                    | zimbraMail    |                   | AUTHTOKEN                      |                       | s 1501570800000 e 1504249200000                                                                    |
| GetInfoRequest                        | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetMailboxRequest                     | zimbraAdmin   |                   | ADMINAUTHTOKEN,ACCOUNTID       |                       |                                                                                                    |
| GetMailboxMetadataRequest             | zimbraMail    |                   | AUTHTOKEN                      |                       | section "zwc:test" ???                                                                             |
| GetMiniCalRequest                     | zimbraMail    |                   | AUTHTOKEN                      |                       | s 1501570800000 e 1504249200000                                                                    |
| GetMsgRequest                         | zimbraMail    |                   | AUTHTOKEN,MESSAGE              |                       |                                                                                                    |
| GetPrefsRequest                       | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetRightsRequest                      | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetShareInfoRequest                   | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| GetTagRequest                         | zimbraMail    |                   | AUTHTOKEN                      | TAG                   |                                                                                                    |
| GetWhiteBlackListRequest              | zimbraAccount |                   | AUTHTOKEN                      |                       |                                                                                                    |
| ModifyAccountRequest                  | zimbraAdmin   |                   | ADMINAUTHTOKEN,ACCOUNTID       |                       | zimbraPrefIMFlashTitle FALSE                                                                       |
| ModifyContactRequest                  | zimbraMail    |                   | AUTHTOKEN,CONTACT              |                       | lastName Testing                                                                                   |
| ModifyFilterRulesRequest              | zimbraMail    |                   | AUTHTOKEN                      |                       | name "Testing" bodyTest "Testing" actionDiscard                                                    |
| ModifyPrefsRequest                    | zimbraAccount |                   | AUTHTOKEN                      |                       | zimbraPrefIMFlashIcon FALSE                                                                        |
| MsgActionRequest                      | zimbraMail    |                   | AUTHTOKEN,MESSAGE              |                       | action "read"                                                                                      |
| NoOpRequest                           | zimbraMail    |                   | AUTHTOKEN                      |                       |                                                                                                    |
| RankingActionRequest                  | zimbraMail    |                   | AUTHTOKEN                      |                       |                                                                                                    |
| SaveDraftRequest                      | zimbraMail    |                   | AUTHTOKEN                      |                       | creates new                                                                                        |
| SearchConvRequest                     | zimbraMail    |                   | AUTHTOKEN,CONVERSATION         |                       | search string "Test"                                                                               |
| SearchRequest                         | zimbraMail    | SEARCHTYPE,SEARCH | AUTHTOKEN                      | APPOINTMENT           | SEARCHTYPE=appointment, SEARCH "\*" is handy with GetAppointmentRequest                            |
| SearchRequest                         | zimbraMail    | SEARCHTYPE,SEARCH | AUTHTOKEN                      | CONVERSATION          | SEARCHTYPE=conversation                                                                            |
| SearchRequest                         | zimbraMail    | SEARCHTYPE,SEARCH | AUTHTOKEN                      | MESSAGE,COMPNUM       | SEARCHTYPE=message, using SEARCH "is:invite not from:${USER}" is handy with SendInviteReplyRequest |
| SendDeliveryReportRequest             | zimbraMail    |                   | AUTHTOKEN,MESSAGE              |                       |                                                                                                    |
| SendInviteReplyRequest                | zimbraMail    |                   | AUTHTOKEN,MESSAGE,COMPNUM      |                       | MESSAGE must have a calendar invite inside at COMPNUM                                              |
| SendMsgRequest                        | zimbraMail    |                   | AUTHTOKEN                      |                       | address ("user1"-\>"user5") content ...                                                            |
| SetCustomMetadataRequest              | zimbraMail    |                   | AUTHTOKEN,MESSAGE              |                       | section "zwc:test" key "foo" value "bar"                                                           |
| SetMailboxMetadataRequest             | zimbraMail    |                   | AUTHTOKEN                      |                       | section "zwc:test" key "foo" value "bar"                                                           |
| SnoozeCalendarItemAlarmRequest        | zimbraMail    |                   | AUTHTOKEN                      |                       |                                                                                                    |
| SyncGalRequest                        | zimbraAccount |                   | AUTHTOKEN                      | GALTOKEN              |                                                                                                    |
| SyncRequest                           | zimbraMail    |                   | AUTHTOKEN                      | SYNCTOKEN             |                                                                                                    |
| TagActionRequest                      | zimbraMail    | TAGACTIONTYPE     | AUTHTOKEN,TAG                  |                       |                                                                                                    |
| WaitSetRequest                        | zimbraMail    |                   | AUTHTOKEN,WAITSETID,WAITSETSEQ |                       |                                                                                                    |
