package br.com.zupacademy.client

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erp-itau-url}")
interface ClientItau {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun buscaConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<Map<String, Any>>
}