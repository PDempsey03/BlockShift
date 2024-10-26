package com.blockshift.model.repositories

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object HighScoreRepository {
    private lateinit var dataBaseHighScores: CollectionReference

    private val TAG: String = javaClass.simpleName

    init{
        loadUserDataBase()
    }

    private fun loadUserDataBase(){
        dataBaseHighScores = FirebaseFirestore.getInstance().collection(HighScoreTableNames.HIGH_SCORES)
    }

    fun updateHighScore(newHighScoreData: HighScoreData, onSuccessCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseHighScores
            .whereEqualTo(UserTableNames.USERNAME, newHighScoreData.username)
            .whereEqualTo(HighScoreTableNames.LEVEL_ID, newHighScoreData.levelID)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // first time beating level, so upload the high score data
                    dataBaseHighScores
                        .add(newHighScoreData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully create a new high score record")
                            onSuccessCallback(true)
                        }
                        .addOnFailureListener(onFailureCallback)
                } else {
                    // subsequent time beating, level so check if beat any high scores
                    val highScoreDoc = querySnapshot.documents[0]
                    val highScoreDocRef = highScoreDoc.reference

                    val storedHighScoreData = highScoreDoc.toObject(HighScoreData::class.java)

                    if(storedHighScoreData == null) {
                        Log.d(TAG, "Stored high score data was null thus couldn't update")
                        onSuccessCallback(false)
                        return@addOnSuccessListener
                    }

                    // check if any of the scores improved
                    val highScoreUpdates = hashMapOf<String, Any>()

                    val newTime = newHighScoreData.time
                    if(newTime < storedHighScoreData.time) {
                        highScoreUpdates[HighScoreTableNames.TIME] = newTime
                    }

                    val newMoves = newHighScoreData.moves
                    if(newMoves < storedHighScoreData.moves) {
                        highScoreUpdates[HighScoreTableNames.MOVES] = newMoves
                    }

                    val newDistance = newHighScoreData.distance
                    if(newDistance < storedHighScoreData.distance) {
                        highScoreUpdates[HighScoreTableNames.DISTANCE] = newDistance
                    }

                    // only need to write back to DB if any updates occurred
                    if(highScoreUpdates.isNotEmpty()) {
                        highScoreDocRef
                            .update(highScoreUpdates)
                            .addOnSuccessListener {
                                Log.d(TAG, "Successfully updated the high scores")
                                onSuccessCallback(true)
                            }
                            .addOnFailureListener(onFailureCallback)
                    } else {
                        Log.d(TAG, "No new high scores were found, so no updates occurred")
                        onSuccessCallback(true)
                    }
                }
            }
            .addOnFailureListener(onFailureCallback)
    }

    fun getHighScoresInRange(highScoreType: String, levelID: String, documentsToGet: Long,
                             onSuccessCallback: (List<HighScoreData>) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseHighScores
            .whereEqualTo(HighScoreTableNames.LEVEL_ID, levelID)
            .orderBy(highScoreType)
            .limit(documentsToGet)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onSuccessCallback(querySnapshot.toObjects(HighScoreData::class.java))
            }
            .addOnFailureListener(onFailureCallback)
    }
}

data class HighScoreData(
    val username: String = "",
    val levelID: String = "",
    val distance: Long = Long.MAX_VALUE,
    val moves: Long = Long.MAX_VALUE,
    val time: Long = Long.MAX_VALUE
)

internal object HighScoreTableNames{
    const val HIGH_SCORES = "highscores"
    const val LEVEL_ID = "levelid"
    const val DISTANCE = "distance"
    const val TIME = "time"
    const val MOVES = "moves"
}