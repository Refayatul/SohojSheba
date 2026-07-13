package com.bonfire.shohojsheba.data.repositories

import android.content.Context
import com.bonfire.shohojsheba.data.models.User
import com.bonfire.shohojsheba.util.AppError
import com.bonfire.shohojsheba.util.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl private constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : AuthRepository {

    // Singleton Pattern:
    // Ensures only ONE instance of AuthRepositoryImpl exists in the entire app.
    // This prevents memory leaks and ensures all parts of the app share the same auth state.
    companion object {
        @Volatile
        private var instance: AuthRepositoryImpl? = null

        fun getInstance(context: Context): AuthRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: AuthRepositoryImpl(
                    FirebaseAuth.getInstance(),
                    FirebaseFirestore.getInstance(),
                    context.applicationContext
                ).also { instance = it }
            }
        }
    }

    // --- Registration Logic ---
    // 1. Create user in Firebase Auth (handles email/password validation & security)
    // 2. Create user document in Firestore (stores extra profile data like name, phone, etc.)
    override fun register(email: String, password: String, name: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading()) // Signal UI to show spinner
        try {
            // Step 1: Create Auth User
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Step 2: Create Firestore Document
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    name = name
                )
                // 'set' creates or overwrites the document with the given ID
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
                
                emit(Resource.Success(user)) // Success!
            } else {
                emit(Resource.Error("Registration failed"))
            }
        } catch (e: Exception) {
            // Handle errors (e.g., email already in use, weak password)
            emit(Resource.Error(e.message ?: "Registration failed"))
        }
    }

    // --- Login Logic ---
    // 1. Authenticate with Firebase Auth
    // 2. Fetch user profile from Firestore
    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            // Step 1: Auth Login
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Step 2: Fetch Profile
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                
                val user = if (userDoc.exists()) {
                    // Convert Firestore document to User object
                    userDoc.toObject(User::class.java) ?: createDefaultUser(firebaseUser)
                } else {
                    // Fallback: If Auth exists but Firestore doc is missing (rare), create it
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: ""
                    )
                    firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                    newUser
                }
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Login failed"))
        }
    }

    override fun googleSignIn(idToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = if (userDoc.exists()) {
                    userDoc.toObject(User::class.java) ?: createDefaultUser(firebaseUser)
                } else {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: ""
                    )
                    firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                    newUser
                }
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Google sign-in failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Google sign-in failed"))
        }
    }

    // --- Logout Logic ---
    override fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            // 1. Sign out from Firebase
            firebaseAuth.signOut()
            
            // 2. Sign out from Google (important for switching accounts)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("187859520695-03dpjg2k339oi3if24ts12ioip830a79.apps.googleusercontent.com")
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().await()

            // 3. Clear local Firestore cache to ensure next login gets fresh data
            firestore.clearPersistence().await()
            
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Logout failed"))
        }
    }

    // --- Real-time User Observation ---
    // 'callbackFlow' converts a callback-based API (Firebase SnapshotListener) into a Kotlin Flow.
    // This allows the UI to automatically update whenever the user's data changes in the cloud.
    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            trySend(null) // No user logged in
            close() // Close the flow
            return@callbackFlow
        }

        // Listen for changes to the user's document in Firestore
        val userDocRef = firestore.collection("users").document(firebaseUser.uid)
        val listener = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // If error, return basic info from Auth
                trySend(createDefaultUser(firebaseUser))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // Parse the updated user data
                val user = snapshot.toObject(User::class.java) ?: createDefaultUser(firebaseUser)
                trySend(user) // Emit the new user data
            } else {
                // Document missing, create it
                val newUser = createDefaultUser(firebaseUser)
                userDocRef.set(newUser).addOnSuccessListener {
                    trySend(newUser)
                }.addOnFailureListener {
                    trySend(newUser)
                }
            }
        }

        // IMPORTANT: Remove the listener when the Flow is cancelled (e.g., user leaves the screen)
        awaitClose { listener.remove() }
    }

    private fun createDefaultUser(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = firebaseUser.displayName ?: ""
        )
    }

    override fun updateUser(user: User): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("No user logged in"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Update failed"))
        }
    }

    override fun resetPassword(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            // Send password reset email (Firebase handles it even for non-existent accounts)
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Password reset failed"))
        }
    }
}
