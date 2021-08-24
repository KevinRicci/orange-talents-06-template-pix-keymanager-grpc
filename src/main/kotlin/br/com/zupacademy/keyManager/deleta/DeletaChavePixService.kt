package br.com.zupacademy.keyManager.deleta

import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.ValidUUID
import br.com.zupacademy.client.bcb.BCBDeletaChavePixRequest
import br.com.zupacademy.client.bcb.ClienteBCB
import br.com.zupacademy.exception.NotFoundException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class DeletaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val clienteBCB: ClienteBCB
) {

    @Transactional
    fun deleta(@NotBlank @ValidUUID pixId: String,
               @NotBlank @ValidUUID uuidCliente: String){
        //verifica se a chave existe e pertence ao cliente
        val chavePix = chavePixRepository.findByIdAndUuidCliente(UUID.fromString(pixId), uuidCliente)
            ?: throw NotFoundException("Chave pix não encontrada ou não pertence ao cliente")

        chavePixRepository.deleteById(UUID.fromString(pixId))
        //deleta a chave no banco central do brasil
        val bcbResponse = clienteBCB.deletaChave(chavePix.valorChave, BCBDeletaChavePixRequest(
            chavePix.valorChave
        ))
        if(bcbResponse.status != HttpStatus.OK) throw IllegalStateException("Não foi possível deletar a chave no Banco Central do Brasil")
    }
}