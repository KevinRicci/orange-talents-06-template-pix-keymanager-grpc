package br.com.zupacademy.chavePix

import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull @field:Column(nullable = false)
    val uuidCliente: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) @field:Column(nullable = false)
    val tipoChave: TipoChave,
    @field:NotBlank @field:Column(unique = true, nullable = false) @field:Max(77)
    val valorChave: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) @field:Column(nullable = false)
    val tipoConta: TipoConta
){

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    var id: UUID? = null
}