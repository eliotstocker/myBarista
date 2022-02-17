# My Barista

An android application to control amd view data from the meCoffee PID module for Rancilio Silvia Machines
it is intended as a replacement for the MeBarista app that used to be available.

## What features are included?

 * Temperature Readouts
 * Graphs of set temp and actual temperature
 * Shot timer (that actually works)
 * Light/Dark themes
 * Temperature control
 * all settings from original app
 * Keep screen on when connected to coffee machine
 * Bluetooth LE only support (BT modules not supported)

## Thanks and dependencies
Much of this code was created by reading through the source of the original application here: https://git.mecoffee.nl/meBarista/meBarista_for_Android/src/master/src/nl/digitalthings/mebarista

### Dependencies:
 * [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - a great native android charting library
 * [EasyPermissions](https://github.com/googlesamples/easypermissions) - does what it says on the tin!
 * [NumberSlidingPicker](https://github.com/sephiroth74/NumberSlidingPicker) - a great number picker used for the main UI temperature selector

**THIS APPLICATION IS CURRENTLY A WORK IN PROGRESS AND DOES NOT FUNCTION 100% TO A PRODUCTION READY STANDARD**

## Known Issues and Todos
 - [ ] Not all settings are being applied correctly every time
 - [ ] Some elements of the dark theme are not being correctly applied
 - [ ] Only BLE is supported
 - [ ] Some settings are missing
 - [ ] Some app crashed have been noticed but not fully identified
 - [ ] Android 12 bluetooth permissions are not yet fully working (cant find device on android 12 devices yet)
 - [ ] Icons etc not yet added