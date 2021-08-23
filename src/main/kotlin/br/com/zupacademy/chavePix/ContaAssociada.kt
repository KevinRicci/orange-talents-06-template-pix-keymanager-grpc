package br.com.zupacademy.chavePix

data class ContaAssociada(
    val agencia: String,
    val numero: String,
    val tipoConta: TipoConta,
    val titular: String,
    val documento: String
) {
}