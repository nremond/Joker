#!/bin/bash

PORT="9080"
HOST="localhost"

# Create 100 users
for i in $(seq 1 100)
do
   curl -X POST -d "{ \"firstname\" : \"$i\", \"lastname\" : \"$i\", \"mail\" : \"$i\", \"password\" : \"$i\" }" 'http://${HOST}:${PORT}/api/user'
done

# Create a game with 3 questions
curl -X POST -d "{ \"questions\" : [ { \"goodchoice\" : 1, \"label\" : \"Question1\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question2\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\", \"choix5\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question3\", \"choices\" : [ \"choix1\", \"choix2\" ] } ], \"parameters\" : { \"longpollingduration\" : 600, \"nbusersthreshold\" : 4, \"questiontimeframe\" : 30, \"nbquestions\" : 3, \"flushusertable\" : true, \"trackeduseridmail\" : \"unused\" } }" 'http://${HOST}:${PORT}/api/game'

# Login, get first question, post answer and get ranking for 4 first users.



for i in $(seq 1 4)
do
	(curl -X POST -d "{ \"mail\" : \"$i\", \"password\" : \"$i\" }" -D $i.txt 'http://${HOST}:${PORT}/api/login';
	session=$(cat $i.txt | grep "Set-Cookie" | cut -d "\"" -f2);
	curl -b "session_key=$session" 'http://${HOST}:${PORT}/api/question/1';
	curl -X POST -b "session_key=$session" -d "{ \"answer\" : $i }" 'http://${HOST}:${PORT}/api/answer/1';
	curl -b "session_key=$session" 'http://${HOST}:${PORT}/api/ranking';)&
done


