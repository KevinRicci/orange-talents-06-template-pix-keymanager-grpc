package br.com.zupacademy.keyManager

import br.com.zupacademy.ChavePixRequest
import br.com.zupacademy.ChavePixRequest.TipoChave.CPF
import br.com.zupacademy.ChavePixRequest.TipoChave.EMAIL
import br.com.zupacademy.KeyManagerPixServiceGrpc
import br.com.zupacademy.TipoConta.CONTA_CORRENTE
import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.TipoChave
import br.com.zupacademy.chavePix.TipoConta
import br.com.zupacademy.client.ClientItau
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class NovaChavePixEndpointTest(
    @Inject val clientChavePixGrpc: KeyManagerPixServiceGrpc.KeyManagerPixServiceBlockingStub,
    @Inject val clientItau: ClientItau,
    @Inject val chavePixRepository: ChavePixRepository
){

    @BeforeEach
    fun setup(){
        chavePixRepository.deleteAll()
    }

    companion object{
        val UUID = java.util.UUID.randomUUID()
    }

    @Test
    fun `deve cadastrar nova chave`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok())

        //ação
        val response = clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                                                    .setUuidCliente(UUID.toString())
                                                    .setTipoConta(CONTA_CORRENTE)
                                                    .setTipoChave(CPF)
                                                    .setValorChave("22779078049")
                                                    .build())
        //validação
        assertNotNull(response.pixId)
        assertEquals(1, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave quando ela já existir`(){
        //cenário
        chavePixRepository.save(ChavePix(
                        UUID.toString(),
                        TipoChave.EMAIL,
                        "kevin@gmail.com",
                        TipoConta.CONTA_POUPANCA
                    ))
        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(
                ChavePixRequest.newBuilder()
                    .setUuidCliente(UUID.toString())
                    .setTipoConta(CONTA_CORRENTE)
                    .setTipoChave(EMAIL)
                    .setValorChave("kevin@gmail.com")
                    .build()
            )}
        //validação
        assertEquals(Status.ALREADY_EXISTS.code, thrown.status.code)
        assertEquals("Já existe uma chave igual", thrown.status.description)
    }

    @Test
    fun `deve gerar chave aleatória`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok())

        //ação
        val response = clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
            .setUuidCliente(UUID.toString())
            .setTipoChave(ChavePixRequest.TipoChave.CHAVE_ALEATORIA)
            .setTipoConta(br.com.zupacademy.TipoConta.CONTA_CORRENTE)
            .setValorChave("")
            .build())

        //validação
        assertNotNull(response.pixId)
        assertEquals(1, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave quando cliente nao for encontrado`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.notFound())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                .setUuidCliente(UUID.toString())
                .setTipoConta(br.com.zupacademy.TipoConta.CONTA_CORRENTE)
                .setTipoChave(ChavePixRequest.TipoChave.CELULAR)
                .setValorChave("+5511944536746")
                .build())
        }

        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertEquals("Cliente não encontrado", response.status.description)
    }

    @Test
    fun `nao deve cadastrar chave com parâmetros inválidos`(){
        //cenário

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder().build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertEquals("Tipo da chave e tipo da conta devem ser preenchidos", response.status.description)
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave com uuid do cliente inválido`(){
        //cenário
        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                .setUuidCliente("")
                .setTipoConta(br.com.zupacademy.TipoConta.CONTA_POUPANCA)
                .setTipoChave(ChavePixRequest.TipoChave.CHAVE_ALEATORIA)
                .build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertEquals("Id do cliente obrigatório", response.status.description)
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave com email inválido`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_POUPANCA.name))
            .thenReturn(HttpResponse.ok())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                .setUuidCliente(UUID.toString())
                .setTipoConta(br.com.zupacademy.TipoConta.CONTA_POUPANCA)
                .setTipoChave(ChavePixRequest.TipoChave.EMAIL)
                .setValorChave("aaa.com")
                .build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertEquals("Email inválido", response.status.description)
        assertEquals(0, chavePixRepository.count())
    }

    @MockBean(ClientItau::class)
    fun clientItauMock(): ClientItau{
        return Mockito.mock(ClientItau::class.java)
    }

    @Factory
    class Clients{

        @Bean
        fun clientChavePixGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerPixServiceGrpc.KeyManagerPixServiceBlockingStub{
            return KeyManagerPixServiceGrpc
                .newBlockingStub(channel)
        }
    }
}