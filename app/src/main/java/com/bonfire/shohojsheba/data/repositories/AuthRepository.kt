package com.bonfire.shohojsheba.data.repositories

import com.bonfire.shohojsheba.data.models.User
import com.bonfire.shohojsheba.util.Resource
import kotlinx.coroutines.flow.Flow

// This Interface is a "Contract".
// It lists all the things our App's Authentication system MUST be able to do.
// It doesn't say *how* to do it (that's in the Implementation class), just *what* to do.
interface AuthRepository {
    // Returns a 'Flow' (stream) of 'Resource' (Success/Error/Loading) containing a 'User'.
    fun register(email: String, password: String, name: String): Flow<Resource<User>>
    fun login(email: String, password: String): Flow<Resource<User>>
    fun googleSignIn(idToken: String): Flow<Resource<User>>
    fun logout(): Flow<Resource<Unit>>
    
    // Observes the current user. If they log out/in, this Flow updates automatically.
    fun getCurrentUser(): Flow<User?>
    
    fun updateUser(user: User): Flow<Resource<User>>
    fun resetPassword(email: String): Flow<Resource<Unit>>
}
