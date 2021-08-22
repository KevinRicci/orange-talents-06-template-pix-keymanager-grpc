package br.com.zupacademy.chavePix

enum class TipoChave {
    CHAVE_ALEATORIA{
        override fun valida(valorChave: String): Boolean {
            if(!valorChave.isNullOrBlank()){
                return false
            }
            return true
        }
    },
    CELULAR{
        override fun valida(valorChave: String): Boolean {
            return "^\\+[1-9][0-9]\\d{1,14}$".toRegex().matches(valorChave)
        }
    },
    CPF{
        override fun valida(valorChave: String): Boolean {
            return "^[0-9]{11}\$".toRegex().matches(valorChave)
        }
    },
    EMAIL{
        override fun valida(valorChave: String): Boolean {
            return "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
            .toRegex().matches(valorChave)
        }
    };

    abstract fun valida(valorChave: String): Boolean
}