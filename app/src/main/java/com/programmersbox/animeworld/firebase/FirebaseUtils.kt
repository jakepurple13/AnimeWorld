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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.programmersbox.anime_db.EpisodeWatched
import com.programmersbox.anime_db.ShowDbModel
import com.programmersbox.anime_sources.Sources
import com.programmersbox.animeworld.R
import com.programmersbox.loggingutils.Loged
import com.programmersbox.loggingutils.f
import com.programmersbox.loggingutils.fa
import com.programmersbox.loggingutils.fd
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.rxutils.toLatestFlowable
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject

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
                .setLogo(R.mipmap.big_logo)
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


object FirebaseDb {

    private const val DOCUMENT_ID = "favoriteShows"
    private const val CHAPTERS_ID = "episodesWatched"

    private val db = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            /*.setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)*/
            //.setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            //.setCacheSizeBytes()
            .build()
    }

    private fun <TResult> Task<TResult>.await(): TResult = Tasks.await(this)

    private val showDoc2 get() = FirebaseAuthentication.currentUser?.let { db.collection("animeworld").document(DOCUMENT_ID).collection(it.uid) }
    private val episodeDoc2 get() = FirebaseAuthentication.currentUser?.let { db.collection("animeworld").document(CHAPTERS_ID).collection(it.uid) }

    private data class FirebaseAllShows(val first: String = DOCUMENT_ID, val second: List<FirebaseShowDbModel> = emptyList())

    private data class FirebaseShowDbModel(
        val title: String? = null,
        val description: String? = null,
        val showUrl: String? = null,
        val imageUrl: String? = null,
        val source: Sources? = null,
        var numEpisodes: Int = 0,
    )

    private data class FirebaseEpisodeWatched(
        val url: String? = null,
        val name: String? = null,
        val showUrl: String? = null,
    )

    private fun FirebaseShowDbModel.toShowDbModel() = ShowDbModel(
        title.orEmpty(),
        description.orEmpty(),
        showUrl.orEmpty(),
        imageUrl.orEmpty(),
        source ?: Sources.GOGOANIME,
        numEpisodes,
    )

    private fun ShowDbModel.toFirebaseShowDbModel() = FirebaseShowDbModel(
        title,
        description,
        showUrl,
        imageUrl,
        source,
        numEpisodes,
    )

    private fun FirebaseEpisodeWatched.toEpisodeModel() = EpisodeWatched(
        url.orEmpty().pathToUrl(),
        name.orEmpty(),
        showUrl.orEmpty().pathToUrl(),
    )

    private fun EpisodeWatched.toFirebaseEpisodeWatched() = FirebaseEpisodeWatched(
        url,
        name,
        showUrl,
    )

    private fun String.urlToPath() = replace("/", "<")
    private fun String.pathToUrl() = replace("<", "/")

    fun getAllShows() = showDoc2
        ?.get()
        ?.await()
        ?.toObjects<FirebaseShowDbModel>()
        ?.map { it.toShowDbModel() }
        .orEmpty()

    fun insertShow(showDbModel: ShowDbModel) = Completable.create { emitter ->
        showDoc2?.document(showDbModel.showUrl.urlToPath())
            ?.set(showDbModel.toFirebaseShowDbModel())
            ?.addOnSuccessListener { emitter.onComplete() }
            ?.addOnFailureListener { emitter.onError(it) } ?: emitter.onComplete()
    }

    fun removeShow(showDbModel: ShowDbModel) = Completable.create { emitter ->
        showDoc2?.document(showDbModel.showUrl.urlToPath())
            ?.delete()
            ?.addOnSuccessListener { emitter.onComplete() }
            ?.addOnFailureListener { emitter.onError(it) } ?: emitter.onComplete()
    }

    fun updateShow(showDbModel: ShowDbModel) = Completable.create { emitter ->
        showDoc2?.document(showDbModel.showUrl.urlToPath())
            ?.update("numEpisodes", showDbModel.numEpisodes)
            ?.addOnSuccessListener { emitter.onComplete() }
            ?.addOnFailureListener { emitter.onError(it) } ?: emitter.onComplete()
    }

    fun insertEpisodeWatched(episodeWatched: EpisodeWatched) = Completable.create { emitter ->
        episodeDoc2?.document(episodeWatched.showUrl.urlToPath())
            ?.update("watched", FieldValue.arrayUnion(episodeWatched.toFirebaseEpisodeWatched()))
            //?.collection(episodeWatched.url.urlToPath())
            //?.document("watched")
            //?.set(episodeWatched.toFirebaseEpisodeWatched())
            ?.addOnSuccessListener { emitter.onComplete() }
            ?.addOnFailureListener { emitter.onError(it) } ?: emitter.onComplete()
    }

    fun removeEpisodeWatched(episodeWatched: EpisodeWatched) = Completable.create { emitter ->
        episodeDoc2?.document(episodeWatched.showUrl.urlToPath())
            ?.update("watched", FieldValue.arrayRemove(episodeWatched.toFirebaseEpisodeWatched()))
            //?.collection(episodeWatched.url.urlToPath())
            //?.document("watched")
            //?.delete()
            ?.addOnSuccessListener { emitter.onComplete() }
            ?.addOnFailureListener { emitter.onError(it) } ?: emitter.onComplete()
        emitter.onComplete()
    }

    class FirebaseListener {

        private var listener: ListenerRegistration? = null

        fun getAllShowsFlowable() = PublishSubject.create<List<ShowDbModel>> { emitter ->
            require(listener == null)
            listener = showDoc2?.addSnapshotListener { value, error ->
                value?.toObjects<FirebaseShowDbModel>()?.map { it.toShowDbModel() }?.let { emitter.onNext(it) }
            }
            if (listener == null) emitter.onNext(emptyList())
        }.toLatestFlowable()

        fun findShowByUrl(url: String?) = PublishSubject.create<Boolean> { emitter ->
            require(listener == null)
            listener = showDoc2?.whereEqualTo("showUrl", url)?.addSnapshotListener { value, error ->
                value?.toObjects<FirebaseShowDbModel>()
                    .also { println(it) }
                    ?.map { it.toShowDbModel() }?.let { emitter.onNext(it.isNotEmpty()) }
            }
            if (listener == null) emitter.onNext(false)
        }.toLatestFlowable()

        fun getAllEpisodesByShow(showUrl: String) = PublishSubject.create<List<EpisodeWatched>> { emitter ->
            require(listener == null)
            listener = episodeDoc2
                ?.document(showUrl.urlToPath())
                ?.addSnapshotListener { value, error ->
                    value?.toObject(Watched::class.java)?.watched
                        ?.map { it.toEpisodeModel() }
                        ?.let { emitter.onNext(it) }
                }
            if (listener == null) emitter.onNext(emptyList())
        }.toLatestFlowable()

        fun getAllEpisodesByShow(showDbModel: ShowDbModel) = getAllEpisodesByShow(showDbModel.showUrl)

        fun unregister() {
            listener?.remove()
        }

    }

    private class Watched(val watched: List<FirebaseEpisodeWatched> = emptyList())

    /*suspend fun uploadAllItems2(dao: MangaDao, context: Context) {
        //Todo: make a workmanager request for this
        //throw Exception("Dont forget to get current firestore items")
        val m = listOfNotNull(getAllManga(), dao.getAllMangaSync()).flatten().map { it.toFirebaseManga().apply { chapterCount = it.numChapters } }
        m.forEachIndexed { index, firebaseManga ->
            firebaseManga.mangaUrl?.replace("/", "<")?.let { it1 -> mangaDoc2?.document(it1)?.set(firebaseManga) }
                ?.addOnSuccessListener {
                    if (index >= m.size) {
                        runOnUIThread {
                            Toast.makeText(context, "Finished Manga", Toast.LENGTH_LONG).show()
                        }
                    }
                    Loged.d("Success!")
                }?.addOnFailureListener {
                    Loged.wtf("Failure!")
                }?.addOnCompleteListener {
                    Loged.d("All done!")
                }
        }

        *//*val c = listOfNotNull(getAllChapters(), dao.getAllChapters()).flatten().distinctBy { it.url }.map { it.toFirebaseChapter() }
        var cCount = 0
        c
            .forEach {
                it.mangaUrl?.replace("/", "<")?.let { it1 -> it.url?.let { it2 -> chapterDoc2?.document(it1)?.collection(it2)?.add(it } }
                    ?.addOnSuccessListener {
                        cCount++
                        if (cCount >= m.size) {
                            runOnUIThread {
                                Toast.makeText(context, "Finished Chapters", Toast.LENGTH_LONG).show()
                            }
                        }
                        Loged.d("Success!")
                    }?.addOnFailureListener {
                        Loged.wtf("Failure!")
                    }?.addOnCompleteListener {
                        Loged.d("All done!")
                    }
            }*//*

        *//*chapterDoc2
            ?.set(CHAPTERS_ID to dao.getAllChapters().map { it.toFirebaseChapter() })
            ?.addOnSuccessListener {
                Loged.d("Success!")
            }?.addOnFailureListener {
                Loged.wtf("Failure!")
            }?.addOnCompleteListener {
                Loged.d("All done!")
            }*//*
    }*/

    //private data class FirebaseChapterList(val first: String, val second: List<FirebaseChapter>)

    /*fun addChapter(model: MangaReadChapter) {
        *//*chapterDoc2
            ?.document(model.mangaUrl.replace("/", "<"))
            ?.set()*//*
            //?.update("second", FieldValue.arrayUnion(mangaModel.toFirebaseChapter()))
    }*/

    /*fun getChapters(model: Episode) = chapterDoc2
        ?.document(model.source.url.replace("/", "<"))
        ?.get()
        ?.await()
        ?.toObject<FirebaseChapterList>()
        ?.second
        ?.map { it.toMangaChapter() }*/
    /*?.toObject<FirebaseAllChapter>()
    ?.second?.map { it.toMangaChapter() }*/
    /*?.whereEqualTo("mangaUrl", model.mangaUrl)
    ?.get()
    ?.await()
    ?.toObjects<FirebaseChapter>()
    ?.map { it.toMangaChapter() }*/

    /*suspend fun uploadChapters(dao: MangaDao, context: Context) {
        val c = listOfNotNull(getAllChapters(), dao.getAllChapters()).flatten().distinctBy { it.url }.map { it.toFirebaseChapter() }
            .groupBy { it.mangaUrl }
        var cCount = 0
        c
            .forEach {
                it.key?.replace("/", "<")
                    ?.let { it1 -> chapterDoc2?.document(it1)?.set("chapters" to it.value) }
                    ?.addOnSuccessListener {
                        cCount++
                        if (cCount >= c.size - 1) {
                            runOnUIThread {
                                Toast.makeText(context, "Finished Chapters", Toast.LENGTH_LONG).show()
                            }
                        }
                        Loged.d("Success!")
                    }?.addOnFailureListener {
                        Loged.wtf("Failure!")
                    }?.addOnCompleteListener {
                        Loged.d("All done!")
                    }
            }
    }*/

    /*suspend fun uploadAllItems(dao: MangaDao) {
        mangaDoc?.set(DOCUMENT_ID to dao.getAllMangaSync().map { it.toMangaModel().toFirebaseManga().apply { chapterCount = it.numChapters } })
            ?.addOnSuccessListener {
                Loged.d("Success!")
            }?.addOnFailureListener {
                Loged.wtf("Failure!")
            }?.addOnCompleteListener {
                Loged.d("All done!")
            }

        chapterDoc
            ?.set(CHAPTERS_ID to dao.getAllChapters().map { it.toFirebaseChapter() })
            ?.addOnSuccessListener {
                Loged.d("Success!")
            }?.addOnFailureListener {
                Loged.wtf("Failure!")
            }?.addOnCompleteListener {
                Loged.d("All done!")
            }
    }*/
/*
    fun updateManga2(mangaDbModel: MangaDbModel) = Completable.create { emitter ->
        mangaDbModel.mangaUrl.replace("/", "<").let {
            mangaDoc2
                ?.document(it)
                ?.update("chapterCount", mangaDbModel.numChapters)
                ?.addOnSuccessListener {
                    Loged.d("Success!")
                    emitter()
                }?.addOnFailureListener {
                    Loged.wtf("Failure!")
                    emitter(it)
                }?.addOnCompleteListener {
                    Loged.d("All done!")
                }
        } ?: emitter()
    }

    fun addManga2(mangaModel: MangaModel, chapterSize: Int) = Completable.create { emitter ->
        mangaModel.mangaUrl.replace("/", "<").let {
            mangaDoc2
                ?.document(it)
                ?.set(mangaModel.toFirebaseManga().apply { chapterCount = chapterSize })
                ?.addOnSuccessListener {
                    Loged.d("Success!")
                    emitter()
                }?.addOnFailureListener {
                    Loged.wtf("Failure!")
                    emitter(it)
                }?.addOnCompleteListener {
                    Loged.d("All done!")
                }
        } ?: emitter()
    }

    fun removeManga2(mangaModel: MangaModel) = Completable.create { emitter ->
        mangaModel.toFirebaseManga().mangaUrl?.replace("/", "<")?.let {
            mangaDoc2
                ?.document(it)
                ?.delete()
                ?.addOnSuccessListener {
                    Loged.d("Success!")
                    emitter()
                }?.addOnFailureListener {
                    Loged.wtf("Failure!")
                    emitter(it)
                }?.addOnCompleteListener {
                    Loged.d("All done!")
                }
        } ?: emitter()
    }

    private data class FirebaseManga(
        val title: String? = null,
        val description: String? = null,
        val mangaUrl: String? = null,
        val imageUrl: String? = null,
        val source: Sources? = null,
        var chapterCount: Int = 0
    )

    private data class FirebaseAllManga(val first: String = DOCUMENT_ID, val second: List<FirebaseManga> = emptyList())

    private data class FirebaseAllShows(val first: String = DOCUMENT_ID, val second: List<FirebaseShowDbModel> = emptyList())

    data class FirebaseShowDbModel(
        val title: String? = null,
        val description: String? = null,
        val showUrl: String? = null,
        val imageUrl: String? = null,
        val source: Sources? = null,
        var numEpisodes: Int = 0
    )

    data class FirebaseEpisodeWatched(
        val url: String? = null,
        val name: String? = null,
        val showUrl: String? = null
    )

    class FirebaseListener {

        var listener: ListenerRegistration? = null
            private set

        fun getAllMangaFlowable() = PublishSubject.create<List<MangaDbModel>> { emitter ->
            require(listener == null)
            listener = mangaDoc2?.addSnapshotListener { value, error ->
                value?.toObjects<FirebaseManga>()?.map { it.toMangaDbModel() }?.let { emitter(it) }
            }
            if (listener == null) emitter(emptyList())
        }.toLatestFlowable()

        fun getAllManga() = mangaDoc2
            ?.get()
            ?.await()
            ?.toObjects<FirebaseManga>()
            ?.map { it.toMangaDbModel() }

        fun findMangaByUrlFlowable(url: String) = PublishSubject.create<Boolean> { emitter ->
            listener = mangaDoc2
                ?.whereEqualTo("mangaUrl", url)
                ?.addSnapshotListener { value, error -> emitter(value?.toObjects<FirebaseManga>()?.isNotEmpty()) }
            if (listener == null) emitter(false)
        }.toLatestFlowable()

    }

    fun getAllMangaFlowable2() = mangaDoc2
        ?.get()
        ?.await()
        ?.toObjects<FirebaseManga>()
        ?.map { it.toMangaDbModel() }

    private data class FirebaseChapter(
        val url: String? = null,
        val name: String? = null,
        val mangaUrl: String? = null
    )

    private data class FirebaseAllChapter(val first: String = CHAPTERS_ID, val second: List<FirebaseChapter> = emptyList())

    private fun MangaReadChapter.toFirebaseChapter() = FirebaseChapter(url, name, mangaUrl)
    private fun FirebaseChapter.toMangaChapter() = MangaReadChapter(url!!, name!!, mangaUrl!!)

    fun getAllChapters() = chapterDoc
        ?.get(Source.DEFAULT)
        ?.await()
        ?.toObject(FirebaseAllChapter::class.java)
        ?.second
        ?.map { it.toMangaChapter() }

    private var allChapterFlowableListener: ListenerRegistration? = null

    fun detachChapterListener() {
        allChapterFlowableListener?.remove()
        allChapterFlowableListener = null
    }

    fun getAllChapterFlowable(): Flowable<List<MangaReadChapter>> = PublishSubject.create<List<MangaReadChapter>> { emitter ->
        allChapterFlowableListener?.remove()
        allChapterFlowableListener = chapterDoc?.addSnapshotListener { documentSnapshot, _ ->
            documentSnapshot?.toObject(FirebaseAllChapter::class.java)
                ?.second
                ?.map { it.toMangaChapter() }?.let { emitter(it) }
        }
        if (allChapterFlowableListener == null) emitter(emptyList())
    }.toLatestFlowable().subscribeOn(Schedulers.io())

    fun addChapter(mangaModel: MangaReadChapter) = Completable.create { emitter ->
        chapterDoc
            ?.update("second", FieldValue.arrayUnion(mangaModel.toFirebaseChapter()))
            ?.addOnSuccessListener {
                emitter()
                Loged.d("Success!")
            }?.addOnFailureListener {
                Loged.wtf("Failure!")
                emitter(it)
            }?.addOnCompleteListener {
                Loged.d("All done!")
            } ?: emitter()
    }

    fun removeChapter(mangaModel: MangaReadChapter) = Completable.create { emitter ->
        chapterDoc
            ?.update("second", FieldValue.arrayRemove(mangaModel.toFirebaseChapter()))
            ?.addOnSuccessListener {
                emitter()
                Loged.d("Success!")
            }?.addOnFailureListener {
                Loged.wtf("Failure!")
                emitter(it)
            }?.addOnCompleteListener {
                Loged.d("All done!")
            } ?: emitter()
    }*/

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
