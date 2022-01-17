# IndoorPositioning
Indoor Positioning System

Includes an Android Application as the final client
Utilises a Firebase Cloudstore Database for RSSI Fingerprinting
Uses GPR Models in python to increase the resolution of the fingerprint



Step 1: Connect the app to your Firebase Cloudstore Database
Step 2: Launch the app and take RSSI measurements of the room
Step 3: Once all RSSI measurements are complete, connect the Java Preprocessor to the same Firebase Cloudstore Database and launch it. It will analyse the data distributions and upload distribution models into a new fingerprint
Step 4: Connect the Python Preprocessor to the Firebase Cloudstore Database and launch it. It will use GPR to extend the distribution models
Step 5: The original Android App can now be launched, and it will start to estimate your indoor position
