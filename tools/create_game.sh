#!/bin/bash

PORT="9080"
HOST="localhost"
FLUSHUSERSTABLE="true"
NBUSERS=2
QUESTIONTIMELIMIT=20
NBQUESTIONS=3
SYNCHROTIME=2

#curl "http://${HOST}:${PORT}/api/join/localhost/7911";

# Create a game with 3 questions
curl -X POST -d "{ \"authentication_key\" : \"1234\", \"parameters\" : { \"questions\" : [ { \"goodchoice\" : 2, \"label\" : \"Question1\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question2\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question3\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 4, \"label\" : \"Question4\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question5\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question6\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 3, \"label\" : \"Question7\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question8\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 4, \"label\" : \"Question9\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question10\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question11\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 3, \"label\" : \"Question12\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 4, \"label\" : \"Question13\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question14\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question15\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question16\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question17\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question18\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question19\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 3, \"label\" : \"Question20\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] } ], \"parameters\" : { \"logintimeout\" : 600, \"synchrotime\" : ${SYNCHROTIME}, \"nbusersthreshold\" : ${NBUSERS}, \"questiontimeframe\" : ${QUESTIONTIMELIMIT}, \"nbquestions\" : ${NBQUESTIONS}, \"flushusertable\" : ${FLUSHUSERSTABLE}, \"trackeduseridmail\" : \"unused\" } } }" "http://${HOST}:${PORT}/api/game"

# Create $NBUSERS users
i=1
while [[ $i -le ${NBUSERS} ]]
do
   curl -X POST -d "{ \"firstname\" : \"$i\", \"lastname\" : \"$i\", \"mail\" : \"$i\", \"password\" : \"$i\" }" "http://${HOST}:${PORT}/api/user"
   let i=i+1
done
