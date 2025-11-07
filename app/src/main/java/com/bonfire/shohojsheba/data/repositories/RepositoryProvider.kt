package com.bonfire.shohojsheba.data.repositories


object RepositoryProvider {


    val firebaseRepository: FirebaseRepository by lazy {
        FirebaseRepository()
    }


}
