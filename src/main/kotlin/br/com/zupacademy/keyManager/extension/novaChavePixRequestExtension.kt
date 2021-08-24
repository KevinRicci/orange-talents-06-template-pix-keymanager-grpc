package br.com.zupacademy.keyManager.extension

import br.com.zupacademy.ChavePixRequest
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.keyManager.cadastra.NovaChavePix

fun ChavePixRequest.toModel() : NovaChavePix{
    return NovaChavePix(
        this.uuidCliente,
        when(this.tipoChave.name){
            "UNKNOWN_TIPO_CHAVE" -> null
            else -> TipoChave.valueOf(this.tipoChave.name)
        },
        this.valorChave,
        when(this.tipoConta.name){
            "UNKNOWN_TIPO_CONTA" -> null
            else -> TipoConta.valueOf(this.tipoConta.name)
        }
    )
}


