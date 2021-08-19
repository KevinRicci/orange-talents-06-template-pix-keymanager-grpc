package br.com.zupacademy.keyManager.extension

import br.com.zupacademy.ChavePixRequest
import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import java.util.*

fun ChavePixRequest.toModel() : ChavePix{
    var valor = this.valorChave
    if(this.tipoChave.name == "CHAVE_ALEATORIA"){
        valor = UUID.randomUUID().toString()
    }

    return ChavePix(
        this.uuidCliente,
        TipoChave.valueOf(this.tipoChave.name),
        valor,
        TipoConta.valueOf(this.tipoConta.name)
    )
}