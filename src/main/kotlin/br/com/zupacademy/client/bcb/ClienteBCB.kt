package br.com.zupacademy.client.bcb

import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ContaAssociada
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.client.bcb.KeyType.CPF
import br.com.zupacademy.keyManager.consulta.ChavePixInfo
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime
import kotlin.IllegalStateException

@Client("\${bcb-url}")
interface ClienteBCB {

    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Post("/api/v1/pix/keys")
    fun cadastraChave(@Body bcbChavePixRequest: BCBCriaChavePixRequest): HttpResponse<BCBCriaChavePixResponse>

    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Delete ("/api/v1/pix/keys/{key}")
    fun deletaChave(@PathVariable key: String, @Body bcbDeletaChavePixRequest: BCBDeletaChavePixRequest): HttpResponse<BCBDeletaChavePixResponse>

    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Get("/api/v1/pix/keys/{key}")
    fun buscaChave(@PathVariable key: String): HttpResponse<BCBBuscaChavePixResponse>
}

data class BCBBuscaChavePixResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
){

    fun toModel(): ChavePixInfo{
        return ChavePixInfo(
            pixId = null,
            uuidCliente = null,
            tipoChave = when(this.keyType.name){
                "CPF" ->{
                    TipoChave.CPF
                }
                "EMAIL" ->{
                    TipoChave.EMAIL
                }
                "PHONE" ->{
                    TipoChave.CELULAR
                }
                "RANDOM" ->{
                    TipoChave.CHAVE_ALEATORIA
                }
                else -> throw IllegalStateException("Erro ao buscar tipo de chave no Banco Central do Brasil")
            },
            valorChave = this.key,
            nomeTitular = owner.name,
            cpfTitular = owner.taxIdNumber,
            nomeInstituicao = Instituicoes().of(this.bankAccount.participant),
            agencia = bankAccount.branch,
            numeroConta = this.bankAccount.accountNumber,
            tipoConta = when(this.bankAccount.accountType.name){
                "CACC" ->{
                    TipoConta.CONTA_CORRENTE
                }
                "SVGS" ->{
                    TipoConta.CONTA_POUPANCA
                }
                else -> throw IllegalStateException("Erro ao buscar tipo de conta no Banco central do Brasil")
            },
            horaCadastro = this.createdAt
        )
    }
}

data class BCBDeletaChavePixResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
){}

data class BCBDeletaChavePixRequest(
    val key: String
){
    val participant: String = "60701190" //itau ispb
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BCBDeletaChavePixRequest

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

data class BCBCriaChavePixRequest(
    val keyType: KeyType?,
    val key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
){
    constructor(chavePix: ChavePix, contaAssociada: ContaAssociada): this(
        keyType = when(chavePix.tipoChave.name){
            "CPF" -> CPF
            "EMAIL" -> KeyType.EMAIL
            "CELULAR" -> KeyType.PHONE
            "CHAVE_ALEATORIA" -> KeyType.RANDOM
            else -> null
        },
        key = chavePix.valorChave,
        bankAccount = BankAccount(
            branch = contaAssociada.agencia,
            accountNumber = contaAssociada.numero,
            accountType = when(contaAssociada.tipoConta.name){
                "CONTA_CORRENTE" -> AccountType.CACC
                "CONTA_POUPANCA" -> AccountType.SVGS
                else -> throw IllegalArgumentException("Erro ao atribuir tipo de conta")
            },
            participant = "60701190" //itau ispb
        ),
        owner = Owner(
            type = Type.NATURAL_PERSON,
            name = contaAssociada.titular,
            taxIdNumber = contaAssociada.documento
        )
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BCBCriaChavePixRequest

        if (keyType != other.keyType) return false

        return true
    }

    override fun hashCode(): Int {
        return keyType?.hashCode() ?: 0
    }

}

data class Owner(
    val type: Type,
    val name: String,
    val taxIdNumber: String
){}

enum class Type{
    NATURAL_PERSON,
    LEGAL_PERSON
}

data class BankAccount(
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType,
    val participant: String
){}

enum class AccountType{
    CACC,
    SVGS
}

enum class KeyType{
    CPF,
    EMAIL,
    PHONE,
    RANDOM
}

data class BCBCriaChavePixResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
){}