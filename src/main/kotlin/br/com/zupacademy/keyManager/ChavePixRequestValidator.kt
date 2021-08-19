package br.com.zupacademy.keyManager

import br.com.zupacademy.ChavePixRequest
import br.com.zupacademy.ChavePixResponse
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.client.ClientItau
import io.grpc.Status
import io.grpc.stub.StreamObserver

class ChavePixRequestValidator {

    companion object {
        fun isValid(
            request: ChavePixRequest,
            response: StreamObserver<ChavePixResponse>,
            clientItau: ClientItau,
            chavePixRepository: ChavePixRepository
        ): Boolean{

            if(request.tipoChave.name == "DESCONHECIDO" || request.tipoConta.name == "DESCONHECIDO"){
                response.onError(Status.INVALID_ARGUMENT
                    .withDescription("Tipo da chave e tipo da conta devem ser preenchidos")
                    .asRuntimeException())
                return false
            }
            if(request.uuidCliente.isNullOrBlank()){
                response.onError(Status.INVALID_ARGUMENT.withDescription("Id do cliente obrigatório").asRuntimeException())
                return false
            }
            if(chavePixRepository.existsByValorChave(request.valorChave)){
                response.onError(Status.ALREADY_EXISTS.withDescription("Já existe uma chave igual").asRuntimeException())
                return false
            }

            if(clientItau.buscaConta(request.uuidCliente, request.tipoConta.name).status().code == 404){
                response.onError(Status.NOT_FOUND.withDescription("Cliente não encontrado").asRuntimeException())
                return false
            }

            when(request.tipoChave.name){
                "CPF" -> {
                    if(!"^[0-9]{11}\$".toRegex().matches(request.valorChave)) {
                        response.onError(Status.INVALID_ARGUMENT.withDescription("CPF inválido").asRuntimeException())
                        return false
                    }
                }
                "EMAIL" -> {
                    if(!emailValido(request.valorChave)){
                        response.onError(Status.INVALID_ARGUMENT.withDescription("Email inválido").asRuntimeException())
                        return false
                    }
                }
                "CELULAR" -> {
                    if(!"^\\+[1-9][0-9]\\d{1,14}$".toRegex().matches(request.valorChave)){
                        response.onError(Status.INVALID_ARGUMENT.withDescription("Celular inválido").asRuntimeException())
                        return false
                    }
                }
                "CHAVE_ALEATORIA" ->{
                    if(!request.valorChave.isNullOrBlank()){
                        response.onError(Status.INVALID_ARGUMENT
                            .withDescription("Chave aleatória não deve possuir valor")
                            .augmentDescription("valor passado: ${request.valorChave}")
                            .asRuntimeException())
                        return false
                    }
                }
            }
            return true
        }


        private fun emailValido(email: String): Boolean{
            return "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
                .toRegex().matches(email)
        }
    }
}