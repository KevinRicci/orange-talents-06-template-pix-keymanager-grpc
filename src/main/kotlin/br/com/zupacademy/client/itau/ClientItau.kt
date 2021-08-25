package br.com.zupacademy.client.itau

import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.keyManager.consulta.ChavePixInfo
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
){

    /**
     * Usado em conjunto com uma chave pix já buscada para montar um ChavePixInfo
     * @param chavePix Deve pertencer à conta buscada
     */
    fun toModel(chavePix: ChavePix): ChavePixInfo{
        return ChavePixInfo(
            chavePix.id.toString(),
            chavePix.uuidCliente,
            chavePix.tipoChave,
            chavePix.valorChave,
            titular.nome,
            titular.cpf,
            instituicao.nome,
            this.agencia.toInt(),
            this.numero.toInt(),
            TipoConta.valueOf(this.tipo),
            chavePix.horaCadastro
        )
    }
}

data class Titular(
    val id: String,
    val nome: String,
    val cpf: String
){}

data class Instituicao(
    val nome: String,
    val ispb: String
){}