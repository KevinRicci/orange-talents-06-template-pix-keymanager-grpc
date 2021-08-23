package br.com.zupacademy.keyManager.cadastra

import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.ContaAssociada
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.client.bcb.BCBCriaChavePixRequest
import br.com.zupacademy.client.bcb.ClienteBCB
import br.com.zupacademy.client.itau.ClientItau
import br.com.zupacademy.exception.NotFoundException
import br.com.zupacademy.exception.PixKeyExistingException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val clientItau: ClientItau,
    @Inject val clientBCB: ClienteBCB,
    @Inject val chavePixRepository: ChavePixRepository
) {

    @Transactional
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

        //valida o valor da chave de acordo com o tipo da chave
        val valorValido = novaChavePix.tipoChave!!.valida(novaChavePix.valorChave)
        if(!valorValido) throw IllegalArgumentException("valor da chave inválido")

        val chavePix = novaChavePix.toModel()
        chavePixRepository.save(chavePix)

        //cadastra a chave no banco central do brasil
        val bcbResponse = clientBCB.cadastraChave(BCBCriaChavePixRequest(
            chavePix,
            ContaAssociada(
                respostaItau.body().agencia,
                respostaItau.body().numero,
                TipoConta.valueOf(respostaItau.body().tipo),
                respostaItau.body().titular.nome,
                respostaItau.body().titular.cpf
            )
        ))

        println(bcbResponse.status)
        println(bcbResponse.body())
        println(bcbResponse.reason())

        if(bcbResponse.status != HttpStatus.CREATED) throw IllegalStateException("Falha ao cadastrar chave no Banco Central do Brasil (BCB)")
        //atualiza chave com o valor retornado pelo bcb, no caso, para chave tipo aleatória
        chavePix.atualizaChave(bcbResponse.body().key)

        return chavePix
    }
}