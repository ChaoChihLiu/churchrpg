# CPBPC TTS Service

including RPG, Daily Remembrance, KJV and CUVS...or more in the future

## Prerequisites
1. openJDK 17
2. gradle-7.5.1

## Getting Started

### How to compile

* compile: ./gradlew build --stacktrace
* upload fatjar to server: ./gradlew clean customFatJarV2; ./access/uploadApp.sh;

### Executing program

* connect to working environment: access/tunnel.sh

1. RPG
* app-{language}.properties
```properties
##data source, content comes from mysql, but bible verses come from bible gateway
language=zh
bible_version=CUV
db_url=jdbc:mysql://{private ip}:3306/db_calvarypandan?useUnicode=true&characterEncoding=UTF-8
db_username=
db_password=
content_category=89

## read RPG content from ... to ...
date_from=2025-02-01
date_to=2025-03-10

## merge clips to a complete audio
audio_merged_bucket=cpbpc-rpg-audio
audio_merged_prefix=rpg-chinese/
audio_merged_format=mp3

## tts audio clips
output_bucket=cpbpc-rpg-audio
output_format=mp3
output_prefix=rpg-chinese/

## tts script 
script_bucket=cpbpc-rpg-script
script_format=xml
script_prefix=rpg-chinese/

## tts service config
name_prefix=crpg
engine=neural
voice_id=zh-CN-YunyangNeural
style=narration-relaxed
speech_speed=75%
speech_volume=loud

## cloud config
region=ap-southeast-1
cloud_sys=azure
```
* run a particular day of RPG
```shell
cd rpg/
./runDailyRPGAudio-english.sh 2025-01-16 true
./runDailyRPGAudio-chinese.sh 2025-01-16 true

#./runDailyRPGAudio-{language}.sh {date} {convert to audio}
```
* run a particular day of RPG
```shell
cd rpg/
nohup ./runRPGAudio-chinese.sh &
nohup ./runRPGAudio-english.sh &

#this cmd converts all RPG articles according to date range in properties, need to run with nohup
```
* schedule cronjob to convert RPG
```shell
30 10 25 * * screen -d -m /home/ec2-user/rpg/scheduleRPGAudio-english.sh
30 9 25 * * screen -d -m /home/ec2-user/rpg/scheduleRPGAudio-chinese.sh

## need to change date range in properties file before running cronjob
```
* other scripts
scripts below are called by the scripts above, purpose is to stop audio auto-merging, or auto-merging will slow down tts progress. 
```shell
./stopChAutoMerge.sh
## stop s3 bucket triggering Lambda function to auto-merge Chinese audio clips
./stopEnAutoMerge.sh
## stop s3 bucket triggering Lambda function to auto-merge English audio clips
./restartAudoMerge.sh                                     
## restart s3 bucket triggering Lambda function to auto-merge audio clips
```
2. Daily Remembrance
Articles are read from mysql too.
* app-devotion.properties
most configuration are same as RPG, only highlight difference below
```properties
## with this search criteria, system convert remembrance month by month
search_criterion=May
##use different AI voice to generate morning and evening devotion
morning_voice_id=en-US-AndrewNeural
evening_voice_id=en-CA-ClaraNeural
```

```shell
cd daily-remembrance/
nohup ./runRem-en.sh &
```
* other scripts
  scripts below are called by the scripts above, purpose is to stop audio auto-merging, or auto-merging will slow down tts progress.
```shell
./stopRemAutoMerge.sh
## stop s3 bucket triggering Lambda function to auto-merge audio clips
./restartAutoMerge.sh.sh                                     
## restart s3 bucket triggering Lambda function to auto-merge audio clips
```
3. KJV & CUVS
* verses come from: <br/>
KJV: <a href="https://www.biblegateway.com/passage/?search=ROM1&version=KJV" target="_blank">Bible Gateway</a>
CUVS: <a href="https://www.biblegateway.com/passage/?search=2%20Chronicles%2021&version=CUVS" target="_blank">Bible Gateway</a>

* app-bibleplan-{language}.properties
  most configuration are same as RPG, only highlight difference below
```properties
##use different AI voice to generate audio for every books
voice_id=en-CA-ClaraNeural
## follow bible reading plan, put books and chapters here, system generates audio files accordingly
day_plan=2Kgs7-8,2Kgs9-10,2Kgs11-12,2Kgs13-14,2Kgs15-16...
```
```properties
## but for CUVS, need to convert book name into base64
## 诗1-3,诗4-6...
day_plan=%E8%AF%971-3,%E8%AF%974-6...
```

```shell
cd bible-reading/
nohup ./runBiblePlan-en.sh &
nohup ./runBiblePlan-ch.sh &
```
* other scripts
  scripts below are called by the scripts above, purpose is to stop audio auto-merging, or auto-merging will slow down tts progress.
```shell
./stopKJVAutoMerge.sh
## stop s3 bucket triggering Lambda function to auto-merge Chinese audio clips
./stopCUVSAutoMerge.sh
## stop s3 bucket triggering Lambda function to auto-merge English audio clips
./restartAutoMerge.sh                                     
## restart s3 bucket triggering Lambda function to auto-merge audio clips
```
4. 
```shell

```
5. 


