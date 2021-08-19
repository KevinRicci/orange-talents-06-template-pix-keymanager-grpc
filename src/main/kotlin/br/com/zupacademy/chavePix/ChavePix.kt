package br.com.zupacademy.chavePix

import javax.persistence.*
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotBlank @field:Column(nullable = false)
    val uuidCliente: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) @field:Column(nullable = false)
    val tipoChave: TipoChave,
    @field:NotBlank @field:Column(unique = true, nullable = false) @field:Max(77)
    val valorChave: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) @field:Column(nullable = false)
    val tipoConta: TipoConta
){

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}