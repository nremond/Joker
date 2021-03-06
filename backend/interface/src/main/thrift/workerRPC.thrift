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

struct ExtendedUserInfoAndScore {
	1: string userId,
	2: i32 score,
	3: string email,
	4: string firstname,
	5: string lastname,
	6: bool isLogged,
	7: i32 lastQuestionAnwered
}

struct BeforeAndAfterScores {
	1: list<UserInfoAndScore> beforeUsers,
	2: list<UserInfoAndScore> afterUsers
}

struct UserLogin {
	1: string userId,
	2: bool alreadyLogged
}

service WorkerRPC {

	UserAndScore validateUserAndInsertQuestionRequest(1: string userId, 2: i32 questionNumber),

	UserAndScore validateUserAndInsertQuestionResponseAndUpdateScore(1: string userId, 2: i32 questionNumber, 3: i32 questionValue, 4: i32 answer, 5: bool answerCorrect),

	UserAndScore validateUserAndGetScore(1: string userId),

	UserLogin loginUser(1: string email, 2: string password),

	bool insertUser(1: string email, 2: string password, 3: string firstname, 4: string lastname),

	void flushUsers(1: i32 useless),

	list<UserInfoAndScore> getTop100(1: i32 useless),
	list<UserInfoAndScore> get50Before(1: string userId),
	list<UserInfoAndScore> get50After(1: string userId),

	string getScoreAsJson(1: string email),

	string getAllAnswersAsJson(1: string email, 2: list<i32> goodAnswers),
	string getAnswerAsJson(1: string email, 2: i32 questionNumber, 3: string question, 4: i32 goodAnswer),

	BeforeAndAfterScores get50BeforeAnd50After(1: string userId),

	ExtendedUserInfoAndScore getExtendedUserInfo(1: string userId),

	void gameEnded(1: i32 useless),
	void gameCreated(1: i32 useless),
	
	void ping(1: i32 useless),
	
	list<UserInfoAndScore> getUsers(1: i32 from, 2: i32 limit)
	
}
