curl --location --request POST "https://southeastasia.tts.speech.microsoft.com/cognitiveservices/v1" \
--header "Ocp-Apim-Subscription-Key: ${key}" \
--header 'Content-Type: application/ssml+xml' \
--header 'X-Microsoft-OutputFormat: audio-48khz-192kbitrate-mono-mp3' \
--header 'User-Agent: curl' \
--data-raw '<speak version='\''1.0'\'' xml:lang='\''en-US'\''>
    <voice xml:lang='\''en-US'\'' xml:gender='\''Female'\'' name='\''en-US-JennyNeural'\''>
        my voice is my passport verify me
    </voice>
</speak>' > output.mp3