package com.lefriedman.distribroot.livedata;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DataSnapshotLiveData extends LiveData<DataSnapshot> {
    private static final String TAG = DataSnapshotLiveData.class.getSimpleName();

    private final Query mQuery;
    private final FirebaseValueEventListener eventListener = new FirebaseValueEventListener();

    public DataSnapshotLiveData(Query query) {
        mQuery = query;
    }

    public DataSnapshotLiveData(DatabaseReference ref) {
        mQuery = ref;
    }

    @Override
    protected void onActive() {
        Log.d(TAG, "onActive");
        mQuery.addValueEventListener(eventListener);
    }

    @Override
    protected void onInactive() {
        Log.d(TAG, "onInactive");
        mQuery.removeEventListener(eventListener);

    }

    private class FirebaseValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            setValue(snapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e(TAG, "FirebaseValueEventListener onCancelled: " + mQuery, error.toException());
        }
    }
}
