namespace java cl.own.usi.thrift

struct UserAndScore {
	1: string userId,
	2: i32 score
}

struct UserInfoAndScore {
	1: string userId,
	2: i32 score,
	3: string email,
	4: string firstname,
	5: string lastname
}

service WorkerRPC {

	UserAndScore validateUserAndInsertQuestionRequest(1: string userId, 2: i32 questionNumber),
	
	UserAndScore validateUserAndInsertQuestionResponseAndUpdateScore(1: string userId, 2: i32 questionNumber, 3: i32 questionValue, 4: i32 answer, 5: bool answerCorrect),
	
	UserAndScore validateUserAndGetScore(1: string userId),
	
	string loginUser(1: string email, 2: string password),
	
	bool insertUser(1: string email, 2: string password, 3: string firstname, 4: string lastname),
	
	void flushUsers(),
	
	list<UserInfoAndScore> getTop100(),
	list<UserInfoAndScore> get50Before(1: string userId),
	list<UserInfoAndScore> get50After(1: string userId),
	
	void startRankingsComputation()
	
}
