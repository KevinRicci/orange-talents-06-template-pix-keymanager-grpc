package br.com.zupacademy.client.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erp-itau-url}")
interface ClientItau {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun buscaConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<ItauResponse>
}

data class ItauResponse(
    val tipo: String,
    val instituicao: Instituicao,
    val agencia: String,
    val numero: String,
    val titular: Titular
){}

data class Titular(
    val id: String,
    val nome: String,
    val cpf: String
){}

data class Instituicao(
    val nome: String,
    val ispb: String
){}