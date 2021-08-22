package br.com.zupacademy.keyManager.deleta

import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.ValidUUID
import br.com.zupacademy.exception.NotFoundException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class DeletaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository
) {

    fun deleta(@NotBlank @ValidUUID pixId: String,
               @NotBlank @ValidUUID uuidCliente: String){
        if(!chavePixRepository.existsByIdAndUuidCliente(UUID.fromString(pixId), uuidCliente)){
            throw NotFoundException("Chave pix não encontrada ou não pertence ao cliente")
        }

        chavePixRepository.deleteById(UUID.fromString(pixId))
    }
}