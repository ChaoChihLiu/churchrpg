rm -rf bibleplan-en.log;
java -cp /Users/liuchaochih/Documents/GitHub/churchrpg/build/libs/churchrpg-1.0-SNAPSHOT.jar -Dapp.properties=/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-bibleplan-english.properties com.cpbpc.reading.plan.BibleAudio  2>&1 | tee bibleplan-en.log;
