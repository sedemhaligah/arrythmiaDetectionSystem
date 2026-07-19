# Arrhythmia Detection System

A Markov-model-based classifier for detecting cardiac arrhythmia intervals
from ECG-derived interval data.

## Structure
- `DataLoader.java` — loads interval data (`train.txt`, `eval_*.txt`)
- `MarkovClassifier.java` — Markov chain classifier
- `Interval.java` — interval/data model
- `Evaluator.java` — evaluates classifier accuracy against eval sets
- `Main.java` — entry point

## Build
```
mvn package
```
