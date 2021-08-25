package br.com.zupacademy.keyManager.consulta

import br.com.zupacademy.ConsultaChavePixResponse
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset

class ChavePixInfo(
    val pixId: String?,
    val uuidCliente: String?,
    val tipoChave: TipoChave,
    val valorChave: String,
    val nomeTitular: String,
    val cpfTitular: String,
    val nomeInstituicao: String,
    val agencia: Int,
    val numeroConta: Int,
    val tipoConta: TipoConta,
    val horaCadastro: LocalDateTime
) {

    /**
     * Consultas internas por pixId e uuidCliente retornam esses dois campos juntos,
     * consultas pelo valor da chave pix no Banco Central do Brasil não retornam esses campos.
     * ChavePixInfo é usado nos dois contextos
     */
    fun toConsultaChavePixResponse(): ConsultaChavePixResponse{
        if(this.pixId != null && this.uuidCliente != null){
            return ConsultaChavePixResponse.newBuilder()
                .setPixId(this.pixId)
                .setUuidCliente(this.uuidCliente)
                .setTipoChave(br.com.zupacademy.TipoChave.valueOf(this.tipoChave.name))
                .setValorChave(this.valorChave)
                .setNomeTitular(this.nomeTitular)
                .setCpfTitular(this.cpfTitular)
                .setNomeInstituicao(this.nomeInstituicao)
                .setAgencia(this.agencia)
                .setNumeroConta(this.numeroConta)
                .setTipoConta(br.com.zupacademy.TipoConta.valueOf(this.tipoConta.name))
                .setHoraCadastro(Timestamp.newBuilder().setSeconds(this.horaCadastro.toEpochSecond(ZoneOffset.UTC)).setNanos(this.horaCadastro.nano).build())
                .build()
        }else{
            return ConsultaChavePixResponse.newBuilder()
                .setTipoChave(br.com.zupacademy.TipoChave.valueOf(this.tipoChave.name))
                .setValorChave(this.valorChave)
                .setNomeTitular(this.nomeTitular)
                .setCpfTitular(this.cpfTitular)
                .setNomeInstituicao(this.nomeInstituicao)
                .setAgencia(this.agencia)
                .setNumeroConta(this.numeroConta)
                .setTipoConta(br.com.zupacademy.TipoConta.valueOf(this.tipoConta.name))
                .setHoraCadastro(Timestamp.newBuilder().setSeconds(this.horaCadastro.toEpochSecond(ZoneOffset.UTC)).setNanos(this.horaCadastro.nano).build())
                .build()
        }
    }
}