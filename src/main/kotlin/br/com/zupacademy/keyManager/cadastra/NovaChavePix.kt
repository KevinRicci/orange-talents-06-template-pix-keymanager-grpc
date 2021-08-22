package br.com.zupacademy.keyManager.cadastra

import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@Introspected
data class NovaChavePix(

    @field:Pattern(regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")
    @field:NotBlank
    val uuidCliente: String,
    @field:NotNull
    val tipoChave: TipoChave?,
    val valorChave: String,
    @field:NotNull
    val tipoConta: TipoConta?
) {

    /**
     * Só use se as properties já foram validadas
     */
    fun toModel(): ChavePix{
        return ChavePix(
            uuidCliente = this.uuidCliente,
            tipoChave = this.tipoChave!!,
            valorChave = if(this.tipoChave == TipoChave.CHAVE_ALEATORIA) UUID.randomUUID().toString() else this.valorChave,
            tipoConta = this.tipoConta!!
        )
    }
}