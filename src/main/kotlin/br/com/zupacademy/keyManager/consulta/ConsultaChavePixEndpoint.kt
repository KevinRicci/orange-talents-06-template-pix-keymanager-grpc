package br.com.zupacademy.keyManager.consulta

import br.com.zupacademy.*
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.client.bcb.ClienteBCB
import br.com.zupacademy.client.itau.ClientItau
import br.com.zupacademy.exception.handler.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.ZoneOffset
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

    override fun buscaChaves(
        request: BuscaChavesPixRequest,
        responseObserver: StreamObserver<BuscaChavesPixResponse>
    ) {
        if(request.uuidCliente.isNullOrBlank()){
            throw IllegalArgumentException("uuid do cliente não pode ser vazio ou nulo")
        }

        val chaves = chavePixRepository.findByUuidCliente(request.uuidCliente).map {
            BuscaChavesPixResponse.Chaves.newBuilder()
                .setPixId(it.id.toString())
                .setUuidCliente(it.uuidCliente)
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setValorChave(it.valorChave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setHoraCadastro(Timestamp.newBuilder()
                    .setNanos(it.horaCadastro.nano)
                    .setSeconds(it.horaCadastro.toEpochSecond(ZoneOffset.UTC))
                    .build())
                .build()
        }

        responseObserver.onNext(BuscaChavesPixResponse.newBuilder()
            .addAllChaves(chaves)
            .build())
        responseObserver.onCompleted()
    }
}