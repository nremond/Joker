#!/usr/bin/python
# -*- coding: latin-1 -*-


import sys
import string
from pymongo import Connection


def main():

	connection = Connection('localhost', 27017)
	db = connection.joker
	users = db.users

	fin = open('1million_users_1.csv','r')		
	for line in fin.readlines():	
		l = line.rstrip()
		t = l.split(',')
		lastname = t[0]
		firstname = t[1]
		email = t[2]
		password = t[3]

		new_user = [{"email": email,
					"password": password,
					"firstname": firstname,
					"lastname": lastname,
					"score": 0,
					"isLogged": False}]

		users.insert(new_user)
		#print "user inserted "+ lastname+","+firstname
	fin.close()

	print db.collection_names()


if __name__ == "__main__":
    main()

