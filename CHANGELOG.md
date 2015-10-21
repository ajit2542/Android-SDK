# Changelog

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
