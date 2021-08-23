package br.com.zupacademy.chavePix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID>{

    fun existsByValorChave(valorChave: String): Boolean
    fun existsByIdAndUuidCliente(id: UUID, uuidCliente: String): Boolean
    fun findByIdAndUuidCliente(id: UUID, uuidCliente: String): ChavePix?
}