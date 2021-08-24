package br.com.zupacademy.keyManager.cadastra

import br.com.zupacademy.ChavePixRequest
import br.com.zupacademy.ChavePixRequest.TipoChave.CPF
import br.com.zupacademy.ChavePixRequest.TipoChave.EMAIL
import br.com.zupacademy.KeyManagerPixServiceCadastraGrpc
import br.com.zupacademy.chavePix.*
import br.com.zupacademy.client.bcb.*
import br.com.zupacademy.client.itau.ClientItau
import br.com.zupacademy.client.itau.Instituicao
import br.com.zupacademy.client.itau.ItauResponse
import br.com.zupacademy.client.itau.Titular
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
import java.time.LocalDateTime
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class CadastraChavePixEndpointTest(
    @Inject val clientChavePixGrpc: KeyManagerPixServiceCadastraGrpc.KeyManagerPixServiceCadastraBlockingStub,
    @Inject val clientItau: ClientItau,
    @Inject val clientBCB: ClienteBCB,
    @Inject val chavePixRepository: ChavePixRepository
){

    @BeforeEach
    fun setup(){
        chavePixRepository.deleteAll()
    }

    companion object{
        val UUID = java.util.UUID.randomUUID()

        fun criaChavePixBCBRequest(): BCBCriaChavePixRequest{
            return BCBCriaChavePixRequest(ChavePix(
                UUID.toString(),
                TipoChave.CPF,
                "22779078049",
                TipoConta.CONTA_CORRENTE),
                ContaAssociada("1234", "55454", TipoConta.CONTA_CORRENTE, "kevin", "52037557874"
                )
            )
        }

        fun criaChavePixBCBReponse(): BCBCriaChavePixResponse{
            return BCBCriaChavePixResponse(KeyType.CPF,
                "22779078049",
                BankAccount("2121", "1213", AccountType.CACC),
                Owner(Type.NATURAL_PERSON, "kevin", "5203757874"),
                LocalDateTime.now()
            )
        }

        fun criaItauResponse(): ItauResponse{
            return ItauResponse("CONTA_CORRENTE",
                Instituicao("Itaú", "60701190"),
                "1212",
                "1212",
                Titular("c56dfef4-7901-44fb-84e2-a2cefb157890", "Kevin", "52037557874")
            )
        }
    }

    @Test
    fun `deve cadastrar nova chave`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(criaItauResponse()))

        Mockito.`when`(clientBCB.cadastraChave(criaChavePixBCBRequest()))
            .thenReturn(HttpResponse.created(criaChavePixBCBReponse()))

        //ação
        val response = clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                                                    .setUuidCliente(UUID.toString())
                                                    .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
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
                    .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
                    .setTipoChave(EMAIL)
                    .setValorChave("kevin@gmail.com")
                    .build()
            )}
        //validação
        assertEquals(Status.ALREADY_EXISTS.code, thrown.status.code)
        assertEquals("chave já existente", thrown.status.description)
    }

    @Test
    fun `deve gerar chave aleatória`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok(criaItauResponse()))

        Mockito.`when`(clientBCB.cadastraChave(
            BCBCriaChavePixRequest(
                KeyType.RANDOM,
                "*any*",  //como no meio do fluxo é gerado um UUID random provisório, não tenho como saber o valor
                BankAccount("123", "123", AccountType.CACC),
                Owner(Type.NATURAL_PERSON, "kevin", "5203757874")
            )
        )).thenReturn(
            HttpResponse.created(BCBCriaChavePixResponse(
            KeyType.RANDOM, "cdb14b95-ea86-4d18-ae95-56960afdce41",
            BankAccount("1234", "1234", AccountType.CACC),
            Owner(Type.NATURAL_PERSON, "kevin", "5203757874"),
            LocalDateTime.now()
        )))


        //ação
        val response = clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
            .setUuidCliente(UUID.toString())
            .setTipoChave(ChavePixRequest.TipoChave.CHAVE_ALEATORIA)
            .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
            .setValorChave("")
            .build())

        //validação
        assertNotNull(response.pixId)
        assertEquals(1, chavePixRepository.count())
        assertTrue(chavePixRepository.existsByValorChave("cdb14b95-ea86-4d18-ae95-56960afdce41"))
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
                .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
                .setTipoChave(ChavePixRequest.TipoChave.CELULAR)
                .setValorChave("+5511944536746")
                .build())
        }

        //validação
        assertEquals(Status.NOT_FOUND.code, response.status.code)
        assertEquals("cliente não encontrado", response.status.description)
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
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave com uuid do cliente inválido`(){
        //cenário
        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                .setUuidCliente("")
                .setTipoConta(ChavePixRequest.TipoConta.CONTA_POUPANCA)
                .setTipoChave(ChavePixRequest.TipoChave.CHAVE_ALEATORIA)
                .build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
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
                .setTipoConta(ChavePixRequest.TipoConta.CONTA_POUPANCA)
                .setTipoChave(ChavePixRequest.TipoChave.EMAIL)
                .setValorChave("aaa.com")
                .build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertEquals("valor da chave inválido", response.status.description)
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave com celular inválido`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_POUPANCA.name))
            .thenReturn(HttpResponse.ok())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                .setUuidCliente(UUID.toString())
                .setTipoConta(ChavePixRequest.TipoConta.CONTA_POUPANCA)
                .setTipoChave(ChavePixRequest.TipoChave.CELULAR)
                .setValorChave("445367448")
                .build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertEquals("valor da chave inválido", response.status.description)
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave com cpf inválido`() {
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_POUPANCA.name))
            .thenReturn(HttpResponse.ok())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(
                ChavePixRequest.newBuilder()
                    .setUuidCliente(UUID.toString())
                    .setTipoConta(ChavePixRequest.TipoConta.CONTA_POUPANCA)
                    .setTipoChave(ChavePixRequest.TipoChave.CPF)
                    .setValorChave("5305785902577")
                    .build()
            )
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertEquals("valor da chave inválido", response.status.description)
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `nao deve cadastrar chave aleatória quando houver valor`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_POUPANCA.name))
            .thenReturn(HttpResponse.ok())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                .setUuidCliente(UUID.toString())
                .setTipoConta(ChavePixRequest.TipoConta.CONTA_POUPANCA)
                .setTipoChave(ChavePixRequest.TipoChave.CHAVE_ALEATORIA)
                .setValorChave("+5511944587944")
                .build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, response.status.code)
        assertEquals("valor da chave inválido", response.status.description)
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `não deve gerar chave quando cliente BCB retornar erro`(){
        //cenário
        Mockito.`when`(clientItau.buscaConta(UUID.toString(), TipoConta.CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok(criaItauResponse()))
        Mockito.`when`(clientBCB.cadastraChave(criaChavePixBCBRequest()))
            .thenReturn(HttpResponse.badRequest())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.cadastraChave(ChavePixRequest.newBuilder()
                .setUuidCliente(UUID.toString())
                .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
                .setTipoChave(CPF)
                .setValorChave("52037557879")
                .build())
        }

        //validação
        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.FAILED_PRECONDITION.code, response.status.code)
        assertEquals("Falha ao cadastrar chave no Banco Central do Brasil (BCB)", response.status.description)
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
    fun clientChavePixGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerPixServiceCadastraGrpc.KeyManagerPixServiceCadastraBlockingStub{
        return KeyManagerPixServiceCadastraGrpc
            .newBlockingStub(channel)
    }
}
