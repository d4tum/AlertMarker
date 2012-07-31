AlertMarker
==================
This is the Android app AlertMarker created as part of the three day Android App Programming workshop run in July 2012 by the Clayton School of Information Technology at Monash University.

The app implements proximity alerts by placing markers through long pressing a position on a map.
The purpose of the app is to introduces the 4 components of an Android app namely:
- Activities
- Services
- Content providers
- Broadcast receivers

More information about these components can be found at http://developer.android.com/guide/components/fundamentals.html

The code is non-trivial and covers other topics such as:
- Map Overlays:
	- My Location
	- Itemized Overlay
	- Gesture Listener
- SQLite database
- PreferenceActivity
- Action bar UI design pattern
- Theming
- AlertDialog
- Location Listener


Proximity alerts are implemented using ```xml locationManager.addProximityAlert(double latitude, double longitude, float radius, long expiration, PendingIntent intent) ```
from the ```xml android.location.LocationManager ``` class in a ```xml Service ```. An unique ```xml PendingIntent ``` is created for each proximity alert. When an alert fires, a ```xml BroadcastReceiver ``` captures it and sends a ``` xml Notification ``` to the devices status bar.

##Requirements
For the map to work, a valid Google Maps Android API Key will need to be obtained - see https://developers.google.com/maps/documentation/android/mapkey
The app uses ActionBarSherlock for its Action bar - http://actionbarsherlock.com
This should be added as a library project. The project should also be built against the Google APIs for Platform 4.1 API 16 with the Java Compiler Compliance level set at 1.6.

Copyright 2012 Matthew Browne

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


