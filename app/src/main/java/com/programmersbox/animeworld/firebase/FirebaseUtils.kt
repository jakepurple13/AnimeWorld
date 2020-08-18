package com.programmersbox.animeworld.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.programmersbox.animeworld.R
import com.programmersbox.loggingutils.Loged
import com.programmersbox.loggingutils.fa
import com.programmersbox.loggingutils.fd

object FirebaseAuthentication {

    private val RC_SIGN_IN = 32

    private var gso: GoogleSignInOptions? = null

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var googleSignInClient: GoogleSignInClient? = null

    fun authenticate(context: Context) {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso!!)
    }

    fun signIn(activity: Activity) {
        //val signInIntent = googleSignInClient!!.signInIntent
        //activity.startActivityForResult(signInIntent, RC_SIGN_IN)
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        activity.startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.AppTheme)
                .setLogo(R.drawable.ic_launcher_foreground)
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    fun signOut() {
        auth.signOut()
        //currentUser = null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, context: Context) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                //currentUser = auth.currentUser//FirebaseAuth.getInstance().currentUser
                Loged.fd(currentUser)
                // ...
                //val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                //val account = task.getResult(ApiException::class.java)!!
                //Loged.d("firebaseAuthWithGoogle:" + account.id)
                //googleAccount = account
                //firebaseAuthWithGoogle(account.idToken!!)
                Toast.makeText(context, "Signed in Successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Loged.fa(response?.error?.errorCode)
                Toast.makeText(context, "Signed in Unsuccessfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, context: Context, activity: Activity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                //Log.d(TAG, "signInWithCredential:success")
                //val user = auth.currentUser
                //updateUI(user)
                //currentUser = auth.currentUser
                Toast.makeText(context, "Signed in Successfully", Toast.LENGTH_SHORT).show()
            } else {
                // If sign in fails, display a message to the user.
                //Log.w(TAG, "signInWithCredential:failure", task.exception)
                // ...
                //Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                //updateUI(null)
                Toast.makeText(context, "Signed in Unsuccessfully", Toast.LENGTH_SHORT).show()
            }

            // ...
        }
    }

    fun onStart() {
        //currentUser = auth.currentUser
    }

    /*companion object {
        //var googleAccount: GoogleSignInAccount? = null
        //private set
        val currentUser: FirebaseUser? get() = FirebaseAuth.getInstance().currentUser
        //private set
    }*/

    val currentUser: FirebaseUser? get() = FirebaseAuth.getInstance().currentUser

}

/*

fun Context.getFirebase(persistence: Boolean = true) = FirebaseDB(this, persistence)

class FirebaseDB(private val context: Context, persistence: Boolean = true) {

    private val firebaseInstance: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        firebaseInstance.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(persistence)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
    }

    private fun <TResult> Task<TResult>.await(): TResult = Tasks.await(this)

    */
/*fun storeAllSettings() {
        FirebaseAuth.getInstance().currentUser?.let {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference(it.uid).child("/settings")
            ref.setValue(context.defaultSharedPreferences.all.toJson())
        }
    }*//*


    */
/*fun loadAllSettings(afterLoad: () -> Unit = {}) {
        FirebaseAuth.getInstance().currentUser?.let {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference(it.uid).child("/settings")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        val value = p0.getValue(String::class.java)
                        Loged.d("Value is: $value")
                        val loadedSettings = value?.fromJson<Map<String, *>>()
                        val edit = context.defaultSharedPreferences.edit()
                        if (loadedSettings != null) {
                            for (i in loadedSettings) {
                                when (i.value) {
                                    is String -> edit.putString(i.key, i.value as String)
                                    is Int -> edit.putInt(i.key, i.value as Int)
                                    is Float -> edit.putFloat(i.key, i.value as Float)
                                    is Long -> edit.putLong(i.key, i.value as Long)
                                    is Boolean -> edit.putBoolean(i.key, i.value as Boolean)
                                    else -> null
                                }?.apply()
                            }
                        }
                        edit.apply()
                    } catch (e: Exception) {
                    }
                    val newValue = context.defaultSharedPreferences.getLong("pref_duration", 3_600_000)
                    KUtility.currentDurationTime = newValue
                    KUtility.cancelAlarm(context)
                    KUtility.scheduleAlarm(context, newValue)
                    afterLoad()
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }*//*


    companion object {
        fun firebaseSetup(persistence: Boolean = true) {
            //FirebaseFirestore.setLoggingEnabled(true)
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(persistence)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        }

        suspend fun getAllShows(context: Context): List<ShowInfo> = GlobalScope.async {
            val shows = ShowDatabase.getDatabase(context).showDao().allShows
            val fireShow = try {
                Tasks.await(
                    FirebaseFirestore.getInstance()
                        .collection(FirebaseAuth.getInstance().uid!!)
                        .get()
                ).toObjects(FirebaseShow::class.java)
            } catch (e: Exception) {
                emptyList<FirebaseShow>()
            }
            return@async (shows.map { ShowInfo(it.name, it.link) } + fireShow.map {
                ShowInfo(it.name ?: "N/A", it.url ?: "N/A")
            }.toMutableList().filter { it.name != "N/A" }).distinctBy { it.url }
        }.await()

        suspend fun getAllFireShows(context: Context, source: Source = Source.DEFAULT): List<FirebaseShow> = GlobalScope.async {
            val shows = ShowDatabase.getDatabase(context).showDao().allShows
            val fireShow = try {
                Tasks.await(
                    FirebaseFirestore.getInstance()
                        .collection(FirebaseAuth.getInstance().uid!!)
                        .get(source)
                ).toObjects(FirebaseShow::class.java)
            } catch (e: Exception) {
                emptyList<FirebaseShow>()
            }
            return@async (shows.map { FirebaseShow(it.name, it.link, it.showNum) } + fireShow.toMutableList()
                .filter { it.name != "N/A" }).distinctBy { it.url }
        }.await()

        suspend fun getShowSync(url: String, context: Context): FirebaseShow? = withContext(Dispatchers.Default) {
            val fire = try {
                Tasks.await(
                    FirebaseFirestore.getInstance()
                        .collection(FirebaseAuth.getInstance().uid!!)
                        .document(url.replace("/", "<"))
                        .get()
                ).toObject(FirebaseShow::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val show = try {
                ShowDatabase.getDatabase(context).showDao().getEpisodes(url)
            } catch (e: Exception) {
                emptyList<Episode>()
            }
            if (fire != null) {
                FirebaseShow(fire.name,
                    fire.url,
                    fire.showNum,
                    fire.episodeInfo?.plus(show.map { FirebaseEpisode(it.showName, it.showUrl) })?.distinctBy { it.url } ?: emptyList())
            } else {
                null
            }
        }
    }

    fun storeDb() {
        GlobalScope.launch {
            val showAndEpisode = mutableMapOf<Show, MutableList<Episode>>()
            val db = ShowDatabase.getDatabase(context).showDao()
            val shows = db.allShows
            for (i in shows) {
                showAndEpisode[i] = ShowDatabase.getDatabase(context).showDao().getEpisodesByUrl(i.link)
            }
            for (i in showAndEpisode) {
                storeShow(Pair(i.key, i.value))
            }
        }
    }

    data class FirebaseEpisode(
        val name: String? = null,
        val url: String? = null
    )

    data class FirebaseShow(
        val name: String? = null,
        val url: String? = null,
        var showNum: Int = 0,
        val episodeInfo: List<FirebaseEpisode>? = null
    )

    fun addShow(show: ShowInfo) {
        val data2 = FirebaseShow(show.name, show.url)

        val user = FirebaseAuth.getInstance()
        val store = firebaseInstance
            .collection(user.uid!!)
            .document(show.url.replace("/", "<"))
            .set(data2)

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun removeShow(show: Show) {
        val user = FirebaseAuth.getInstance()
        val store = firebaseInstance
            .collection(user.uid!!)
            .document(show.link.replace("/", "<"))
            .delete()

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun addEpisode(url: String, episode: Episode) {
        val user = FirebaseAuth.getInstance()
        val store = firebaseInstance
            .collection(user.uid!!)
            .document(url.replace("/", "<"))
            .update("episodeInfo", FieldValue.arrayUnion(FirebaseEpisode(episode.showName, episode.showUrl)))

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun removeEpisode(url: String, episode: Episode) {
        val user = FirebaseAuth.getInstance()
        val store = firebaseInstance
            .collection(user.uid!!)
            .document(url.replace("/", "<"))
            .update("episodeInfo", FieldValue.arrayRemove(FirebaseEpisode(episode.name, episode.source.url)))

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun getShowSync(url: String): FirebaseShow? = try {
        firebaseInstance
            .collection(FirebaseAuth.getInstance().uid!!)
            .document(url.replace("/", "<"))
            .get().await().toObject(FirebaseShow::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun getAllShowsSync(): List<FirebaseShow> = try {
        firebaseInstance
            .collection(FirebaseAuth.getInstance().uid!!)
            .get().await().toObjects(FirebaseShow::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    private fun storeShow(showInfo: Pair<ShowInfo, MutableList<Episode>>) {
        val data2 = FirebaseShow(
            showInfo.first.name,
            showInfo.first.url,
            showInfo.second.size,
            showInfo.second.map { FirebaseEpisode(it.name, it.source.url) })

        val user = FirebaseAuth.getInstance()
        val store = firebaseInstance
            .collection(user.uid!!)
            .document(showInfo.first.url.replace("/", "<"))
            .set(data2)

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }
    }

    fun updateShowNum(showInfo: FirebaseShow) {
        val user = FirebaseAuth.getInstance()
        val store = firebaseInstance
            .collection(user.uid!!)
            .document(showInfo.url!!.replace("/", "<"))
            .update("showNum", showInfo.showNum)

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }
    }

}*/
