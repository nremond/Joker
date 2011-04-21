#!/bin/bash

PORT="9080"
HOST="localhost"
FLUSHUSERSTABLE="true"
NBUSERS=6
QUESTIONTIMELIMIT=10
SYNCHROTIME=2
NBQUESTIONS=9

#curl "http://${HOST}:${PORT}/api/join/localhost/7911";

# Create a game with 9 questions
curl -X POST -d "{ \"authentication_key\" : \"1234\", \"parameters\" : \"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;usi:gamesession xmlns:usi=&quot;http://www.usi.com&quot; xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xsi:schemaLocation=&quot;http://www.usi.com gamesession.xsd &quot;&gt;  &lt;usi:questions&gt;    &lt;usi:question goodchoice=&quot;1&quot;&gt;      &lt;usi:label&gt;This the quesion 1.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;1&quot;&gt;      &lt;usi:label&gt;This the quesion 2.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;2&quot;&gt;      &lt;usi:label&gt;This the quesion 3.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;2&quot;&gt;      &lt;usi:label&gt;This the quesion 4.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;        &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;3&quot;&gt;      &lt;usi:label&gt;This the quesion 5.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;3&quot;&gt;      &lt;usi:label&gt;This the quesion 6.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;4&quot;&gt;      &lt;usi:label&gt;This the quesion 7.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;4&quot;&gt;      &lt;usi:label&gt;This the quesion 8.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;    &lt;usi:question goodchoice=&quot;4&quot;&gt;      &lt;usi:label&gt;This the quesion 9.&lt;/usi:label&gt;      &lt;usi:choice&gt;Choix 1&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 2&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 3&lt;/usi:choice&gt;      &lt;usi:choice&gt;Choix 4&lt;/usi:choice&gt;    &lt;/usi:question&gt;            &lt;/usi:questions&gt;  &lt;usi:parameters&gt;    &lt;usi:logintimeout&gt;20&lt;/usi:logintimeout&gt;    &lt;usi:synchrotime&gt;${SYNCHROTIME}&lt;/usi:synchrotime&gt;    &lt;usi:nbusersthreshold&gt;${NBUSERS}&lt;/usi:nbusersthreshold&gt;    &lt;usi:questiontimeframe&gt;${QUESTIONTIMELIMIT}&lt;/usi:questiontimeframe&gt;    &lt;usi:nbquestions&gt;20&lt;/usi:nbquestions&gt;    &lt;usi:flushusertable&gt;${FLUSHUSERSTABLE}&lt;/usi:flushusertable&gt;    &lt;usi:trackeduseridmail&gt;usi:trackeduseridmail&lt;/usi:trackeduseridmail&gt;  &lt;/usi:parameters&gt;&lt;/usi:gamesession&gt;\" }" "http://${HOST}:${PORT}/api/game"

# Create $NBUSERS users
i=1
while [[ $i -le ${NBUSERS} ]]
do
   curl -X POST -d "{ \"firstname\" : \"$i\", \"lastname\" : \"$i\", \"mail\" : \"$i\", \"password\" : \"$i\" }" "http://${HOST}:${PORT}/api/user"
   let i=i+1
done

# Login, get first question, post answer and get ranking for $NBUSERS users.
i=1
while [[ $i -le ${NBUSERS} ]]
do
	(val=$i; curl -X POST -d "{ \"mail\" : \"$val\", \"password\" : \"$val\" }" -D $val.txt "http://${HOST}:${PORT}/api/login";
	session=$(cat $val.txt | grep "Set-Cookie" | sed -e "s/^Set-Cookie: session_key=\(.*\)\$/\1/" | sed -e "s/\"//g" | cut -d ";" -f 1);
	j=1
	while [[ $j -le ${NBQUESTIONS} ]]
	do
		curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/question/${j}";
		#let "e = $RANDOM % ${QUESTIONTIMELIMIT} / 2"; sleep ${e};
		let "choice = ${val} % 4 + 1";
 		curl -X POST -b "session_key=\"$session\"" -d "{ \"answer\" : $choice }" "http://${HOST}:${PORT}/api/answer/${j}";
		let "j = ${j} + 1"
	done
	let "sleep = ${QUESTIONTIMELIMIT} + ${SYNCHROTIME} + 1"
	sleep $sleep
	curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/ranking";)&
	let i=i+1
done

