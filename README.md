# Distribroot 
## Udacity Android Developer Nanodegree capstone project 

## Overview
Distribroot makes planning and mobilizing your neighborhood distributions powerful, simple, and  fast. Create a distributor profile to notify subscribers of updates, such as time changes or cancelations. Users can view local distributions on a map and subscribe to one or more distribution centers for updates. This capstone project showcases the skills learned throughout the nanodegree program, and spans the full development process.


## Screenshots
![Screen](https://raw.githubusercontent.com/kangarruu/Distribroot/master/distribroot_screenshot.png)

## Getting Started
In order to build the app you must provide your own Maps API key.

        1. Create a file secure.properties under Distribroot/secure.properties
        2. Add this line, where YOUR_API_KEY is your API key:
            MAPS_API_KEY=YOUR_API_KEY

## Features
*  Authenticates users 
*  Displays distributors within 10 kilometers of the user’s location on a google map using Fused Location Provider.
*  Handles data persistence by saving new distributor details to Firebase Database and Geofire
*  Uses Geolocation API to fetch Lat and lng coordinates for newly inputted distributors.
*  Allows users to subscribe to updates from favorite distributions
*  Sends notifications to users when updates have been added to their subscribed distributors (feature currently in development)
*   Uses Google’s architecture components for best practices, including ViewModel, LiveData, Repository pattern, Advanced Databinding

## Libraries and APIs
*   [Retrofit 2](https://github.com/square/retrofit)
*   [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
*   [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
*   [Geofire](https://github.com/firebase/geofire-java)
*   [Firebase Database](https://firebase.google.com/products/realtime-database)
*   [Firebase Authentication](https://firebase.google.com/docs/auth)
*   [Google Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview)
 *   [Geolocation API](https://developers.google.com/maps/documentation/geolocation/overview)


### Image Resources
*  Splashscreen photo by Ahmed Carter on Unsplash
<a href="https://www.vecteezy.com/free-vector/tree">Tree Vectors by Vecteezy</a>
