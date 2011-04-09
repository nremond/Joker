#!/bin/bash

PORT="9080"
HOST="localhost"
FLUSHUSERSTABLE="true"
NBUSERS=2
QUESTIONTIMELIMIT=20
SYNCHROTIME=2

#curl "http://${HOST}:${PORT}/api/join/localhost/7911";

# Create a game with 3 questions
curl -X POST -d "{ \"authentication_key\" : \"1234\", \"parameters\" : \"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;usi:gamesession xmlns:usi=&quot;http://www.usi.com&quot; xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xsi:schemaLocation=&quot;http://www.usi.com gamesession.xsd &quot;&gt;  &lt;usi:questions&gt;    &lt;usi:question goodchoice=&quot;1&quot;&gt;      &lt;usi:label&gt;This the quesion 1.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;1&quot;&gt;      &lt;usi:label&gt;This the quesion 2.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;2&quot;&gt;      &lt;usi:label&gt;This the quesion 3.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;2&quot;&gt;      &lt;usi:label&gt;This the quesion 4.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;        &lt;/usi:question&gt;      &lt;/usi:questions&gt;  &lt;usi:parameters&gt;    &lt;usi:logintimeout&gt;20&lt;/usi:logintimeout&gt;    &lt;usi:synchrotime&gt;${SYNCHROTIME}&lt;/usi:synchrotime&gt;    &lt;usi:nbusersthreshold&gt;${NBUSERS}&lt;/usi:nbusersthreshold&gt;    &lt;usi:questiontimeframe&gt;${QUESTIONTIMELIMIT}&lt;/usi:questiontimeframe&gt;    &lt;usi:nbquestions&gt;20&lt;/usi:nbquestions&gt;    &lt;usi:flushusertable&gt;${FLUSHUSERSTABLE}&lt;/usi:flushusertable&gt;    &lt;usi:trackeduseridmail&gt;usi:trackeduseridmail&lt;/usi:trackeduseridmail&gt;  &lt;/usi:parameters&gt;&lt;/usi:gamesession&gt;\" }" "http://${HOST}:${PORT}/api/game"

# Create $NBUSERS users
i=1
while [[ $i -le ${NBUSERS} ]]
do
   curl -X POST -d "{ \"firstname\" : \"$i\", \"lastname\" : \"$i\", \"mail\" : \"$i\", \"password\" : \"$i\" }" "http://${HOST}:${PORT}/api/user"
   let i=i+1
done
