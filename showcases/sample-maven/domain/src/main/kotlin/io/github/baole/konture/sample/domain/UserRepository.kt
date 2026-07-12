package io.github.baole.konture.sample.domain

interface UserRepository {
    fun getUser(id: String): User
}
