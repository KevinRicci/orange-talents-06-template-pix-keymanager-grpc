package br.com.zupacademy.keyManager.consulta

import br.com.zupacademy.*
import br.com.zupacademy.chavePix.ChavePix
import br.com.zupacademy.chavePix.ChavePixRepository
import br.com.zupacademy.chavePix.TipoChave.*
import br.com.zupacademy.chavePix.TipoConta.CONTA_POUPANCA
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChavePixEndpointTest(
    @Inject val clienteBCB: ClienteBCB,
    @Inject val clientItau: ClientItau,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val clientChavePixGrpc: KeyManagerPixServiceConsultaGrpc.KeyManagerPixServiceConsultaBlockingStub
){

    @Test
    fun `deve retornar todas as chaves pix por um cliente id`(){
        //cenário
        chavePixRepository.save(ChavePix(
            uuidCliente = "00c9e540-88b4-481f-8748-40b7eca19442",
            tipoChave = br.com.zupacademy.chavePix.TipoChave.EMAIL,
            valorChave = "kevin@teste.com",
            tipoConta = br.com.zupacademy.chavePix.TipoConta.CONTA_POUPANCA,
            horaCadastro = LocalDateTime.now()
        ))
        chavePixRepository.save(ChavePix(
            uuidCliente = "00c9e540-88b4-481f-8748-40b7eca19442",
            tipoChave = br.com.zupacademy.chavePix.TipoChave.CELULAR,
            valorChave = "+5511997477855",
            tipoConta = br.com.zupacademy.chavePix.TipoConta.CONTA_CORRENTE,
            horaCadastro = LocalDateTime.now()
        ))

        //ação
        val response = clientChavePixGrpc.buscaChaves(BuscaChavesPixRequest.newBuilder()
            .setUuidCliente("00c9e540-88b4-481f-8748-40b7eca19442")
            .build())

        //validação
        assertEquals(2, response.chavesCount)
        assertEquals("kevin@teste.com", response.getChaves(0).valorChave)
        assertEquals("+5511997477855", response.getChaves(1).valorChave)
    }

    @Test
    fun `nao deve retornar as chaves se o cliente id não for informado`(){
        //cenário
        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.buscaChaves(BuscaChavesPixRequest.newBuilder().build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, thrown.status.code)
        assertEquals("uuid do cliente não pode ser vazio ou nulo", thrown.status.description)
    }

    @Test
    fun `deve retornar uma lista vazia caso não encontre nenhuma chave pelo cliente id`(){
        //cenário
        //ação
        val response = clientChavePixGrpc.buscaChaves(BuscaChavesPixRequest.newBuilder()
            .setUuidCliente("180e3c72-1195-4a2b-8562-1932ab4bcc3e")
            .build())

        //validação
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `deve buscar os dados de uma chave pix pelo seu valor`(){
        //cenário
        Mockito.`when`(clienteBCB.buscaChave("+5511998536746"))
            .thenReturn(HttpResponse.ok(BCBBuscaChavePixResponse(
                KeyType.PHONE,
                "+5511998536746",
                BankAccount("123", "123", AccountType.CACC, "60701190"),
                Owner(Type.NATURAL_PERSON, "Kevin", "5203757479"),
                LocalDateTime.now()
            )))

        //ação
        val response = clientChavePixGrpc.consultaChavePix(ConsultaChavePixRequest.newBuilder()
            .setChavePix("+5511998536746")
            .build())

        //validação
        assertNotNull(response)
        assertEquals("+5511998536746", response.valorChave)
        assertEquals(TipoChave.CELULAR, response.tipoChave)
        assertEquals(TipoConta.CONTA_CORRENTE, response.tipoConta)
    }

    @Test
    fun `deve buscar os dados de uma chave pix pelo pixId e uuidCliente`(){
        //cenário
        val CHAVE_CRIADA = chavePixRepository.save(ChavePix(
            uuidCliente = "3a3107f5-a7aa-46e9-826c-6e123fcd1363",
            tipoChave = EMAIL,
            valorChave = "kevin@gmail.com",
            tipoConta = CONTA_POUPANCA
        ))

        Mockito.`when`(clientItau.buscaConta(CHAVE_CRIADA.uuidCliente, CHAVE_CRIADA.tipoConta.name))
            .thenReturn(HttpResponse.ok(ItauResponse(
                tipo = "CONTA_POUPANCA",
                instituicao = Instituicao(nome = "Itaú Unibanco S.A", ispb = "60701190"),
                agencia = "123",
                numero = "123",
                titular = Titular(CHAVE_CRIADA.uuidCliente, nome = "Kevin", cpf = "52037574189")
            )))

        //ação
        val response = clientChavePixGrpc.consultaChavePix(ConsultaChavePixRequest.newBuilder()
            .setPorPixIdEIdCliente(
                ConsultaChavePixRequest.PorPixIdEIdCliente.newBuilder()
                    .setPixId(CHAVE_CRIADA.id.toString())
                    .setUuidCliente(CHAVE_CRIADA.uuidCliente)
                    .build())
            .build())

        //validação
        assertNotNull(response)
        assertEquals("kevin@gmail.com", response.valorChave)
        assertEquals(TipoChave.EMAIL, response.tipoChave)
        assertEquals(TipoConta.CONTA_POUPANCA, response.tipoConta)
    }

    @Test
    fun `não deve retornar a chave quando a busca for pelo valor da chave e o BCB não encontrar`(){
        //cenário
        Mockito.`when`(clienteBCB.buscaChave("kevin@gmail.com"))
            .thenReturn(HttpResponse.notFound())

        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.consultaChavePix(ConsultaChavePixRequest.newBuilder()
                .setChavePix("kevin@gmail.com")
                .build())
        }

        //validação
        assertEquals(Status.NOT_FOUND.code, thrown.status.code)
        assertEquals("Chave pix não encontrada", thrown.status.description)
    }

    @Test
    fun `não deve retornar a chave quando a busca for por pixId e uuidCliente e o sistema interno não encontrar`(){
        //cenário
        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.consultaChavePix(ConsultaChavePixRequest.newBuilder()
                .setPorPixIdEIdCliente(
                    ConsultaChavePixRequest.PorPixIdEIdCliente.newBuilder()
                        .setPixId("3a3107f5-a7aa-46e9-826c-6e123fcd1363")
                        .setUuidCliente("cd091ff7-21a8-44c5-b7f9-e216d3bd3b25")
                        .build())
                .build())
        }

        //validação
        assertEquals(Status.NOT_FOUND.code, thrown.status.code)
        assertEquals("Chave pix não encontrada ou não pertence ao cliente", thrown.status.description)
    }

    @Test
    fun `não deve retornar a chave quando a consulta de chave pix não for passada nenhum valor`(){
        //cenário
        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientChavePixGrpc.consultaChavePix(ConsultaChavePixRequest.newBuilder().build())
        }

        //validação
        assertEquals(Status.INVALID_ARGUMENT.code, thrown.status.code)
        assertEquals("Tipo de busca não especificada", thrown.status.description)
    }


    @MockBean(ClienteBCB::class)
    fun clientBCB(): ClienteBCB{
        return Mockito.mock(ClienteBCB::class.java)
    }

    @MockBean(ClientItau::class)
    fun clientItau(): ClientItau{
        return Mockito.mock(ClientItau::class.java)
    }
}

@Factory
class Clients{

    @Bean
    fun clientChavePixGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerPixServiceConsultaGrpc.KeyManagerPixServiceConsultaBlockingStub{
        return KeyManagerPixServiceConsultaGrpc.newBlockingStub(channel)
    }
}