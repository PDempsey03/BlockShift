package com.blockshift.model.db

class OfflineUserRepository(private val userDao: UserDao) {
    suspend fun addUser(user:User) {
        userDao.insert(user)
    }
}