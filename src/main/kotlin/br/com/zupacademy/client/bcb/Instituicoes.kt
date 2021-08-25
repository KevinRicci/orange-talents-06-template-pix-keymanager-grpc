package br.com.zupacademy.client.bcb

import br.com.zupacademy.exception.InternalServerErrorException

class Instituicoes {

    fun of(valor: String): String{
        return when(valor){
            "60701190" -> "ITAÚ UNIBANCO S.A."
        else -> throw InternalServerErrorException("Erro ao buscar nome da instituição financeira")
        }
    }
}