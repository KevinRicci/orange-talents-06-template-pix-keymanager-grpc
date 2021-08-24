package br.com.zupacademy.keyManager.deleta

import br.com.zupacademy.DeletaChaveRequest
import br.com.zupacademy.KeyManagerPixServiceDeletaGrpc
import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.client.bcb.BCBDeletaChavePixRequest
import br.com.zupacademy.client.bcb.ClienteBCB
import br.com.zupacademy.client.itau.ClientItau
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class DeletaChavePixEnpointTest(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val clientItau: ClientItau,
    @Inject val clientBCB: ClienteBCB,
    @Inject val clientChavePixGrpc: KeyManagerPixServiceDeletaGrpc.KeyManagerPixServiceDeletaBlockingStub
){

    lateinit var CHAVE_CADASTRADA: ChavePix

    @BeforeEach
    fun setup(){
        CHAVE_CADASTRADA = chavePixRepository.save(ChavePix(
            UUID.randomUUID().toString(),
            TipoChave.EMAIL,
            "kevin@gmail.com",
            TipoConta.CONTA_CORRENTE
        ))
    }

    @AfterEach
    fun tearDown(){
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve deletar uma chave`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(CHAVE_CADASTRADA.uuidCliente, TipoConta.CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok())

        Mockito.`when`(clientBCB.deletaChave(CHAVE_CADASTRADA.valorChave, BCBDeletaChavePixRequest(CHAVE_CADASTRADA.valorChave)))
            .thenReturn(HttpResponse.ok())

        //ação
        assertEquals(1, chavePixRepository.count())
        val responseDeleta = clientChavePixGrpc.deletaChave(
            DeletaChaveRequest.newBuilder()
            .setPixId(CHAVE_CADASTRADA.id.toString())
            .setUuidCliente(CHAVE_CADASTRADA.uuidCliente)
            .build())

        //validação
        assertEquals(0, chavePixRepository.count())
        assertEquals(CHAVE_CADASTRADA.id.toString(), responseDeleta.pixId)
    }

    @Test
    fun `não deve deletar quando os parâmetros forem inválidos`(){
        //cenário
        //ação
        assertEquals(1, chavePixRepository.count())
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.deletaChave(DeletaChaveRequest.newBuilder()
                .setPixId("1")
                .setUuidCliente("1")
                .build())
        }

        //validação
        assertEquals(1, chavePixRepository.count())
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertNotNull(response?.status?.description)
    }

    @Test
    fun `não deve deletar quando chave for existente mas pertence a outro cliente`(){
        //cenário
        //ação
        assertEquals(1, chavePixRepository.count())
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.deletaChave(DeletaChaveRequest.newBuilder()
                .setUuidCliente(UUID.randomUUID().toString())
                .setPixId(CHAVE_CADASTRADA.id.toString())
                .build())
        }

        //validação
        assertEquals(1, chavePixRepository.count())
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertEquals("Chave pix não encontrada ou não pertence ao cliente", response.status.description)
    }

    @Test
    fun `não deve remover quando chave for inexistente`(){
        //cenário
        //ação
        chavePixRepository.deleteById(CHAVE_CADASTRADA.id)
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.deletaChave(DeletaChaveRequest.newBuilder()
                .setPixId(CHAVE_CADASTRADA.id.toString())
                .setUuidCliente(CHAVE_CADASTRADA.uuidCliente)
                .build())
        }

        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertEquals("Chave pix não encontrada ou não pertence ao cliente", response.status.description)
    }

    @Test
    fun `não deve remover quando o cliente BCB retornar erro`(){
        //cenário
        Mockito.`when`(clientBCB.deletaChave(CHAVE_CADASTRADA.valorChave, BCBDeletaChavePixRequest(CHAVE_CADASTRADA.valorChave)))
            .thenReturn(HttpResponse.unprocessableEntity())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.deletaChave(
                DeletaChaveRequest.newBuilder()
                    .setPixId(CHAVE_CADASTRADA.id.toString())
                    .setUuidCliente(CHAVE_CADASTRADA.uuidCliente)
                    .build()
            )
        }

        //validação
        assertEquals(1, chavePixRepository.count())
        assertEquals(Status.FAILED_PRECONDITION.code, response.status.code)
        assertEquals("Não foi possível deletar a chave no Banco Central do Brasil", response.status.description)
    }

    @MockBean(ClientItau::class)
    fun clientItauMock(): ClientItau {
        return Mockito.mock(ClientItau::class.java)
    }

    @MockBean(ClienteBCB::class)
    fun clientBCBMock(): ClienteBCB{
        return Mockito.mock(ClienteBCB::class.java)
    }
}

@Factory
class Clients{

    @Bean
    fun clienteDeletaChavePixGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerPixServiceDeletaGrpc.KeyManagerPixServiceDeletaBlockingStub{
        return KeyManagerPixServiceDeletaGrpc.newBlockingStub(channel)
    }
}