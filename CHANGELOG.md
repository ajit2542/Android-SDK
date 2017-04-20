# Changelog

## 2.3.3 (2017-04-20)
#### Added features
- Updating android dependencies to the latest version


## 2.3.1 (2016-11-23)
#### Bugs fixed
- Correcting indoor tiles providers


## 2.3.0 (2016-11-23)
#### Added features
- Using the new www.goindoor.co servers
- Removing httpclientandroidlib dependency

#### Bugs fixed
- Improving instruction creation


## 2.2.1 (2016-07-27)
#### Added features
- Improved closest algorithm
- Projection is done in edges that fulfill the properties

#### Bugs fixed
- Correcting beacon maintenance in multiple floors


## 2.2.0 (2016-05-11)
#### Added features
- Introducing DDB
- Introducing Offline tiles

#### Bugs fixed
- Correcting behavior when no buildings are present in the workspace
- Correcting a bug that might not decode some edge properly under some circumstances
- Improving data sync
- Adding WebService UTF-8 support

## 2.1.1 (2016-03-10)
#### Bugs fixed
- Correcting a bug that might not update to the most updated data when starting the SDK


## 2.1.0 (2016-03-07)
#### Added features
- Adding offline mode
- Introducing proxibeacons
- Removing Links library

#### :warning: Deprecated methods
- x and y variables have been deprecated in favor of longitude and latitude
- Introducing new Geometry class


## 2.0.6 (2016-01-22)
#### Bugs fixed
- Improving switching floors experience


## 2.0.5-HOTFIX (2015-12-16)
#### Bugs fixed
- Updating Google Play Services to 8.3.0
- Updating build tools to 23.0.2
- Updating support library to 23.1.1


## 2.0.5 (2015-12-14)
#### Bugs fixed
- Correcting how floor ID is computed in LocationResult


## 2.0.4 (2015-11-17)
#### Added features
- Adding beta feature: Force BLE scan
- Location algorithm can be changed after GoIndoor initialization

#### Bugs fixed
- Correcting support for stairs, lifts and escalators


## 2.0.3 (2015-10-21)
#### Bugs fixed
- Fixing asset manager bug that might stop the SDK under certain conditions
- Correcting treatment of empty strings for Notification targets


## 2.0.2 (2015-10-05)
#### Added features
- Introducing LOCATION_TYPE_PROJECT

#### Bugs fixed
- Routing returns null if there is no route found


## 2.0.0 (2015-09-18)
#### Added features
- Introducing new GoIndoor class
- Multimodal navigation
- Asset finder
- Automatic maintenance


## 1.4.0 (2015-07-16)
#### Added features
- Adding Eddystone support

#### Bugs fixed
- Correcting how nearby notifications are triggered


## 1.2.1 (2015-06-16)
#### Bugs fixed
- When updating an iBeacon, it should be possible to update the live data properties and the normal properties in one call
- Adding security level when retrieving objects: If an object has incorrect format will be dismissed
- No buildings exception is thrown when retrieving buildings with ids that doesn't exists
- Correcting notifications triggering system


## 1.2.0 (2015-05-28)

#### Added features
- Notification support


## 1.0.0 (2015-05-01)
- Initial commit
