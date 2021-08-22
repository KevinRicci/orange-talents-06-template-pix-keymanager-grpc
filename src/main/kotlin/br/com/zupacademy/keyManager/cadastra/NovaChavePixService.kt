package br.com.zupacademy.keyManager.cadastra

import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.client.ClientItau
import br.com.zupacademy.exception.GrpcException
import br.com.zupacademy.exception.NotFoundException
import br.com.zupacademy.exception.PixKeyExistingException
import br.com.zupacademy.exception.handler.ErrorHandler
import io.grpc.Status
import io.micronaut.validation.Validated
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val clientItau: ClientItau,
    @Inject val chavePixRepository: ChavePixRepository
) {

    fun registra(@Valid novaChavePix: NovaChavePix): ChavePix{
        //verifica se a chave já existe
        if(chavePixRepository.existsByValorChave(novaChavePix.valorChave)){
            throw PixKeyExistingException("chave já existente")
        }

        //busca conta e verifica se existe cliente com esse id e tipo de conta
        val respostaItau = clientItau.buscaConta(
            novaChavePix.uuidCliente,
            novaChavePix.tipoConta!!.name
        )
        if(respostaItau.status.code == 404) throw NotFoundException("cliente não encontrado")
        val valorValido = novaChavePix.tipoChave!!.valida(novaChavePix.valorChave)
        if(!valorValido) throw IllegalArgumentException("valor da chave inválido")

        val chavePix = novaChavePix.toModel()
        chavePixRepository.save(chavePix)

        return chavePix
    }
}