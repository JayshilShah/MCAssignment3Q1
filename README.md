# MCAssignment3Q1
# Sensing App
The Sensing App is an Android application that utilizes the device's accelerometer and gyroscope sensors to track and record orientation data (roll, pitch, and yaw). The app provides a graphical representation of the orientation data over time and saves the data to a Room database.

## Features
### Real-time Orientation Tracking: 
The app listens to the device's accelerometer and gyroscope sensors and calculates roll, pitch, and yaw in real-time.
### Graphical Data Visualization: 
Orientation data is displayed graphically using line charts, allowing the user to easily observe changes in roll, pitch, and yaw over time.
### Data Storage:
Orientation data is stored in a local Room database, enabling historical data tracking and analysis.
### Data Export: 
Exporting orientation data to a CSV file (currently commented out in the code).
## Architecture
### MainActivity: 
Manages sensor events and orientation calculations. Displays orientation data and manages navigation.
### HistoryActivity: 
Displays orientation data graphs using MPAndroidChart.
### AppDatabase: 
Defines the Room database, including the OrientationDataDao data access object.
### MainViewModel: 
Manages the live data of orientation data and handles data insertion and retrieval.
## Technologies Used
Android Jetpack Components: Including Room, ViewModel, and LiveData.
MPAndroidChart: For graph visualization.
Kotlin: As the primary programming language for the project.
Compose: For the user interface design and layout.
## Setup and Installation
### Clone this repository:
shell
Copy code
git clone [(https://github.com/JayshilShah/MCAssignment3Q1/)]
Open the project in Android Studio.
Build and run the app on an emulator or physical Android device.
## Usage
Launch the app to start tracking orientation data.
The app will display the current roll, pitch, and yaw values in real-time.
Use the "History Graphs" button to navigate to the graph view and observe orientation data over time.
## Future Improvements
Add more features such as data export to CSV.
Implement additional sensor-based functionalities.
Enhance user experience and interface design.
