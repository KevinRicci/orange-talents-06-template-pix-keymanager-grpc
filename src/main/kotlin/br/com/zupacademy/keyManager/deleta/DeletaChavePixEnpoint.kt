package br.com.zupacademy.keyManager.deleta

import br.com.zupacademy.DeletaChaveRequest
import br.com.zupacademy.DeletaChaveResponse
import br.com.zupacademy.KeyManagerPixServiceDeletaGrpc
import br.com.zupacademy.exception.handler.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeletaChavePixEnpoint(
    @Inject val deletaChavePixService: DeletaChavePixService
) : KeyManagerPixServiceDeletaGrpc.KeyManagerPixServiceDeletaImplBase() {

    override fun deletaChave(request: DeletaChaveRequest, responseObserver: StreamObserver<DeletaChaveResponse>) {
        deletaChavePixService.deleta(request.pixId, request.uuidCliente)

        responseObserver.onNext(DeletaChaveResponse.newBuilder()
            .setPixId(request.pixId)
            .build())
        responseObserver.onCompleted()
    }
}