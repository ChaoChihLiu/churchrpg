./gradlew clean build customFatJar
java -jar -Dapp.properties=/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app.properties
-Dpublish.date=2023-02-27 -Duse.polly=false build/libs/churchrpg-1.0-SNAPSHOT.jar ;

ssh -i calvarypandan-it-admin.pem ec2-user@X.X.X.X

db_url=jdbc:mysql://172.30.0.105:3306/calvarypandan
db_username=rpgtoaudio
db_password=rpgt0@udio

ssh -L 3306:localhost:3306 -L 3307:localhost:3307 -i
/Users/liuchaochih/Documents/GitHub/churchrpg/access/calvarypandan-it-admin.pem ec2-user@3.0.103.226

