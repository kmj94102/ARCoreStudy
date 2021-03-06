package com.example.arcorestudy

import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

/** Helper class for Firebase storage of cloud anchor IDs.  */
internal class StorageManager(context: Context?) {
    /** Listener for a new Cloud Anchor ID from the Firebase Database.  */
    internal interface CloudAnchorIdListener {
        fun onCloudAnchorIdAvailable(cloudAnchorId: String?)
    }

    /** Listener for a new short code from the Firebase Database.  */
    internal interface ShortCodeListener {
        fun onShortCodeAvailable(shortCode: Int?)
    }

    private val rootRef: DatabaseReference

    /** Gets a new short code that can be used to store the anchor ID.  */
    fun nextShortCode(listener: ShortCodeListener) {
        // Run a transaction on the node containing the next short code available. This increments the
        // value in the database and retrieves it in one atomic all-or-nothing operation.
        rootRef
            .child(KEY_NEXT_SHORT_CODE)
            .runTransaction(
                object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        var shortCode: Int? = currentData.getValue(Int::class.java)
                        if (shortCode == null) {
                            shortCode = INITIAL_SHORT_CODE - 1
                        }
                        currentData.value = shortCode + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                        if (!committed) {
                            Log.e(TAG, "Firebase Error", error?.toException())
                            listener.onShortCodeAvailable(null)
                        } else {
                            listener.onShortCodeAvailable(currentData?.getValue(Int::class.java))
                        }
                    }
                })
    }

    /** Stores the cloud anchor ID in the configured Firebase Database.  */
    fun storeUsingShortCode(shortCode: Int, cloudAnchorId: String?) {
        rootRef.child(KEY_PREFIX + shortCode).setValue(cloudAnchorId)
    }

    /**
     * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
     * was not stored for this short code.
     */
    fun getCloudAnchorID(shortCode: Int, listener: CloudAnchorIdListener) {
        rootRef
            .child(KEY_PREFIX + shortCode)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        listener.onCloudAnchorIdAvailable(java.lang.String.valueOf(dataSnapshot.getValue()))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            TAG, "The database operation for getCloudAnchorID was cancelled.",
                            error.toException()
                        )
                        listener.onCloudAnchorIdAvailable(null)
                    }
                })
    }

    companion object {
        private val TAG = StorageManager::class.java.name
        private const val KEY_ROOT_DIR = "shared_anchor_codelab_root"
        private const val KEY_NEXT_SHORT_CODE = "next_short_code"
        private const val KEY_PREFIX = "anchor;"
        private const val INITIAL_SHORT_CODE = 142
    }

    init {
        val firebaseApp = FirebaseApp.initializeApp(context!!)
        rootRef = FirebaseDatabase.getInstance(firebaseApp!!).reference.child(KEY_ROOT_DIR)
        DatabaseReference.goOnline()
    }
}