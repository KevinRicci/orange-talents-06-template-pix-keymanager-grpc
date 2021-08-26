package br.com.zupacademy.keyManager.cadastra

import br.com.zupacademy.*
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.client.itau.ClientItau
import br.com.zupacademy.exception.handler.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CadastraChavePixEndpoint(
    @Inject val clientItau: ClientItau,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val novaChavePixService: NovaChavePixService
) : KeyManagerPixServiceCadastraGrpc.KeyManagerPixServiceCadastraImplBase(){

    override fun cadastraChave(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        val novaChavePix = request.toModel()

        val chaveCriada = novaChavePixService.registra(novaChavePix)
        responseObserver.onNext(ChavePixResponse.newBuilder()
            .setPixId(chaveCriada.id.toString())
            .build())
        responseObserver.onCompleted()
    }
}