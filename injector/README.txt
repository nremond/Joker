Injector usage : 

java \
	-Dcom.ning.http.client.AsyncHttpClient.defaultMaxTotalConnections=10000 \
	-Dcom.ning.http.client.AsyncHttpClient.defaultMaxConnectionsPerHost=10000 \
	-jar joker-injector-shaded.jar "gateway host" [ "(int)gateway port" [ "(int)number of users" ] ]


!! you need to have the file located at ../tools/1million_users_1.csv otherwhise it will fail.



