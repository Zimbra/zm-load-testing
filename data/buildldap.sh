#! /bin/bash

for i in {1..10}; do
 ctsmts=$(date -u +"%Y%m%d%H%M%SZ")
 entry1=$(date -u +"%Y%m%d%H%M%S.%6NZ")
 zct=$(date -u +"%Y%m%d%H%M%S.%3NZ")
 uuid1=$(uuidgen)

 echo dn: dc=some"$i",dc=com
 echo zimbraDomainStatus: active
 echo zimbraDomainType: local
 echo objectClass: dcObject
 echo objectClass: organization
 echo objectClass: zimbraDomain
 echo objectClass: amavisAccount
 echo zimbraId: $uuid1
 echo zimbraCreateTimestamp: "$zct"
 echo zimbraDomainName: some"$i".com
 echo zimbraMailStatus: enabled
 echo o: some"$i".com domain
 echo dc: some$i
 echo structuralObjectClass: organization
 echo entryUUID: "$uuid1"
 echo creatorsName: uid=zimbra,cn=admins,cn=zimbra
 echo createTimestamp: "$ctsmts"
 echo entryCSN: "$entry1"#000000#001#000000
 echo modifiersName: uid=zimbra,cn=admins,cn=zimbra
 echo modifyTimestamp: "$ctsmts"

 echo ""
 uuid2=$(uuidgen)
 entry2=$(date -u +"%Y%m%d%H%M%S.%6NZ")
 echo dn: ou=people,dc=some"$i",dc=com
 echo objectClass: organizationalRole
 echo ou: people
 echo cn: people
 echo structuralObjectClass: organizationalRole
 echo entryUUID: "$uuid1"
 echo creatorsName: uid=zimbra,cn=admins,cn=zimbra
 echo createTimestamp: "$ctsmts"
 echo entryCSN: "$entry2"#000000#001#000000
 echo modifiersName: uid=zimbra,cn=admins,cn=zimbra
 echo modifyTimestamp: "$ctsmts"

 echo ""
 uuid3=$(uuidgen)
 entry3=$(date -u +"%Y%m%d%H%M%S.%6NZ")
 echo dn: cn=groups,dc=some"$i",dc=com
 echo objectClass: organizationalRole
 echo cn: groups
 echo description: dynamic groups base
 echo structuralObjectClass: organizationalRole
 echo entryUUID: "$uuid1"
 echo creatorsName: uid=zimbra,cn=admins,cn=zimbra
 echo createTimestamp: "$ctsmts"
 echo entryCSN: "$entry3"#000000#001#000000
 echo modifiersName: uid=zimbra,cn=admins,cn=zimbra
 echo modifyTimestamp: "$ctsmts"
 echo ""
done

