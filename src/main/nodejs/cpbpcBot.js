import express from 'express';
import bodyParser from 'body-parser';
import fetch from 'node-fetch';
import fs from 'fs';
import properties from 'properties';

// Replace 'YOUR_API_TOKEN' with your bot's API token
let API_TOKEN = null;
let TELEGRAM_API_URL = null;
let WEBHOOK_URL = null;

const app = express();
app.use(bodyParser.json());

app.post('/webhook', (req, res) => {
    const update = req.body;
    console.info(`message is ${JSON.stringify(update)}`);
    if (update.message) {
        handleMessage(update.message);
    } else if (update.callback_query) {
        handleCallbackQuery(update.callback_query);
    }

    res.sendStatus(200);
});

const handleMessage = async (message) => {
    const chatId = message.chat.id;
    const text = message.text;

    if (text === '/start') {
        const keyboard = {
            inline_keyboard: [
                [
                    { text: 'Option 1', callback_data: '1' },
                    { text: 'Option 2', callback_data: '2' }
                ]
            ]
        };
        await sendMessage(chatId, 'Please choose:', keyboard);
    } else {
        await sendMessage(chatId, `You said: ${text}`);
    }
};

const handleCallbackQuery = async (callbackQuery) => {
    const chatId = callbackQuery.message.chat.id;
    const data = callbackQuery.data;

    await sendMessage(chatId, `You selected option: ${data}`);
};

const sendMessage = async (chatId, text, replyMarkup = null) => {
    const url = `${TELEGRAM_API_URL}sendMessage`;
    console.info( `send message url is ${url}` );
    let payload = null;
    if ( replyMarkup ){
        payload = {
            chat_id: chatId,
                text: text,
            reply_markup: replyMarkup
        };
    }
    if ( !replyMarkup ){
        payload = {
            chat_id: chatId,
            text: text
        };
    }

    // await fetch(url, {
    //     method: 'POST',
    //     headers: { 'Content-Type': 'application/json' },
    //     body: JSON.stringify(payload)
    // });

    try {
        console.info(`reply payload is ${text}`)
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}, ${JSON.stringify(response.body)}`);
        }

        const data = await response.json();
        console.log('Response from server:', data);
    } catch (error) {
        console.error('Error fetching data:', error.message);
    }
};

const readPropertiesAndSetToken = async (filePath) => {
    try {
        const data = fs.readFileSync(filePath, 'utf8');
        const obj = properties.parse(data);
        API_TOKEN = obj.telemgram_bot_api_key; // Assuming the key in properties is 'telemgram_bot_api_key'
        TELEGRAM_API_URL = `https://api.telegram.org/bot${API_TOKEN}/`;
        WEBHOOK_URL = `https://rpg-7f76ccf078caba5f.elb.ap-southeast-1.amazonaws.com:88/webhook`;

        console.info( `TELEGRAM_API_URL ${TELEGRAM_API_URL}` );
        console.info( `WEBHOOK_URL ${WEBHOOK_URL}` );
    } catch (error) {
        console.error('Error parsing properties:', error);
    }
};

app.listen(89, async () => {
    console.log('Bot server is running on port 89');
    await readPropertiesAndSetToken('../.secret');
    // checkAndSetWebhook();
});

app.get('/health', (req, res) => {
    res.status(200).send('Server is healthy');
});
const checkAndSetWebhook = async () => {
    try {
        console.info(`telegram api url : ${TELEGRAM_API_URL}getWebhookInfo`)
        const getWebhookInfoUrl = `${TELEGRAM_API_URL}getWebhookInfo`;
        const response = await fetch(getWebhookInfoUrl);
        const data = await response.json();

        if (data.result.url !== WEBHOOK_URL) {
            console.log('Setting webhook...');
            const setWebhookUrl = `${TELEGRAM_API_URL}setWebhook?url=${WEBHOOK_URL}`;
            const setResponse = await fetch(setWebhookUrl);
            const setData = await setResponse.json();

            if (setData.ok) {
                console.log('Webhook set successfully');
            } else {
                console.error('Error setting webhook:', setData);
            }
        } else {
            console.log('Webhook is already set');
        }
    } catch (error) {
        console.error('Error checking or setting webhook:', error);
    }
};
