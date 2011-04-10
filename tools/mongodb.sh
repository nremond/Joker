#!/bin/bash
#
# MongoDB Joker start script
# does all the tricky work of starting up correctly the following components:
# - 3 mongod shard & replica set instances (one master and two slaves replica sets)
# - 1 mongos routing service
# - 1 mongod config server (only on the first three virtual machines)

REPLICA_SET_PREFIX="jokerRepl"

# Total number of nodes for the application
# TODO: change me
NB_NODES=3

CURRENT_IP=$(ifconfig  | grep 'inet addr:'| grep -v '127.0.0.1' | cut -d: -f2 | awk '{ print $1}')
NODE_NUMBER=$(echo $CURRENT_IP | awk -F. '{print $4}')

START_CONFIG=0

# MongoDB settings
MONGODB_PORT_1=10001
MONGODB_PORT_2=10002
MONGODB_PORT_3=10003

CONFIGDB_PORT=20000

MONGODB_FOLDER=/home/user/mongodb/
LOG_FOLDER=${MONGODB_FOLDER}logs/

create_folder() {
	local FOLDER=$1
	if [[ ! -d $FOLDER ]]; then
		mkdir -p $FOLDER
	fi	
}

start_db() {
	local REPLSET=$1
	local PORT=$2
	
	local FOLDER=${MONGODB_FOLDER}${REPLSET}
	create_folder $FOLDER
	
	mongod --shardsvr --dbpath ${FOLDER} --replSet $REPLSET --port $PORT --journal --logpath ${LOG_FOLDER}${REPLSET}.log --logappend --fork
}

start_config() {
	local FOLDER=${MONGODB_FOLDER}config
	create_folder $FOLDER

	mongod --configsvr --dbpath ${FOLDER} --port $CONFIGDB_PORT --journal --logpath ${LOG_FOLDER}config.log --logappend --fork
}

start_routing() {
	sleep 2
	mongos --configdb 192.168.1.2:$CONFIGDB_PORT,192.168.1.3:$CONFIGDB_PORT,192.168.1.4:$CONFIGDB_PORT --chunkSize 5 --logpath ${LOG_FOLDER}mongos.log --logappend --fork
}

stop_all() {
	# enough for a clean stop
	killall -q mongos
	sleep 2
	killall -q mongod
}

case "$1" in
	stop)
		stop_all
		exit 0
	;;
esac

echo "[INFO] Detected node number $NODE_NUMBER (total number of nodes is $NB_NODES)"

# compute replica set names
let "SET_1 = $NODE_NUMBER - 1"
let "SET_2 = $NB_NODES - 2 + $NODE_NUMBER"
if [[ $SET_2 -gt NB_NODES ]]; then
	let "SET_2 = $SET_2 % $NB_NODES"
fi
if [[ $SET_2 -eq 1 ]]; then
	let "SET_3 = $NB_NODES"
else
	let "SET_3 = $SET_2 - 1"
fi

SET_1="${REPLICA_SET_PREFIX}${SET_1}"
SET_2="${REPLICA_SET_PREFIX}${SET_2}"
SET_3="${REPLICA_SET_PREFIX}${SET_3}"

# start a config process only on the first three nodes (first node start at 2!)
if [[ $NODE_NUMBER -le 4 ]]; then
	START_CONFIG=1
fi

if [[ $START_CONFIG -gt 0 ]]; then
	echo "[INFO] Node will host one mongoDB config process"
fi

echo "[INFO] Master replica set is $SET_1 on port $MONGODB_PORT_1"
echo "[INFO] First slave replica set is $SET_2 on port $MONGODB_PORT_2"
echo "[INFO] Second slave replica set is $SET_3 on port $MONGODB_PORT_3"

# Be sure to create all the needed folder or else mongoDB will not start
create_folder $LOG_FOLDER

# Start main DB processes
start_db $SET_1 $MONGODB_PORT_1
start_db $SET_2 $MONGODB_PORT_2
start_db $SET_3 $MONGODB_PORT_3

# Start the config process
if [[ $START_CONFIG -gt 0 ]]; then
	start_config
fi

# Start the mongo routing process
start_routing
