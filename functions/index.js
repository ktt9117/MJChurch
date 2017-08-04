/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Import the Cloud Functions for Firebase and the Firebase Admin modules here.
const functions = require('firebase-functions');
// Import and initialize the Firebase Admin SDK
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Write the addWelcomeMessages Function here.
exports.addWelcomeMessages = functions.auth.user().onCreate(event => {
    const user = event.data;
    console.log('A new user signed in for the first time.');
    const fullName = user.displayName || user.email;
    const writer = {
        email: 'admin@mjchurch.org',
        name: '관리자'
    }

    // Saves the new welcome message into the database
    // Which then displays it in the FriendlyChat clients.
    return admin.database().ref('message').push({
        body: `${fullName}님이 가입하셨습니다! 환영합니다!`,
        timeStamp: Date.now(),
        writer: writer
    });
});

// Write the sendNotifications Function here.
exports.sendNotifications = functions.database.ref('/message/{body}').onWrite(event => {    
    const snapshot = event.data;
    console.log('a new message wrote in databases : ', snapshot.val());
    // Only send a notification when a message has been created.
    if (snapshot.previous.val()) {
        console.log('just return caused only send a notification when a message has been created');
        return;
    }

    // Notification details.
    const text = snapshot.val().body;
    const payload = {
        notification: {
            title: `${snapshot.val().writer.name}`,
            body: text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : '',
            click_action: `open_chat`
        }
    };

    const options = {
        priority: "high",
        timeToLive: 60*60*2
    };

    console.log('send messaging to topic "chat_room_topic"');
    return admin.messaging().sendToTopic("chat_room_topic", payload, options);
});