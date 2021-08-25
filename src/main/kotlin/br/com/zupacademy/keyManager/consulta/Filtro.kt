package br.com.zupacademy.keyManager.consulta

import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.ValidUUID
import br.com.zupacademy.client.bcb.ClienteBCB
import br.com.zupacademy.client.itau.ClientItau
import br.com.zupacademy.exception.NotFoundException
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.util.*
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank

sealed class Filtro {

    abstract fun filtra(chavePixRepository: ChavePixRepository, clientItau: ClientItau, clienteBCB: ClienteBCB): ChavePixInfo

    @Introspected
    class PorId(
        @field:NotBlank @field:ValidUUID val pixId: String,
        @field:NotBlank @field:ValidUUID val uuidCliente: String
    ): Filtro(){

        override fun filtra(chavePixRepository: ChavePixRepository, clientItau: ClientItau, clienteBCB: ClienteBCB): ChavePixInfo {
           val chavePix = chavePixRepository.findByIdAndUuidCliente(UUID.fromString(this.pixId), this.uuidCliente)
               ?: throw NotFoundException("Chave pix não encontrada ou não pertence ao cliente")
            val conta = clientItau.buscaConta(chavePix.uuidCliente, chavePix.tipoConta.name)
            return conta.body().toModel(chavePix)
        }
    }

    @Introspected
    class PorChave(
        @field:NotBlank @field:Max(77) val valorChave: String
    ): Filtro(){

        override fun filtra(chavePixRepository: ChavePixRepository, clientItau: ClientItau, clienteBCB: ClienteBCB): ChavePixInfo {
            val response = clienteBCB.buscaChave(this.valorChave)
            if(response.status != HttpStatus.OK){
                throw NotFoundException("Chave pix não encontrada")
            }
            return response.body().toModel()
        }
    }
}