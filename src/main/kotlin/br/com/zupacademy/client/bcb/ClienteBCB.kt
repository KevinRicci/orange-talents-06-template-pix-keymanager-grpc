package br.com.zupacademy.client.bcb

import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ContaAssociada
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

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
            "CPF" -> KeyType.CPF
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
                else -> null
            }
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
    val accountType: AccountType?
){
    val participant: String = "60701190" //itau ispb
}

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