package br.com.zupacademy.keyManager

import br.com.zupacademy.*
import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.client.ClientItau
import br.com.zupacademy.exception.GrpcException
import br.com.zupacademy.exception.handler.ErrorHandler
import br.com.zupacademy.keyManager.cadastra.NovaChavePixService
import br.com.zupacademy.keyManager.extension.toModel
import com.google.protobuf.Any
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@ErrorHandler
@Singleton
class ChavePixEndpoint(
    @Inject val clientItau: ClientItau,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val novaChavePixService: NovaChavePixService
) : KeyManagerPixServiceGrpc.KeyManagerPixServiceImplBase(){

    override fun cadastraChave(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        val novaChavePix = request.toModel()

        val chaveCriada = novaChavePixService.registra(novaChavePix)
        responseObserver.onNext(ChavePixResponse.newBuilder()
            .setPixId(chaveCriada.id.toString())
            .build())
        responseObserver.onCompleted()
    }

    override fun deletaChave(request: DeletaChaveRequest, responseObserver: StreamObserver<DeletaChaveResponse>) {
        if(!chavePixRepository.existsByIdAndUuidCliente(UUID.fromString(request.pixId), request.uuidCliente)){
            responseObserver.onError(
                Status.NOT_FOUND
                .withDescription("Chave pix ou id do cliente não encontrado")
                .asRuntimeException())
            return
        }

        chavePixRepository.deleteById(UUID.fromString(request.pixId))
        responseObserver.onNext(
            DeletaChaveResponse.newBuilder()
            .setResposta("Chave deletada")
            .build())
        responseObserver.onCompleted()
    }
}