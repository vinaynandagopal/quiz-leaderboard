# Quiz Leaderboard System

A Java solution for the SRM Internship Assignment — consumes a quiz API, deduplicates responses, aggregates scores, and submits a final leaderboard.

---

## Problem Summary

- Poll a quiz API **10 times** (poll index 0–9) with a **5-second delay** between each call
- Each response contains score events: `{ roundId, participant, score }`
- The **same event can appear in multiple polls** (duplicates must be ignored)
- Deduplication key: `roundId + participant`
- Compute **total score per participant**, sort descending
- Submit the leaderboard **once**

---

## How It Works

```
Poll 0 → Poll 1 → ... → Poll 9
       ↓
Collect all events
       ↓
Deduplicate using (roundId + participant)
       ↓
Aggregate scores per participant
       ↓
Sort leaderboard by totalScore (desc)
       ↓
POST /quiz/submit  (once)
```

### Deduplication Logic

```java
String key = roundId + "|" + participant;
if (!deduplicatedEvents.containsKey(key)) {
    deduplicatedEvents.put(key, score);  // new event
} else {
    // duplicate — silently ignored
}
```

---

## Prerequisites

- Java 11+
- Maven 3.6+

---

## Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/your-username/quiz-leaderboard.git
   cd quiz-leaderboard
   ```

2. **Set your registration number**

   Open `src/main/java/com/quiz/QuizLeaderboard.java` and update:
   ```java
   private static final String REG_NO = "YOUR_REG_NO"; // e.g. "2024CS101"
   ```

3. **Build**
   ```bash
   mvn clean package
   ```

4. **Run**
   ```bash
   java -jar target/quiz-leaderboard-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

---

## Sample Output

```
=== Starting Quiz Leaderboard System ===

Polling 1/10 (poll=0)...
  Events received: 4 | New: 4 | Duplicates ignored: 0
  Waiting 5 seconds...

Polling 2/10 (poll=1)...
  Events received: 4 | New: 2 | Duplicates ignored: 2
  Waiting 5 seconds...
...

=== Final Leaderboard ===
  Alice                : 120
  Bob                  : 100
  -------------------------
  TOTAL SCORE (all users) : 220

=== Submission Response ===
{
  "isCorrect" : true,
  "isIdempotent" : true,
  "submittedTotal" : 220,
  "expectedTotal" : 220,
  "message" : "Correct!"
}

✅ SUCCESS! Leaderboard is correct.
```

---

## Project Structure

```
quiz-leaderboard/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/quiz/
                └── QuizLeaderboard.java
```

---

## Key Design Decisions

| Decision | Reason |
|---|---|
| `LinkedHashMap` for deduplication | Preserves insertion order, O(1) lookup |
| Dedup key = `roundId + participant` | Matches spec exactly |
| 5-second delay between polls | Mandatory per API requirements |
| Single POST submit | Spec says submit only once |
| Jackson for JSON | Lightweight, widely used |
