# IndoorPositioning

Indoor Positioning System using WiFi Access Points and Pedestrian Dead Reckoning (PDR). 

## Activities
The project's  main activities is shown in Figure 1. 
1. The first activity "COLLECT RSSI DATA" allows you to sample, record, and upload WiFi RSSI values. (See Figure 2) 
2. The forth activity "INDOOR LOCALISATION" allows you to see your current position indoors (Your position is the red dot, and the direction you are facing is the orange dot)  (See Figure 3)
3. The fifth activity "TEST ACCURACY" allows you to test the accuracy of the model.  (See Figure 4)

<p align="center"><b>Figure 1</b>. Main Page</p>

<img src="/image/main.png" height="600" alt="Main Activity"/>





<p align="center"><b>Figure 2</b>. Collect RSSI Data</p>

<img src="/image/rssi.png" height="600" alt="RSSI Sampling Activity"/>





<p align="center"><b>Figure 3</b>. Indoor Localisation</p>

<img src="/image/positioning_activity.jpg" height="600" alt="Indoor Positioning Activity"/>





<p align="center"><b>Figure 4</b>. Test Accuracy</p>

<img src="/image/accuracy.png" height="600" alt="Test Accuracy Activity"/>


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
1. Connect your phone to the computer
1. Open the following directory in the phone: “Android/data/com.scslab.indoorpositioning/files”
1. Copy the “distributions.json” file to the “IndoorPositioning/Python” directory in the repository
1. Run the DistributionRegression.py file. This will read the distributions, extend the data using Gaussian Process Regression, and display the results for each access point. Once you have closed all the graphs, four new files will be created, one for each direction (north_distributions.json, east_distributions.json, etc).
1. Copy the four distribution files into “Android/data/com.scslab.indoorpositioning/files”
1. You can not open the Indoor Localisation screen in the app, and it should work as expected.

### Test accuracy

1.   Stand in the middle of test points.
2.   Open Indoor Positioning App
3.   Click on “Test Accuracy”
4.   Use the arrow buttons to set “Reference X” and “Reference Y” to the real location of reference points.
5.   Place the phone on the top of boxes
6.   Adjust the orientation of the phone to the North.
7.   Click on the “AUTO REFRESH & SUBMIT” button and wait.
8.   Repeat steps 5 - 6 for East, South and West directions.
9.   Repeat steps 1 - 7 for other test points.
10.   The result is stored in the "accuracy_records.json" file on the phone. To find the result file, connect the phone with the computer, click “Internal shared storage” under the “devices and drives”, enter the “Android/data/com.scslab.indoorpositioning/files” folder
