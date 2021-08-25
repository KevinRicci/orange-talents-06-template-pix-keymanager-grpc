package br.com.zupacademy.keyManager.consulta

import br.com.zupacademy.ConsultaChavePixRequest
import br.com.zupacademy.ConsultaChavePixResponse
import br.com.zupacademy.KeyManagerPixServiceConsultaGrpc
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.client.bcb.ClienteBCB
import br.com.zupacademy.client.itau.ClientItau
import br.com.zupacademy.exception.handler.ErrorHandler
import br.com.zupacademy.keyManager.extension.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultaChavePixEndpoint(
    @Inject val validator: Validator,
    @Inject val clientItau: ClientItau,
    @Inject val clienteBCB: ClienteBCB,
    @Inject val chavePixRepository: ChavePixRepository
): KeyManagerPixServiceConsultaGrpc.KeyManagerPixServiceConsultaImplBase(){

    override fun consultaChavePix(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chavepixInfo = filtro.filtra(chavePixRepository, clientItau, clienteBCB)

        responseObserver.onNext(chavepixInfo.toConsultaChavePixResponse())
        responseObserver.onCompleted()
    }
}