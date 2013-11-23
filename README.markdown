AutoHud
=======

A Google Glass app that displays your car's speed, tachometer, fuel, and instantaneous MPG in real time.

Requires an ELM327-based OBD-to-Bluetooth adapter, available for less than $15. Compatible with all cars sold after 1996.

Installing this Glassware adds a new voice command to your Google Glass, "Show Dashboard". Run the command to insert a live card capable of showing information straight from your car's Engine Control Unit.

AutoHud was developed by Zack Freedman of Voidstar Lab, and is the first publicly-released GDK app to connect to third-party hardware. It is released under the Apache 2.0 license.

If your OBD II adapter has a Bluetooth name that isn't "OBDII", you'll need to recompile the app. See below.

## Requirements
- A car with an OBD II port. If your car was sold in the US after 1996, this is legally required.
- An ELM327-based OBD-to-Bluetooth adapter. I designed this app to use with [this inexpensive dongle](http://www.amazon.com/gp/product/B00AAOOQJC/ref=oh_details_o00_s01_i00?ie=UTF8&psc=1 "Vgate Scan"). 
- Optional: An [OBD II extender cable](http://www.amazon.com/gp/product/B00C6SLP94/ref=oh_details_o00_s00_i00?ie=UTF8&psc=1 "ASSEM Cable") if your car's OBD II port is in an inconvenient location.
- A computer with adb installed and included in your $PATH. There are many easy-to-follow guides on how to do this.
- A Bluetooth keyboard. This is only required for setup.
- A Google Glass. Possibly costs more than the car.

## Setup Instructions
1. Double-check the Requirements and make sure you have everything.

2. Find and download [Settings.apk](http://www.glassxe.com/2013/05/23/settings-apk-and-launcher2-apk-from-the-hacking-glass-session-at-google-io/ "Enjoy your free link juice, GlassXE"). Install it on your Glass by connecting it to your computer, opening your favorite command shell, and running `adb install -r [path to Settings.apk]`. Do not take [path to Settings.apk] literally and actually type [path to Settings.apk] into the Terminal. You're smarter than that.

3. Download VoidstarAutoHud.apk from right here and install it the same way.

4. Get into the car and plug the dongle into the OBD II port. Its power light will illuminate.

5. Open Settings.apk and pair the Bluetooth keyboard with your Glass. Pair with the OBDII dongle. Usual passwords are 0000, 1234, and 6789. Throw the keyboard out the window, you're done with it.

6. If your dongle has a name other than "OBDII", you will need to recompile and reinstall the app. See below.

7. Turn your car's ignition. **This is not optional. Your car's ECU is only active when the engine is running.**

7. Return to the Glass clock. Say "OK Glass, show dashboard." Tap, hit Connect.

8. After a few seconds, RPM, fuel, and MPG should appear. Welcome to the future!

9. Drive safely. Wearing Glass while driving is legal in most states, but you have a run-in with the law, it will set a legal precedent that will get many otherwise innocent people into trouble. Remember, automobile crimes on interstate highways are felonies. I strongly suggest testing the app on a track or abandoned lot instead of the road.

10. When you're done driving, tap the AutoHud card, select Stop, and tap it. **Unplug the OBD dongle when you turn off the ignition or it will drain your battery!**

## Special Instructions
If your dongle is called something other than "OBDII", AutoHud will not detect it. For the time being, the name is hardcoded in. To compile a custom version with your dongle's name, follow these steps:

1. Install the Android Developer Toolkit, sdk-tools, and the GDK.

2. Pull AutoHud's source from GitHub and open it in ADT Eclipse.

3. Open the file "strings.xml" in the folder "values" in the folder "res". Click "obd_adapter_name" and change its Value to the name of your OBD II dongle.

4. Connect your Glass to your computer. Click Run → Run As → Android Application. Select your Glass and your new, tweaked version should install.

## Attribution
The application structure is based off the Google Glass "Compass" GDK sample and used under Apache 2.0.

The icons are taken from Font Awesome and used under SIL OFL 1.1. Font Awesome was developed by Dave Gandy and is available at http://fontawesome.io. It totally rules and you should download it.