package io.github.baole.konture.sample.data

import io.github.baole.konture.sample.domain.User
import io.github.baole.konture.sample.domain.UserRepository

class UserRepositoryImpl : UserRepository {
    override fun getUser(id: String): User {
        return User(id, "User-$id")
    }
}
