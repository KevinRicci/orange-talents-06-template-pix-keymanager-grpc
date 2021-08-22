package br.com.zupacademy.exception

import io.grpc.Status

class GrpcException(
    val status: Status,
    val mensagem: String
) : RuntimeException() {
}