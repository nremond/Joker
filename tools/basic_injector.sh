#!/bin/bash

PORT="9080"
HOST="localhost"
FLUSHUSERSTABLE="true"
NBUSERS=3
QUESTIONTIMELIMIT=10

#curl "http://${HOST}:${PORT}/api/join/localhost/7911";

# Create a game with 3 questions
curl -X POST -d "{ \"questions\" : [ { \"goodchoice\" : 1, \"label\" : \"Question1\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question2\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question3\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 4, \"label\" : \"Question4\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question5\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question6\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 3, \"label\" : \"Question7\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question8\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 4, \"label\" : \"Question9\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question10\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question11\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 3, \"label\" : \"Question12\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 4, \"label\" : \"Question13\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question14\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question15\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question16\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question17\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question18\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question19\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 3, \"label\" : \"Question20\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] } ], \"parameters\" : { \"logintimeout\" : 600, \"synchrotime\" : 2, \"nbusersthreshold\" : ${NBUSERS}, \"questiontimeframe\" : ${QUESTIONTIMELIMIT}, \"nbquestions\" : 10, \"flushusertable\" : ${FLUSHUSERSTABLE}, \"trackeduseridmail\" : \"unused\" } }" "http://${HOST}:${PORT}/api/game"

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
	session=$(cat $val.txt | grep "Set-Cookie" | sed -e "s/^Set-Cookie: session_key=\(.*\)\$/\1/" | sed -e "s/\"//g");
	j=1
	while [[ $j -le ${QUESTIONTIMELIMIT} ]]
	do
		curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/question/${j}";
		#let "e = $RANDOM % ${QUESTIONTIMELIMIT} / 2"; sleep ${e};
		let "choice = ${val} % 4 + 1";
 		curl -X POST -b "session_key=\"$session\"" -d "{ \"answer\" : $choice }" "http://${HOST}:${PORT}/api/answer/${j}";
		let "j = ${j} + 1"
	done
	curl -b "session_key=\"$session\"" "http://${HOST}:${PORT}/api/ranking";)&
	let i=i+1
done

