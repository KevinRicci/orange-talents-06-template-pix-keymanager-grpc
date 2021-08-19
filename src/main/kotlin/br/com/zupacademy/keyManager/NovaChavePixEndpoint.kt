package br.com.zupacademy.keyManager

import br.com.zupacademy.ChavePixRequest
import br.com.zupacademy.ChavePixResponse
import br.com.zupacademy.KeyManagerPixServiceGrpc
import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.client.ClientItau
import br.com.zupacademy.keyManager.extension.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NovaChavePixEndpoint(
    @Inject val clientItau: ClientItau,
    @Inject val chavePixRepository: ChavePixRepository
) : KeyManagerPixServiceGrpc.KeyManagerPixServiceImplBase(){

    override fun cadastraChave(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        if(!ChavePixRequestValidator.isValid(request, responseObserver, clientItau, chavePixRepository)){
            return
        }
        val chavePix = request.toModel()
        chavePixRepository.save(chavePix)

        responseObserver.onNext(ChavePixResponse.newBuilder()
                                                    .setPixId(chavePix.id!!)
                                                    .build())
        responseObserver.onCompleted()
    }
}