package br.com.zupacademy.chavePix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long>{

    fun existsByValorChave(valorChave: String): Boolean
}