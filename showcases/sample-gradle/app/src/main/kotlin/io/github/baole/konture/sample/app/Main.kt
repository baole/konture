package io.github.baole.konture.sample.app

import io.github.baole.konture.sample.data.UserRepositoryImpl
import io.github.baole.konture.sample.domain.GetUserUseCase

fun main() {
    val repository = UserRepositoryImpl()
    val getUserUseCase = GetUserUseCase(repository)
    val user = getUserUseCase.execute("123")
    println("Hello, ${user.name}!")
}
