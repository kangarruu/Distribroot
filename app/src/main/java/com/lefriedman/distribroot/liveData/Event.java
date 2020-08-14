package com.lefriedman.distribroot.liveData;

//Used as a wrapper for data that is exposed via a LiveData that represents an event.
//For distribroot use case used to avoid LiveData callback from displaying incorrect
//toast response to Geolocation Api call
// As per recommendation in:
// https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150

public class Event<T> {

    private T mContent;

    private boolean hasBeenHandled = false;

    public Event(T content) {
        if (content == null) {
            throw new IllegalArgumentException("null values in Event are not allowed.");
        }
        mContent = content;
    }

    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return mContent;
        }
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }
}

