# IndoorPositioning

Indoor Positioning System using WiFi Access Points and Pedestrian Dead Reckoning (PDR). 

## Activities
The project includes two main activities:  
1. The first activity allows you to sample, record, and upload WiFi RSSI values  
2. The second activity allows you to see your current position indoors.  

<p align="center">
    <img src="/image/main_activity.jpg" height="600" alt="Main Activity"/>
    <img src="/image/rssi_activity.jpg" height="600" alt="RSSI Sampling Activity"/>
    <img src="/image/positioning_activity.jpg" height="600" alt="Indoor Positioning Activity"/>
</p>

## Technologies
- Android Studio
- Firebase Cloudstore Database for storing the RSSI Fingerprints
- Gaussian Process Regression (python script) to increase the resolution of the fingerprint

## Guides

### How to install the app
1. Download the repository files, and open the IndoorPositioningApp folder in Android studio
1. All the settings that can be changed can be found in the IndoorPositioningSettings.java class
1. You must drag your database's “google-services.json” file into the “IndoorPositioningApp/App” directory in order to be able to connect to the database
1. You can now install the app on your phone from android studio

### How to record an RSSI Fingerprint
1. Install and open the Indoor Positioning App
1. Click on “Collect RSSI Data”
1. Disconnect from any wifi network. From experiments, this causes each WiFi 1. RSSI measurement to occur 1-2 seconds faster.
1. Stand at the first reference point, and use the arrow buttons to update your location so that “Reference X” and “Reference Y” reads your real location
1.  Face the north direction of the room
1. Click the “AUTO REFRESH & SUBMIT” button. This will refresh WiFi RSSI values 50 times, and save them to a file within the phone
1. Repeat steps 5-6 facing the three other directions
1. Repeat steps 4-7 for all other reference points
1. Once all reference points have been surveyed, click “Upload Data”. This will upload all the data to the database.

### How to process the fingerprint
1. Click “Process Distributions Data”. This will download all the raw data from the database, and process it into a new file containing the skew-normal distributions for each reference point.
1. Connect your phone to the android device
1. Open the following directory in the phone: “Android/data/com.scslab.indoorpositioning/files”
1. Copy the “distributions.json” file to the “IndoorPositioning/Python” directory in the repository
1. Run the DistributionRegression.py file. This will read the distributions, extend the data using Gaussian Process Regression, and display the results for each access point. Once you have closed all the graphs, four new files will be created, one for each direction (north_distributions.json, east_distributions.json, etc).
1. Copy the four distribution files into “Android/data/com.scslab.indoorpositioning/files”
1. You can not open the Indoor Localisation screen in the app, and it should work as expected.
