# Quiz Leaderboard System

This is my Java solution for the SRM internship coding assignment.

The task was to call a quiz API 10 times, collect score events, remove duplicate events, calculate the total score for each participant, build the final leaderboard, and submit it once.

## What the problem is about

The API returns quiz events in this format:

```json
{ "roundId": "R1", "participant": "Alice", "score": 10 }

The catch is that the same event can appear again in later API responses.
So if duplicates are not handled properly, the final scores will be wrong.
```
The required deduplication key is:
```
roundId + participant
```
Approach
The solution follows these steps:     
       Poll the API 10 times using poll=0 to poll=9
       Wait 5 seconds between each poll as required
       Read all events from each response
       Ignore duplicate events using roundId + participant
       Add valid scores to each participant’s total
       Sort the leaderboard in descending order of total score
       Submit the final leaderboard once
       Deduplication idea
       If the same roundId + participant combination appears again, it is skipped.

Example:
       R1 + Alice + 10 appears in poll 0
       The same event appears again in poll 3
       It should only be counted once

Tech used
       Java
       Java HttpClient
       Jackson for JSON parsing
       Maven for dependency management
Prerequisites
       Java 11 or above
       Maven 3.6 or above

How to run
Clone the repository
```
git clone https://github.com/your-username/quiz-leaderboard.git
cd quiz-leaderboard
```

Build the project
```
mvn clean package
```

Run the application
```
mvn exec:java "-Dexec.mainClass=com.srm.quiz.QuizLeaderboardApp" "-Dexec.args=YOUR_REG_NO"
```

Example:
```
mvn exec:java "-Dexec.mainClass=com.srm.quiz.QuizLeaderboardApp" "-Dexec.args=RA2311030010031"
```

Project structure
```
quiz-leaderboard/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── srm/
                    └── quiz/
                        └── QuizLeaderboardApp.java
```

Notes
       The program polls exactly 10 times
       It keeps a set of processed roundId + participant keys
       Duplicate events are ignored
       Scores are aggregated correctly per participant
       The final leaderboard is submitted only once

Why this solution is correct
This solution matches the assignment requirements exactly:
       10 polls
       5-second delay between requests
       deduplication based on roundId + participant
       leaderboard sorted by total score
       one final submission

Sample outcome
After processing all events, the application prints:
-the final leaderboard
-the total combined score
-the API response from the submission endpoint

If the submission is correct, the response should show something like:
```
{
  "isCorrect": true,
  "isIdempotent": true,
  "submittedTotal": 220,
  "expectedTotal": 220,
  "message": "Correct!"
}
```





