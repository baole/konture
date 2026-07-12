package io.github.baole.konture.sample.domain

class GetUserUseCase(private val userRepository: UserRepository) {
    fun execute(id: String): User {
        return userRepository.getUser(id)
    }
}
