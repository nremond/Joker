#!/bin/bash

PORT="9080"
HOST="localhost"

NBUSERS=10
QUESTIONTIMELIMIT=60

# Create a game with 3 questions
curl -X POST -d "{ \"questions\" : [ { \"goodchoice\" : 1, \"label\" : \"Question1\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\", \"choix5\", \"choix6\", \"choix7\", \"choix8\", \"choix9\", \"choix10\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question2\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\", \"choix5\", \"choix6\", \"choix7\", \"choix8\", \"choix9\", \"choix10\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question3\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\", \"choix5\", \"choix6\", \"choix7\", \"choix8\", \"choix9\", \"choix10\" ] } ], \"parameters\" : { \"longpollingduration\" : 600, \"nbusersthreshold\" : ${NBUSERS}, \"questiontimeframe\" : ${QUESTIONTIMELIMIT}, \"nbquestions\" : 3, \"flushusertable\" : true, \"trackeduseridmail\" : \"unused\" } }" "http://${HOST}:${PORT}/api/game"

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
	(val=$i; let "choice = ${val} % 10 + 1"; curl -X POST -d "{ \"mail\" : \"$val\", \"password\" : \"$val\" }" -D $val.txt "http://${HOST}:${PORT}/api/login";
	session=$(cat $val.txt | grep "Set-Cookie" | sed -e "s/^Set-Cookie: session_key=\(.*\)\$/\1/" | sed -e "s/\"//g");
	curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/question/1";
	#let "e = $RANDOM % ${QUESTIONTIMELIMIT} / 2"; sleep ${e};
 	curl -X POST -b "session_key=\"$session\"" -d "{ \"answer\" : $choice }" "http://${HOST}:${PORT}/api/answer/1";
	curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/question/2";
	#let "e = $RANDOM % ${QUESTIONTIMELIMIT} / 2"; sleep ${e};
	curl -X POST -b "session_key=\"$session\"" -d "{ \"answer\" : $choice }" "http://${HOST}:${PORT}/api/answer/2";
	curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/question/3";
	##let "e = $RANDOM % ${QUESTIONTIMELIMIT} / 2"; sleep ${e};
	curl -X POST -b "session_key=\"$session\"" -d "{ \"answer\" : $choice }" "http://${HOST}:${PORT}/api/answer/3";
	curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/ranking";)&
	let i=i+1
done

