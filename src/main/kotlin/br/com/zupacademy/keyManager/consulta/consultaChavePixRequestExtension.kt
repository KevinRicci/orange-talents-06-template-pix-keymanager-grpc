package br.com.zupacademy.keyManager.consulta

import br.com.zupacademy.ConsultaChavePixRequest
import br.com.zupacademy.ConsultaChavePixRequest.FiltroCase.*
import java.lang.IllegalArgumentException
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChavePixRequest.toModel(validator: Validator): Filtro{
    val filtro = when(filtroCase){
        PORPIXIDEIDCLIENTE ->{
            Filtro.PorId(porPixIdEIdCliente.pixId, porPixIdEIdCliente.uuidCliente)
        }
        CHAVEPIX ->{
            Filtro.PorChave(chavePix)
        }
        FILTRO_NOT_SET ->{
            throw IllegalArgumentException("Tipo de busca não especificada")
        }
    }

    val violations = validator.validate(filtro)
    if(violations.isNotEmpty()){
        throw ConstraintViolationException(violations)
    }
    return filtro
}